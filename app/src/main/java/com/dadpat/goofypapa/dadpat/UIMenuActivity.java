package com.dadpat.goofypapa.dadpat;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class UIMenuActivity extends AppCompatActivity {


    private ImageView m_img_back, m_img_blue, m_img_menu_animal;
    private Intent m_toCardList, m_toBlueSearch;
    private BlueManagerStateListen m_blueManagerStateListen;

    private Handler m_handler;

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
            public void onScan(String p_code) {

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
//                finish();
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
                startActivity(m_toBlueSearch);
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

    void connectStateChanged()
    {
        if(Control.instance().isDeviceConnected())
        {
            m_img_blue.setImageResource(R.mipmap.home_icon_conect);
        }else{
            m_img_blue.setImageResource(R.mipmap.home_icon_conect_no);
        }
    }

}
