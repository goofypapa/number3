package com.dadpat.goofypapa.dadpat;

public class AnimalAudio {
    public int id;
    public int cardId;
    public String dir;
    public String url;
    public String md5;
    public int  audioType;

    public AnimalAudio()
    {
        id = 0;
        cardId = 0;
        dir = "";
        url = "";
        md5 = "";
        audioType = 0;
    }

    public AnimalAudio( int p_cardId, String p_dir, String p_url, String p_md5, int p_audioType )
    {
        id = 0;
        cardId = p_cardId;
        dir = p_dir;
        url = p_url;
        md5 = p_md5;
        audioType = p_audioType;
    }

}
