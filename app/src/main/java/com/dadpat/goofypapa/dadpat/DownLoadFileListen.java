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

public interface DownLoadFileListen {
    public void onLog( String p_log );
    public void onStartDownLoad();
    public void onEndDownload( boolean p_state, Object p_obj );
    public void onUpdateDownloadRate( float p_rate );
}
