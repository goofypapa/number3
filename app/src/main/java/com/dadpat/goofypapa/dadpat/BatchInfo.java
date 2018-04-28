package com.dadpat.goofypapa.dadpat;

import java.util.UUID;

public class BatchInfo {

    public String m_id;
    public String m_batchName;
    public String m_explain;
    public String m_cover;


    public BatchInfo()
    {
        m_id = null;
        m_batchName = "";
        m_explain = "";
        m_cover = "";
    }

    public BatchInfo( String p_id, String p_batchName, String p_explain, String p_cover )
    {
        m_id = p_id;
        m_batchName = p_batchName;
        m_explain = p_explain;
        m_cover = p_cover;
    }
}
