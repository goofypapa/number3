package com.dadpat.goofypapa.dadpat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class CardListActivity extends AppCompatActivity {

    private ImageView m_img_back;
    private ImageView m_img_listStyle;

    private boolean m_doubleColumn;

    private Intent m_toInfo;

    private TableLayout m_cardBatchList;

    private Db_Animals m_db_animals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        Log.d("DEBUG", "StartCardList");

        m_doubleColumn = true;

        m_db_animals = new Db_Animals(getApplicationContext());

        m_img_back = findViewById(R.id.img_back);
        m_img_listStyle = findViewById(R.id.img_listStyle);

        m_toInfo = new Intent(this, CardInfoActivity.class);

        m_cardBatchList = findViewById(R.id.tl_cardBatchList);

        m_img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        m_img_listStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_doubleColumn = !m_doubleColumn;
                refreshListStyle();
            }
        });


        refreshListStyle();

    }

    private void refreshListStyle()
    {
        m_cardBatchList.removeAllViews();

        if( m_doubleColumn )
        {
            m_img_listStyle.setImageResource(R.mipmap.list_icon_smallpic);

            ArrayList<String> t_activationBatchList = m_db_animals.getAnimalGroupList();

            ArrayList<BatchInfo> t_batchList = m_db_animals.getBatchList();

            Point t_windowSize = new Point();
            getWindowManager().getDefaultDisplay().getSize(t_windowSize);

            int t_imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getApplicationContext().getResources().getDisplayMetrics());
            int t_spaceHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getApplicationContext().getResources().getDisplayMetrics());
            int t_lockSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, getApplicationContext().getResources().getDisplayMetrics());
            int t_lockMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 114, getApplicationContext().getResources().getDisplayMetrics());

            TableRow t_tableRow = null;
            RelativeLayout t_lineBox = null;

            for (int i = 0; i < t_batchList.size(); ++i) {

                BatchInfo t_batch = t_batchList.get(i);
                ImageInfo t_img = m_db_animals.getImage(t_batch.m_cover);
                boolean t_localImg = true;
                boolean t_isActive = false;

                for( int k = 0; k < t_activationBatchList.size(); ++k)
                {
                    if( t_batch.m_id.equals(t_activationBatchList.get(k)) )
                    {
                        t_isActive = true;
                        break;
                    }
                }

                String t_fileName = "";

                if( t_img == null )
                {
                    t_localImg = false;
                }else{

                    String[] t_list = t_img.m_url.split("/");
                    t_fileName = t_list[t_list.length - 1];

                    File t_file = new File(Control.instance().m_imagePath + t_fileName );
                    if( !t_file.exists() )
                    {
                        t_localImg = false;
                    }
                }

                if( i % 2 == 0 ) {
                    t_tableRow = new TableRow(m_cardBatchList.getContext());
                    t_lineBox = new RelativeLayout(t_tableRow.getContext());
                }

                CardRelativeLayout t_relativeLayout = new CardRelativeLayout(t_lineBox.getContext(), t_batch.m_id );

                t_relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardRelativeLayout t_cardRelativeLayout = (CardRelativeLayout)v;

                        CardInfoActivity.s_cradGroupId = t_cardRelativeLayout.cradId;

                        startActivity(m_toInfo);
                    }
                });

                t_relativeLayout.setMinimumWidth(t_windowSize.x / 2);

                ImageView t_imageView = new ImageView(t_relativeLayout.getContext());

                if( t_localImg )
                {
                    t_imageView.setImageURI(Uri.fromFile(new File(Control.instance().m_imagePath + t_fileName)));
                }else{
                    t_imageView.setImageResource(R.mipmap.card_group_cover_loding);

                    new GetBitmap(Control.sm_serviceHost + "/" + t_batch.m_cover, t_imageView, new GetBitmapListen() {
                        @Override
                        public void callBack(Object p_obj, Bitmap p_bitmap) {
                            ((ImageView)p_obj).setImageBitmap(p_bitmap);
                        }
                    }).get();
                }

                ImageView t_lock = null;
                if(!t_isActive) {
                    t_lock = new ImageView(t_relativeLayout.getContext());
                    t_lock.setImageResource(R.mipmap.list_icon_lock);
                }


                TextView t_textView = new TextView(t_relativeLayout.getContext());

                t_textView.setText(t_batch.m_batchName);
                t_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.goofyPapaWhite));
                t_textView.setTextSize(18);
                t_textView.setGravity(Gravity.CENTER);

                t_relativeLayout.addView(t_imageView);

                t_relativeLayout.addView(t_textView);

                if(t_lock != null) {
                    t_relativeLayout.addView(t_lock);
                }

                RelativeLayout.LayoutParams textViewParam = (RelativeLayout.LayoutParams) t_textView.getLayoutParams();
                textViewParam.topMargin = t_imgSize - t_spaceHeight / 2;
                textViewParam.width = t_imgSize;
                textViewParam.bottomMargin = t_spaceHeight;
                t_textView.setLayoutParams(textViewParam);

                RelativeLayout.LayoutParams imageViewParam = (RelativeLayout.LayoutParams) t_imageView.getLayoutParams();
                imageViewParam.width = t_imgSize;
                imageViewParam.height = t_imgSize;
                t_imageView.setLayoutParams(imageViewParam);


                if(t_lock != null) {
                    RelativeLayout.LayoutParams lockViewParam = (RelativeLayout.LayoutParams) t_lock.getLayoutParams();
                    lockViewParam.width = t_lockSize;
                    lockViewParam.height = t_lockSize;
                    lockViewParam.leftMargin = t_lockMargin;
                    lockViewParam.topMargin = t_lockMargin;
                    t_lock.setLayoutParams(lockViewParam);
                }

                t_lineBox.addView(t_relativeLayout);

                RelativeLayout.LayoutParams relativeViewParam  = (RelativeLayout.LayoutParams) t_relativeLayout.getLayoutParams();
                relativeViewParam.width = t_imgSize;
                relativeViewParam.leftMargin = t_imgSize * (i % 2) + ( t_windowSize.x - t_imgSize * 2 ) / 3 * (i % 2 + 1);
                t_relativeLayout.setLayoutParams(relativeViewParam);


                if( i % 2 == 0 )
                {
                    m_cardBatchList.addView(t_tableRow);
                    t_tableRow.addView(t_lineBox);
                }
            }
        }else{
            m_img_listStyle.setImageResource(R.mipmap.list_icon_bigpic);

            ArrayList<String> t_activationBatchList = m_db_animals.getAnimalGroupList();

            ArrayList<BatchInfo> t_batchList = m_db_animals.getBatchList();

            Point t_windowSize = new Point();
            getWindowManager().getDefaultDisplay().getSize(t_windowSize);

            int t_imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 340, getApplicationContext().getResources().getDisplayMetrics());
            int t_spaceHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getApplicationContext().getResources().getDisplayMetrics());
            int t_lockSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58, getApplicationContext().getResources().getDisplayMetrics());
            int t_lockMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 245, getApplicationContext().getResources().getDisplayMetrics());

            TableRow t_tableRow = null;

            for (int i = 0; i < t_batchList.size(); ++i) {

                BatchInfo t_batch = t_batchList.get(i);
                ImageInfo t_img = m_db_animals.getImage(t_batch.m_cover);
                boolean t_localImg = true;
                boolean t_isActive = false;

                for( int k = 0; k < t_activationBatchList.size(); ++k)
                {
                    if( t_batch.m_id.equals(t_activationBatchList.get(k)) )
                    {
                        t_isActive = true;
                        break;
                    }
                }

                String t_fileName = "";

                if( t_img == null )
                {
                    t_localImg = false;
                }else{

                    String[] t_list = t_img.m_url.split("/");
                    t_fileName = t_list[t_list.length - 1];

                    File t_file = new File(Control.instance().m_imagePath + t_fileName );
                    if( !t_file.exists() )
                    {
                        t_localImg = false;
                    }
                }

                t_tableRow = new TableRow(m_cardBatchList.getContext());

                RelativeLayout t_lineBox = new RelativeLayout(t_tableRow.getContext());

                CardRelativeLayout t_relativeLayout = new CardRelativeLayout( t_lineBox.getContext(), t_batch.m_id );

                t_relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardRelativeLayout t_cardRelativeLayout = (CardRelativeLayout)v;

                        CardInfoActivity.s_cradGroupId = t_cardRelativeLayout.cradId;

                        startActivity(m_toInfo);
                    }
                });

                t_relativeLayout.setMinimumWidth(t_windowSize.x / 2);

                ImageView t_imageView = new ImageView(t_relativeLayout.getContext());

                if( t_localImg )
                {
                    t_imageView.setImageURI(Uri.fromFile(new File(Control.instance().m_imagePath + t_fileName)));
                }else{
                    t_imageView.setImageResource(R.mipmap.card_group_cover_loding);

                    new GetBitmap(Control.sm_serviceHost + "/" + t_batch.m_cover, t_imageView, new GetBitmapListen() {
                        @Override
                        public void callBack(Object p_obj, Bitmap p_bitmap) {
                            ((ImageView)p_obj).setImageBitmap(p_bitmap);
                        }
                    }).get();
                }

                ImageView t_lock = null;
                if(!t_isActive) {
                    t_lock = new ImageView(t_relativeLayout.getContext());
                    t_lock.setImageResource(R.mipmap.list_icon_lock);
                }


                TextView t_textView = new TextView(t_relativeLayout.getContext());

                t_textView.setText(t_batch.m_batchName);
                t_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.goofyPapaWhite));
                t_textView.setTextSize(18);
                t_textView.setGravity(Gravity.CENTER);

                t_relativeLayout.addView(t_imageView);

                t_relativeLayout.addView(t_textView);

                if(t_lock != null) {
                    t_relativeLayout.addView(t_lock);
                }

                RelativeLayout.LayoutParams textViewParam = (RelativeLayout.LayoutParams) t_textView.getLayoutParams();
                textViewParam.topMargin = t_imgSize - t_spaceHeight / 2;
                textViewParam.width = t_imgSize;
                textViewParam.bottomMargin = t_spaceHeight;
                t_textView.setLayoutParams(textViewParam);

                RelativeLayout.LayoutParams imageViewParam = (RelativeLayout.LayoutParams) t_imageView.getLayoutParams();
                imageViewParam.width = t_imgSize;
                imageViewParam.height = t_imgSize;
                t_imageView.setLayoutParams(imageViewParam);


                if(t_lock != null) {
                    RelativeLayout.LayoutParams lockViewParam = (RelativeLayout.LayoutParams) t_lock.getLayoutParams();
                    lockViewParam.width = t_lockSize;
                    lockViewParam.height = t_lockSize;
                    lockViewParam.leftMargin = t_lockMargin;
                    lockViewParam.topMargin = t_lockMargin;
                    t_lock.setLayoutParams(lockViewParam);
                }

                t_lineBox.addView(t_relativeLayout);

                RelativeLayout.LayoutParams relativeViewParam  = (RelativeLayout.LayoutParams) t_relativeLayout.getLayoutParams();
                relativeViewParam.width = t_imgSize;
                relativeViewParam.leftMargin = ( t_windowSize.x - t_imgSize ) / 2 ;
                t_relativeLayout.setLayoutParams(relativeViewParam);

                t_tableRow.addView(t_lineBox);
                m_cardBatchList.addView(t_tableRow);
            }
        }
    }

}
