package com.dadpat.goofypapa.dadpat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UIMainActivity extends AppCompatActivity {

    Intent m_toMenu;
    Intent m_toUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Control.createInstance(getApplicationContext());


        m_toMenu = new Intent(UIMainActivity.this, UIMenuActivity.class);
        m_toUpdate = new Intent( UIMainActivity.this, UIUpdateActivity.class);
//        startActivity(m_toMenu);

        startActivity(m_toUpdate);
    }
}
