package com.dadpat.goofypapa.dadpat;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BlueManager m_blueManager;

    List<String> m_logList;
    private TextView m_log;

    Button m_btnDeviceManager;
    Button m_btnDownLoadFile;

    Handler m_handler;

    FileManager m_fileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_log = (TextView)findViewById(R.id.tv_log);
        m_logList = new LinkedList<>();

        m_btnDeviceManager = (Button)findViewById(R.id.btn_deviceManager);

        m_btnDownLoadFile = (Button)findViewById(R.id.btn_downLoad);

        m_handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                m_btnDeviceManager.setText( m_blueManager.isConnectDevice() ? "断开连接" : "连接设备" );
            }
        };

        m_fileManager = new FileManager(getApplicationContext(), new LogListen() {
            @Override
            public void onLog(String p_log) {
                m_logList.add(p_log);
                showLog();
            }
        });

        m_blueManager = new BlueManager( getApplicationContext(), new BlueManagerStateListen(){
            @Override
            public void onStateChange(int p_state) {
                Message t_message = new Message();
                t_message.obj = "stateChanged";
                m_handler.sendMessage(t_message);
            }
            @Override
            public void onLog( String p_str )
            {
                m_logList.add(p_str);
                showLog();
            }

            @Override
            public void onScan( String p_code )
            {

            }
        } );



        m_btnDeviceManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_blueManager.isConnectDevice())
                {
                    m_blueManager.desconnect();
                }else {
                    m_blueManager.connect();
                }
            }
        });

        m_btnDownLoadFile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                DownLoadFileListen t_downLoadImg =  new DownLoadFileListen( getApplicationContext(), new LogListen() {
                    @Override
                    public void onLog(String p_log) {
                        m_logList.add(p_log);
                        showLog();
                    }
                },
                    "https://t11.baidu.com/it/u=2662104199,2409942689&fm=173&app=25&f=JPEG?w=580&h=290&s=DFAAAD4727D349D25AE4E1C203003033",
                    getApplicationContext().getExternalFilesDir(null).getPath() + "/" + "haha.jpg" );

                t_downLoadImg.start();
            }
        });
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
