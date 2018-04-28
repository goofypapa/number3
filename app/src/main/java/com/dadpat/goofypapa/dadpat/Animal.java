package com.dadpat.goofypapa.dadpat;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Animal {
    int m_cardNumber;
    String m_serviceId;
    String m_group;
    String m_coverImage;
    ArrayList<AnimalAudio> m_musicPaths;

    public Animal()
    {
        m_cardNumber = -1;
        m_serviceId = "";
        m_group = "";
        m_coverImage = "";
        m_musicPaths = null;
    }

    public Animal( int p_cardNumber, String p_serviceId, String p_group, String p_coverImage )
    {
        m_cardNumber = p_cardNumber;
        m_serviceId = p_serviceId;
        m_group = p_group;
        m_coverImage = p_coverImage;
        m_musicPaths = null;
    }

    public Animal( int p_cardNumber, String p_serviceId, String p_group, String p_coverImage, ArrayList<AnimalAudio> p_musicPaths )
    {
        m_cardNumber = p_cardNumber;
        m_serviceId = p_serviceId;
        m_group = p_group;
        m_coverImage = p_coverImage;
        m_musicPaths = p_musicPaths;
    }
}
