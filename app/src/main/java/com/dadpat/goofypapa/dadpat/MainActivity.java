package com.dadpat.goofypapa.dadpat;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Intent m_toMenu;
    Intent m_toUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Control.createInstance(getApplicationContext());


        m_toMenu = new Intent(MainActivity.this, MenuActivity.class);
        m_toUpdate = new Intent( MainActivity.this, UpdateActivity.class);
//        startActivity(m_toMenu);

        startActivity(m_toUpdate);
    }
}
