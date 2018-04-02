package com.dadpat.goofypapa.dadpat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownLoadFile {

    private Handler m_handler;

    private String m_url, m_path;

    private Context m_context;

    private Object m_obj;

    private DownLoadFileListen m_downloadFileListen;

    DownLoadFile(Context p_context, DownLoadFileListen p_downloadFileListen, String p_url, String p_path, Object p_obj )
    {
        m_url = p_url;
        m_path = p_path;
        m_context = p_context;
        m_obj = p_obj;

        m_downloadFileListen = p_downloadFileListen;

        m_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String t_info = (String)msg.obj;
                if( t_info == "start" )
                {
                    m_downloadFileListen.onStartDownLoad();
                    return;
                }

                if( t_info == "end" )
                {
                    m_downloadFileListen.onEndDownload(true, m_obj);
                    return;
                }

                if( t_info.contains("loading:") )
                {
                    float t_rate = Float.parseFloat(t_info.split(":")[1]);
                    m_downloadFileListen.onUpdateDownloadRate(t_rate);
                    return;
                }

                m_downloadFileListen.onEndDownload( false, m_obj );
            }
        };
    }

    public void start()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message t_message = new Message();
                t_message.obj = "start";
                m_handler.sendMessage(t_message);

                try {
                    URL t_url = new URL(m_url);
                    URLConnection t_conn = t_url.openConnection();

                    int t_contentLength = t_conn.getContentLength();
                    int t_currDownloadSize = 0;

                    InputStream t_is = t_conn.getInputStream();

                    byte[] t_bs = new byte[1024];

                    int t_readLen;

                    OutputStream t_os = new FileOutputStream( m_path );

                    while((t_readLen = t_is.read(t_bs)) > 0){

                        t_currDownloadSize += t_readLen;
                        t_message = new Message();
                        t_message.obj = "loading:" + ((float)t_currDownloadSize / t_contentLength);
                        m_handler.sendMessage(t_message);
                        t_os.write(t_bs, 0, t_readLen);
                    }

                    t_message = new Message();
                    t_message.obj = "end";
                    m_handler.sendMessage(t_message);

                    t_os.close();
                    t_is.close();

                }catch (Exception e){
                    t_message = new Message();
                    t_message.obj = e.toString();
                    m_handler.sendMessage(t_message);
                }
            }
        }).start();
    }
}
