package com.dadpat.goofypapa.dadpat;

public class DBBatchInfo {

    public String m_id;
    public String m_batchName;
    public String m_explain;
    public String m_cover;
    public String m_group;
    public boolean m_activation;


    public DBBatchInfo()
    {
        m_id = null;
        m_batchName = "";
        m_explain = "";
        m_cover = "";
        m_group = "";
        m_activation = false;
    }

    public DBBatchInfo(String p_id, String p_batchName, String p_explain, String p_cover, boolean p_activation, String p_group )
    {
        m_id = p_id;
        m_batchName = p_batchName;
        m_explain = p_explain;
        m_cover = p_cover;
        m_activation = p_activation;
        m_group = p_group;
    }
}
