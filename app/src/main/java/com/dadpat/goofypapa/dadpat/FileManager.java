package com.dadpat.goofypapa.dadpat;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Lenovo on 2018/3/23.
 */

public class FileManager {

    LogListen m_logListen;

    Context m_context;



    public FileManager(Context p_context, LogListen p_logListen)
    {
        m_context = p_context;
        m_logListen = p_logListen;
        m_logListen.onLog( "path:" + m_context.getExternalFilesDir(null).getPath() );
    }

    public File createFile(String fileName) {
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}
