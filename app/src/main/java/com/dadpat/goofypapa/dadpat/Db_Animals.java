package com.dadpat.goofypapa.dadpat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

/**
 * Created by Lenovo on 2018/3/26.
 */

public class Db_Animals extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "animal.db";//数据库名字
    private static final int DATABASE_VERSION = 1;//数据库版本号
    private static final String TABLE_ANIMAL = "animal";
    private static final String CREATE_TABLE_ANIMAL = "create table if not exists " + TABLE_ANIMAL + " ("
            + "cardId integer primary key, "
            + "serviceId varchar,"
            + "groupId integer,"
            + "audios varchar)";


    private SQLiteDatabase m_db;

    private LogListen m_logListen;

    private static boolean sm_isInitTables = false;



    Handler m_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            m_logListen.onLog((String)msg.obj);
        }
    };


    public Db_Animals( Context p_context, LogListen p_logListen )
    {
        this(p_context, DATABASE_NAME, null, DATABASE_VERSION);
        m_db = getWritableDatabase();
        m_logListen = p_logListen;

        onCreate( m_db );
    }

    private Db_Animals(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);//调用到SQLiteOpenHelper中
    }

    public void insert( Animal p_animal )
    {
        String t_audios = "";
        for(int i = 0; i < p_animal.m_musicPaths.size(); ++i)
        {
            t_audios += i == 0 ? p_animal.m_musicPaths.get(i) : "," + p_animal.m_musicPaths.get(i);
        }

        ContentValues values = new ContentValues();
        values.put("cardId", p_animal.m_cardNumber);
        values.put("serviceId", p_animal.m_serviceId);
        values.put("groupId", p_animal.m_group);
        values.put("audios", t_audios);
        m_db.insert(TABLE_ANIMAL, "cardId", values);
    }

    public void getListInfo()
    {
        Cursor cursor = m_db.query(TABLE_ANIMAL, null, null, null, null, null, "cardId");
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {

                Message t_message = new Message();
                t_message.obj = "-----" + cursor.getInt(0) + ", " + cursor.getString(1) + ", " + cursor.getInt(2);
                m_handler.sendMessage(t_message);

                cursor.moveToNext();
            }
        }
        cursor.close();
    }

    public Animal getAnimalByCardId( int p_cardId )
    {
        Animal t_result = null;

        Cursor cursor = m_db.query(TABLE_ANIMAL, null, "cardId = " + p_cardId, null, null, null, "cardId");

        if (cursor.moveToFirst() && !cursor.isAfterLast() ) {

            int t_cardId = cursor.getInt(0);
            String t_serviceId = cursor.getString(1);
            int t_groupId = cursor.getInt(2);
            String t_audios = cursor.getString(3);

            ArrayList<String> t_audioList = new ArrayList<String>();

            String[] t_audioSplistList = t_audios.split(",");

            for( int i = 0; i < t_audioSplistList.length; ++i ){
                t_audioList.add(t_audioSplistList[i]);
            }

            t_result = new Animal( t_cardId, t_serviceId, t_groupId, t_audioList );
        }
        cursor.close();

        return t_result;
    }

    public ArrayList<Integer> getAnimalGroupList()
    {
        ArrayList<Integer> t_results = new ArrayList<Integer>();

        String t_columns[] = { "groupId"};

        Cursor cursor = m_db.query(TABLE_ANIMAL, t_columns, null, null, "groupId",null, null );

        if(cursor.moveToFirst())
        {
            while( !cursor.isAfterLast() )
            {
                int t_groupId = cursor.getInt(0);
                t_results.add(t_groupId);
                cursor.moveToNext();
            }
        }

        cursor.close();

        return t_results;
    }

    public void clearAnimals()
    {
//        m_db.execSQL("drop table " + TABLE_ANIMAL);
//        m_db.execSQL(CREATE_TABLE_ANIMAL);
        m_db.execSQL("delete from " + TABLE_ANIMAL);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        if( sm_isInitTables )
        {
            return;
        }
        db.execSQL(CREATE_TABLE_ANIMAL);
        Message t_message = new Message();
        t_message.obj = "init tables";
        m_handler.sendMessage(t_message);
        sm_isInitTables = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    @Override
    protected void finalize()
    {
        m_db.close();
    }

}
