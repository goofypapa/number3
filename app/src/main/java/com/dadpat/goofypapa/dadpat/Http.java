package com.dadpat.goofypapa.dadpat;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class Http {

    Context m_context;
    String m_url;
    String m_result;
    boolean m_requesting;
    HttpListen m_httpListen;


    public Http( Context p_context, String p_url, HttpListen p_httpListen )
    {
        m_context = p_context;
        m_url = p_url;
        m_requesting = false;
        m_result = null;
        m_httpListen = p_httpListen;
    }

    public void get( )
    {
        if( m_requesting ) return;
        m_result = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_requesting = true;
                m_result = "";
                try {
                    URL t_url = new URL(m_url);
                    URLConnection t_conn = t_url.openConnection();
                    InputStream t_is = t_conn.getInputStream();

                    BufferedReader t_reader = new BufferedReader(new InputStreamReader(t_is, Charset.forName("UTF-8")));

                    String t_line = null;

                    while( (t_line = t_reader.readLine()) != null )
                    {
                        if( m_result == null )
                        {
                            m_result = t_line;
                            continue;
                        }
                        m_result += t_line;
                    }

                    m_httpListen.callBack( m_result );

                }catch (Exception e){

                }
                m_requesting = false;
            }
        }).start();
    }
}
