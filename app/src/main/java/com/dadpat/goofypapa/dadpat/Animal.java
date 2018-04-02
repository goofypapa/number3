package com.dadpat.goofypapa.dadpat;

import java.util.ArrayList;
import java.util.List;

public class Animal {
    int m_cardNumber;
    String m_serviceId;
    int m_group;
    ArrayList<String> m_musicPaths;

    int m_downloadSize;

    public Animal()
    {
        m_cardNumber = -1;
        m_serviceId = "";
        m_group = -1;
        m_musicPaths = null;
        m_downloadSize = 0;
    }

    public Animal( int p_cardNumber, String p_serviceId, int p_group)
    {
        m_cardNumber = p_cardNumber;
        m_serviceId = p_serviceId;
        m_group = p_group;
        m_musicPaths = null;
        m_downloadSize = 0;
    }

    public Animal( int p_cardNumber, String p_serviceId, int p_group, ArrayList<String> p_musicPaths )
    {
        m_cardNumber = p_cardNumber;
        m_serviceId = p_serviceId;
        m_group = p_group;
        m_musicPaths = p_musicPaths;
        m_downloadSize = 0;
    }
}
