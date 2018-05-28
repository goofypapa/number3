package com.dadpat.goofypapa.dadpat;

import java.util.ArrayList;

public class DBCardInfo {
    int m_cardNumber;
    String m_serviceId;
    String m_group;
    String m_coverImage;
    String m_lineDrawing;
    boolean m_activation;
    ArrayList<DBAudioInfo> m_musicPaths;

    public DBCardInfo()
    {
        m_cardNumber = -1;
        m_serviceId = "";
        m_group = "";
        m_coverImage = "";
        m_lineDrawing = "";
        m_musicPaths = null;
        m_activation = false;
    }

    public DBCardInfo(int p_cardNumber, String p_serviceId, String p_group, String p_coverImage, String p_lineDrawing )
    {
        m_cardNumber = p_cardNumber;
        m_serviceId = p_serviceId;
        m_group = p_group;
        m_coverImage = p_coverImage;
        m_lineDrawing = p_lineDrawing;
        m_musicPaths = null;
        m_activation = false;
    }

    public DBCardInfo(int p_cardNumber, String p_serviceId, String p_group, String p_coverImage, String p_lineDrawing, boolean p_activation, ArrayList<DBAudioInfo> p_musicPaths )
    {
        m_cardNumber = p_cardNumber;
        m_serviceId = p_serviceId;
        m_group = p_group;
        m_coverImage = p_coverImage;
        m_lineDrawing = p_lineDrawing;
        m_musicPaths = p_musicPaths;
        m_activation = p_activation;
    }
}
