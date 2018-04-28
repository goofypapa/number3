package com.dadpat.goofypapa.dadpat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class CardInfoActivity extends AppCompatActivity {

    private ImageView m_imgBack;

    private Db_Animals m_db_animals;

    private Intent m_toWebView;

    private TableLayout m_tlCardList;

    private ImageView m_ivCardGroupCover;
    private TextView m_tvGroupInfo;

    public static String s_cradGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_card_info);


        m_imgBack  = findViewById(R.id.img_back);

        m_tlCardList = findViewById(R.id.tl_cardList);

        m_tvGroupInfo = findViewById(R.id.tv_groupInfo);

        m_tlCardList.removeAllViews();

        m_ivCardGroupCover = findViewById(R.id.iv_groupCover);

        m_db_animals = new Db_Animals(getApplicationContext());

        ArrayList<Animal> t_animalList = m_db_animals.getAnimalsImage( s_cradGroupId );

        BatchInfo t_batch = m_db_animals.getBatchInfo( s_cradGroupId );

        if( t_batch != null )
        {
            String[] t_list = t_batch.m_cover.split("/");
            String t_fileName = t_list[t_list.length - 1];

            File t_file = new File(Control.instance().m_imagePath + t_fileName );

            if( t_file.exists() )
            {
                m_ivCardGroupCover.setImageURI(Uri.fromFile(t_file));
            }

            m_tvGroupInfo.setText(t_batch.m_explain);
        }

        TableRow t_tableRow = null;

        Point t_windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(t_windowSize);

        int t_imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 67, getApplicationContext().getResources().getDisplayMetrics());
        int t_marginSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getApplicationContext().getResources().getDisplayMetrics());

        int t_areaSize = (t_windowSize.x - t_marginSize * 2) / 4;
        try {
            for (int i = 0; i < t_animalList.size(); ++i) {
                Animal t_animal = t_animalList.get(i);


                boolean t_localImg = true;

                String[] t_list = t_animal.m_coverImage.split("/");
                String t_fileName = t_list[t_list.length - 1];

                File t_file = new File(Control.instance().m_imagePath + t_fileName);
                if (!t_file.exists()) {
                    t_localImg = false;
                }

                if (i % 4 == 0) {
                    t_tableRow = new TableRow(m_tlCardList.getContext());
                }

                CardRelativeLayout t_cardRelativeLayout = new CardRelativeLayout(t_tableRow.getContext(), t_animal.m_serviceId);

                t_cardRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardRelativeLayout t_cardRelativeLayout = (CardRelativeLayout)v;
                        WebViewActivity.s_animalId = t_cardRelativeLayout.cradId;
                        startActivity(m_toWebView);

                    }
                });

                t_cardRelativeLayout.setMinimumWidth( t_areaSize );

                ImageView t_imageView = new ImageView(t_cardRelativeLayout.getContext());


                if (t_localImg) {
                    t_imageView.setImageURI(Uri.fromFile(t_file));
                } else {
                    t_imageView.setImageResource(R.mipmap.card_group_cover_loding);
                    new GetBitmap(Control.sm_serviceHost + "/" + t_animal.m_coverImage, t_imageView, new GetBitmapListen() {
                        @Override
                        public void callBack(Object p_obj, Bitmap p_bitmap) {
                            ((ImageView) p_obj).setImageBitmap(p_bitmap);
                        }
                    }).get();
                }

                t_cardRelativeLayout.addView(t_imageView);

                t_tableRow.addView(t_cardRelativeLayout);

                RelativeLayout.LayoutParams  t_lp = (RelativeLayout.LayoutParams)t_imageView.getLayoutParams();
                t_lp.width = t_imgSize;
                t_lp.height = t_imgSize;

                t_lp.leftMargin = ( t_areaSize - t_imgSize ) / 2;

                t_imageView.setLayoutParams(t_lp);



                if (i % 4 == 0 ) {
                    m_tlCardList.addView(t_tableRow);
                }

            }
        }catch (Exception e)
        {
            Log.e("ERROR", e.toString());
        }


        m_toWebView = new Intent(this, WebViewActivity.class);


        m_imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
