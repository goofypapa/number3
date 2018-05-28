package com.dadpat.goofypapa.dadpat;

import java.util.UUID;

public class DBImageInfo {

    public String m_md5;
    public String m_path;

    public DBImageInfo()
    {
        m_md5 = null;
        m_path = "";
    }

    public DBImageInfo(String p_md5, String p_path )
    {
        m_md5 = p_md5;
        m_path = p_path;
    }
}
