package com.dadpat.goofypapa.dadpat;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class UIUniversalCardActivity extends AppCompatActivity {

    static String s_cardId;
    private ImageView m_imgBack;
    private ImageView m_imgSimpleLineDrawing;

    private DataBase m_dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universal_card);

        m_dataBase = new DataBase( getApplicationContext() );

        m_imgBack  = findViewById(R.id.img_back);
        m_imgSimpleLineDrawing = findViewById( R.id.imgSimpleLineDrawing );

        m_imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DBCardInfo t_cardInfo = m_dataBase.getCardById( s_cardId );

        if( t_cardInfo != null )
        {

            DBImageInfo t_image = m_dataBase.getImage( t_cardInfo.m_lineDrawing );

            if( t_image != null ) {
                File t_file = new File( t_image.m_path );

                if (t_file.exists()) {
                    m_imgSimpleLineDrawing.setImageURI(Uri.fromFile(t_file));
                }
            }
        }

    }
}
