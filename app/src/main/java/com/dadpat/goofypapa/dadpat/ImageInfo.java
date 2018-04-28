package com.dadpat.goofypapa.dadpat;

import java.util.UUID;

public class ImageInfo {
    public String m_url;
    public String m_md5;

    public ImageInfo()
    {
        m_url = "";
        m_md5 = null;
    }

    public ImageInfo( String p_url, String p_md5 )
    {
        m_url = p_url;
        m_md5 = p_md5;
    }
}
