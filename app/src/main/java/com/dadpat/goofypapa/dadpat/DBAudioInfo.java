package com.dadpat.goofypapa.dadpat;

public class DBAudioInfo {

    public String md5;
    public int cardId;
    public String path;
    public int  audioType;

    public DBAudioInfo()
    {
        cardId = 0;
        md5 = "";
        path = "";
        audioType = 0;
    }

    public DBAudioInfo(String p_md5, int p_cardId, String p_path, int p_audioType )
    {
        md5 = p_md5;
        cardId = p_cardId;
        path = p_path;
        audioType = p_audioType;
    }

}
