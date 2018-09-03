package com.dadpat.goofypapa.dadpat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class UIMenuActivity extends AppCompatActivity {


    private ImageView m_img_back, m_img_blue, m_img_menu_animal;
    private Intent m_toCardList, m_toBlueSearch;
    private BlueManagerStateListen m_blueManagerStateListen;

    private Handler m_handler;

    private final int REQUEST_ENABLE_BT = 0xa01;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;

    static  final  int  BLUETOOTH_DISCOVERABLE_DURATION = 250;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        m_img_back = findViewById(R.id.img_back);
        m_img_menu_animal = findViewById(R.id.img_menu_animal);
        m_img_blue = findViewById(R.id.img_blue);

        m_handler = new Handler(){
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String t_cmd = (String)msg.obj;
                if( t_cmd == "connectStateChanged" )
                {
                    connectStateChanged();
                }

                if( t_cmd == "toSearch" )
                {
                    Log.d("----------->>", "toSearch");
                    startActivity( m_toBlueSearch );
                }
            }
        };

        m_blueManagerStateListen = new BlueManagerStateListen() {
            @Override
            public void onStateChange(int p_state) {
                Message t_message = new Message();
                t_message.obj = "connectStateChanged";
                m_handler.sendMessage(t_message);
            }

            @Override
            public void onScan(int p_code) {

            }
        };

        Control.instance().addBlueStateListen(m_blueManagerStateListen);

//        new ControlListen() {
//            @Override
//            public void connectStateChanged(int p_connectState) {
//                Message t_message = new Message();
//                t_message.obj = "connectStateChanged";
//                m_handler.sendMessage(t_message);
//            }
//
//            @Override
//            public void onLog(String p_str) {
//
//            }
//        };

        m_toCardList = new Intent(UIMenuActivity.this, UICardListActivity.class);
        m_toBlueSearch = new Intent(UIMenuActivity.this, BlueSearchActivity.class);


        m_img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        m_img_menu_animal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(m_toCardList);
            }
        });

        m_img_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //已经连接的设备点击按钮退出还是
                if( Control.instance().isDeviceConnected() )
                {
                    new AlertDialog.Builder(UIMenuActivity.this)
                            .setMessage("要断开与设备连接吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Control.instance().desConnectBlue();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();

                    return;
                }

                //检测定位权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ( UIMenuActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions( UIMenuActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1 );
                        return;
                    }
                }

                openBlueDevice();
            }
        });
        connectStateChanged();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Control.instance().deleteBlueStateListen(m_blueManagerStateListen);
    }

    @Override
    public void onBackPressed() {


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // 授权被允许
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("-------->", "授权请求被允许");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    openBlueDevice();

                } else {
                    Log.e("-------->", "授权请求被拒绝");

                    new AlertDialog.Builder(UIMenuActivity.this)
                            .setMessage("需要开启定位权限才能使用此功能")
                            .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //引导用户到设置中去进行设置
                                    Intent intent = new Intent();
                                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                    startActivity(intent);

                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();

                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.d( "--------->", "开启蓝牙" );

                Message t_message = new Message();
                t_message.obj = "toSearch";
                m_handler.sendMessage(t_message);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d( "--------->", "不允许开启蓝牙" );
            }
        }

    }

    void connectStateChanged()
    {
        if(Control.instance().isDeviceConnected())
        {
            m_img_blue.setImageResource(R.mipmap.home_icon_conect);
        }else{
            m_img_blue.setImageResource(R.mipmap.home_icon_conect_no);
        }
    }

    private void openBlueDevice()
    {
        if( !Control.instance().isBlueOpen() ) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
            return;
        }
        startActivity(m_toBlueSearch);
    }
}
