package com.dadpat.goofypapa.dadpat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.URL;

public class GetBitmap {

    private String m_url;
    private GetBitmapListen m_gitBitmapListen;
    private Bitmap m_bitmap;
    private Object m_obj;

    public GetBitmap(String p_url, Object p_obj,  GetBitmapListen p_gitBitmapListen)
    {
        m_url = p_url;
        m_obj = p_obj;
        m_gitBitmapListen = p_gitBitmapListen;
    }

    public void get()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    URL conn = new URL(m_url);
                    InputStream in = conn.openConnection().getInputStream();
                    m_bitmap = BitmapFactory.decodeStream(in);
                    in.close();

                    m_gitBitmapListen.callBack(m_obj, m_bitmap);

                }catch (Exception e){

                }
            }
        }).start();
    }
}
