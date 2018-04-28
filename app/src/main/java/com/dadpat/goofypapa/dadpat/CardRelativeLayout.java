package com.dadpat.goofypapa.dadpat;

import android.content.Context;
import android.widget.RelativeLayout;

public class CardRelativeLayout extends RelativeLayout {

    public String cradId;

    public CardRelativeLayout(Context context) {
        super(context);
        cradId = "";
    }

    public CardRelativeLayout(Context context, String p_cardId )
    {
        super(context);
        cradId = p_cardId;
    }


}
