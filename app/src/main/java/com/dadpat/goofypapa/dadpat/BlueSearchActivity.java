package com.dadpat.goofypapa.dadpat;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class BlueSearchActivity extends AppCompatActivity {

    private ImageView m_img_back;

    private BlueManagerStateListen m_blueManagerStateListen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blue_search);

        m_img_back = findViewById(R.id.img_back);

        m_img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        m_blueManagerStateListen = new BlueManagerStateListen() {
            @Override
            public void onStateChange(int p_state) {
                if( p_state == 100 )
                {
                    Control.instance().deleteBlueStateListen(m_blueManagerStateListen);
                    finish();
                }

                if( p_state < 0 )
                {
                    Log.d("DEBUG", "------------1");
                    finish();
                }
            }

            @Override
            public void onScan(int p_code) {

            }
        };

        Control.instance().addBlueStateListen(m_blueManagerStateListen);
        Control.instance().connectBlue();

        if( !Control.instance().isBlueOpen() ) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Control.instance().deleteBlueStateListen(m_blueManagerStateListen);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.d( "--------->", "开启蓝牙" );

            } else if (resultCode == RESULT_CANCELED) {
                Log.d( "--------->", "不允许开启蓝牙" );
                finish();
            }
        }

    }
}
