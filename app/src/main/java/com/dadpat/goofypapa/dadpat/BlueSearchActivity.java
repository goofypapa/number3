package com.dadpat.goofypapa.dadpat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class BlueSearchActivity extends AppCompatActivity {

    private ImageView m_img_back;

    private final int REQUEST_ENABLE_BT = 0xa01;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;

    private BlueManagerStateListen m_blueManagerStateListen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

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
            }

            @Override
            public void onScan(int p_code) {

            }
        };

        Control.instance().addBlueStateListen(m_blueManagerStateListen);
        Control.instance().connectBlue();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Control.instance().deleteBlueStateListen(m_blueManagerStateListen);
    }
}
