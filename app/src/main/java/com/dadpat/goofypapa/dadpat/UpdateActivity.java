package com.dadpat.goofypapa.dadpat;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.widget.TextView;

import java.util.ArrayList;

public class UpdateActivity extends AppCompatActivity {

    public ArrayList<DownLoadFile> m_downloadQueue = null;
    public int m_downloadIndex;

    public Handler m_handler;

    public ArrayList<String> m_listenQueue = null;

    public static UpdateActivity m_instance;

    private TextView m_downloadState;

    private static final int sm_maxDownloadPipe = 6;
    private int m_currDownloadPipe;

    private int m_currDownloadSize = 0;

    private Intent m_toMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        m_instance = this;

        m_toMenu = new Intent(UpdateActivity.this, MenuActivity.class);

        m_downloadState = findViewById(R.id.tv_downState);

        m_handler = new Handler(){
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String t_cmd = (String)msg.obj;

                Log.d("DEBUG", "listen: " + m_listenQueue.size() );
                if( m_listenQueue.size() <= 0 )
                {

                    Control.instance().downloadFiles();
                    Control.instance().downloadImages();

                    m_downloadIndex = 0;
                    m_currDownloadPipe = 0;
                    m_currDownloadSize = 0;



                    Log.d("DEBUG", "update queue: " + m_downloadQueue.size() );
                    if( m_downloadQueue.size() > 0  ) {
                        m_downloadState.setText( "Before downloading");
                        download();
                        return;
                    }

                    startActivity(m_toMenu);
                }
            }
        };

        m_downloadQueue = new ArrayList<>();
        m_listenQueue = new ArrayList<>();


        if( Control.instance().isNetworkConnected() ) {

            Control.instance().tryUpdate();
            Control.instance().tryUpdateBatch();
            Control.instance().tryUpdateAnimal();

            m_downloadState.setText("Search update");
        }else{
            startActivity(m_toMenu);
        }
    }


    public void download()
    {

        while(m_downloadIndex < m_downloadQueue.size() && m_currDownloadPipe < sm_maxDownloadPipe  )
        {
            m_downloadQueue.get(m_downloadIndex).start();

            m_currDownloadPipe++;
            m_downloadIndex++;
        }
    }

    public void downloadEnd()
    {
        if( ++m_currDownloadSize >= m_downloadQueue.size() )
        {
            startActivity(m_toMenu);
            return;
        }

        --m_currDownloadPipe;

        download();

        m_downloadState.setText( "Downloading " + ( (int)Math.floor ( (float)m_downloadIndex / m_downloadQueue.size() * 100 ) ) + "%");
    }

    @Override
    public void onBackPressed() {


    }


}
