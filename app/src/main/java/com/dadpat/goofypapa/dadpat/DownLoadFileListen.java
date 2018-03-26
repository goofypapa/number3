package com.dadpat.goofypapa.dadpat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Lenovo on 2018/3/23.
 */

public class DownLoadFileListen {
    String m_url, m_path;
    LogListen m_logListen;
    Context m_context;

    Handler m_handler;

    DownLoadFileListen(Context p_context, LogListen p_logListen, String p_url, String p_path ){
        m_url = p_url;
        m_path = p_path;
        m_context = p_context;
        m_logListen = p_logListen;

        m_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                m_logListen.onLog((String)msg.obj);
            }
        };
    }

    public void start()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message t_message = new Message();
                t_message.obj = "开始下载...";
                m_handler.sendMessage(t_message);

                try {
                    URL t_url = new URL(m_url);
                    URLConnection t_conn = t_url.openConnection();

                    int t_contentLength = t_conn.getContentLength();

                    InputStream t_is = t_conn.getInputStream();

                    byte[] t_bs = new byte[1024];

                    int t_readLen;

                    OutputStream t_os = new FileOutputStream( m_path );

                    while((t_readLen = t_is.read(t_bs)) > 0){
                        t_message = new Message();
                        t_message.obj = "loading:" + t_readLen;
                        m_handler.sendMessage(t_message);
                        t_os.write(t_bs, 0, t_readLen);
                    }

                    t_message = new Message();
                    t_message.obj = "下载完成。";
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
