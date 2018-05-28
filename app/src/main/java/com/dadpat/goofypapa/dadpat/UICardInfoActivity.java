package com.dadpat.goofypapa.dadpat;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class UICardInfoActivity extends AppCompatActivity {

    private ImageView m_imgBack;

    private DataBase m_dataBase;

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

        m_dataBase = new DataBase(getApplicationContext());

        ArrayList<DBCardInfo> t_DB_cardList = m_dataBase.getCardListByBatche( s_cradGroupId );

        DBBatchInfo t_batch = m_dataBase.getBatchInfo( s_cradGroupId );

        if( t_batch != null )
        {

            DBImageInfo t_image = m_dataBase.getImage(t_batch.m_cover);

            if( t_image != null ) {
                File t_file = new File( t_image.m_path );

                if (t_file.exists()) {
                    m_ivCardGroupCover.setImageURI(Uri.fromFile(t_file));
                }
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
            for (int i = 0; i < t_DB_cardList.size(); ++i) {
                DBCardInfo t_DB_card = t_DB_cardList.get(i);


                boolean t_localImg = false;

                DBImageInfo t_img = m_dataBase.getImage( t_DB_card.m_activation ? t_DB_card.m_coverImage : t_DB_card.m_lineDrawing );

                File t_file = null;

                if( t_img != null ) {
                    t_file = new File(t_img.m_path);
                    if (t_file.exists()) {
                        t_localImg = true;
                    }
                }

                if (i % 4 == 0) {
                    t_tableRow = new TableRow(m_tlCardList.getContext());
                }

                CardRelativeLayout t_cardRelativeLayout = new CardRelativeLayout(t_tableRow.getContext(), t_DB_card.m_serviceId);

                t_cardRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardRelativeLayout t_cardRelativeLayout = (CardRelativeLayout)v;
                        UIWebViewActivity.s_animalId = t_cardRelativeLayout.cradId;
                        startActivity(m_toWebView);

                    }
                });

                t_cardRelativeLayout.setMinimumWidth( t_areaSize );

                ImageView t_imageView = new ImageView(t_cardRelativeLayout.getContext());


                if (t_localImg) {
                    t_imageView.setImageURI(Uri.fromFile(t_file));
                } else {
                    t_imageView.setImageResource(R.mipmap.card_group_cover_loding);
//                    new GetBitmap(Control.sm_serviceHost + "/" + t_DB_card.m_coverImage, t_imageView, new GetBitmapListen() {
//                        @Override
//                        public void callBack(Object p_obj, Bitmap p_bitmap) {
//                            ((ImageView) p_obj).setImageBitmap(p_bitmap);
//                        }
//                    }).get();
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


        m_toWebView = new Intent(this, UIWebViewActivity.class);


        m_imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
