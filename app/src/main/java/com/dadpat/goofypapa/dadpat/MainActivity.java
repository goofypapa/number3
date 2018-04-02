package com.dadpat.goofypapa.dadpat;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Control m_control;

    private List<String> m_logList;
    private TextView m_log;

    private Button m_btnDeviceManager;
    private Button m_btnClear;

    private Handler m_handler;

    private FileManager m_fileManager;

    private LogListen m_logListen;

    private static String sm_serviceHost = "http://www.dadpat.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_log = (TextView)findViewById(R.id.tv_log);
        m_logList = new LinkedList<>();

        m_btnDeviceManager = (Button)findViewById(R.id.btn_deviceManager);

        m_btnClear = (Button)findViewById(R.id.btn_clear);

        m_handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                m_btnDeviceManager.setText( m_control.isDeviceConnected() ? "断开连接" : "连接设备" );
            }
        };

        m_logListen = new LogListen() {
            @Override
            public void onLog(String p_log) {
                m_logList.add(p_log);
                showLog();
            }
        };

        m_fileManager = new FileManager(getApplicationContext(), m_logListen);

        m_control = new Control(getApplicationContext(), new ControlListen() {
            @Override
            public void connectStateChanged(int p_connectState) {
                Message t_message = new Message();
                t_message.obj = "" + p_connectState;
                m_handler.sendMessage(t_message);
            }

            @Override
            public void onLog(String p_str) {
                m_logList.add(p_str);
                showLog();
            }
        });



        m_btnDeviceManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_control.isDeviceConnected())
                {
                    m_control.desConnectBlue();
                }else {
                    m_control.connectBlue();
                }
            }
        });

        m_btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_control.clearData();
            }
        });

        Db_Animals t_db = new Db_Animals(getApplicationContext(), m_logListen);

        m_logList.add("group list:");
        showLog();

        ArrayList<Integer> t_list = t_db.getAnimalGroupList();

        for( int i = 0; i < t_list.size(); ++i )
        {
            m_logList.add("\tgroup: " + t_list.get(i));
            showLog();
        }

//        t_db.insertTest();
//        t_db.getListInfo();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


    private void showLog()
    {
        while( m_logList.size() > 20 )
        {
            m_logList.remove(0);
        }

        String t_log = "";
        for( String item : m_logList )
        {
            t_log = t_log.isEmpty() ? item : t_log + "\n" + item;
        }

        m_log.setText(t_log);
    }
}
