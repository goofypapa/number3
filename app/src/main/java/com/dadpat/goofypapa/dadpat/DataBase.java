package com.dadpat.goofypapa.dadpat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Lenovo on 2018/3/26.
 */

public class DataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "animal.db";//数据库名字
    private static final int DATABASE_VERSION = 1;//数据库版本号
    private static final String TABLE_ANIMAL = "animal";
    private static final String CREATE_TABLE_ANIMAL = "create table if not exists " + TABLE_ANIMAL + " ("
            + "cardId integer primary key, "
            + "serviceId varchar, "
            + "coverImage varchar, "
            + "lineDrawing varchar, "
            + "activation integer, "
            + "groupId varchar)";

    private static final String TABLE_BATCHES = "batches";
    private static final String CREATE_TABLE_BATCHES = "create table if not exists " + TABLE_BATCHES + " ("
            + "id varchar primary key, "
            + "name varchar, "
            + "mExplain varchar, "
            + "cover varchar, "
            + "activation integer, "
            + "type varchar)";

    private static final String TABLE_AUDIOS = "audios";
    private static final String CREATE_TABLE_AUDIOS = "create table if not exists " + TABLE_AUDIOS + " ("
            + "md5 varchar primary key, "
            + "cardId integer, "
            + "path varchar, "
            + "audioType integer)";

    private static final String TABLE_IMAGES = "images";
    private static final String CREATE_TABLE_IMAGES = "create table if not exists " + TABLE_IMAGES + " ("
            + "md5 varchar primary key, "
            + "path varchar)";


    private SQLiteDatabase m_db;

    private static boolean sm_isInitTables = false;


    public DataBase(Context p_context)
    {
        this(p_context, DATABASE_NAME, null, DATABASE_VERSION);
        m_db = getWritableDatabase();
        onCreate( m_db );
    }

    private DataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);//调用到SQLiteOpenHelper中
    }

    public void insert( DBCardInfo p_cardInfo)
    {
        ContentValues values = new ContentValues();
        values.put("cardId", p_cardInfo.m_cardNumber);
        values.put("serviceId", p_cardInfo.m_serviceId);
        values.put("coverImage", p_cardInfo.m_coverImage);
        values.put("lineDrawing", p_cardInfo.m_lineDrawing);
        values.put("groupId", p_cardInfo.m_group);
        values.put("activation", p_cardInfo.m_activation);
        m_db.insert(TABLE_ANIMAL, "cardId", values);
    }

    public void insert( DBBatchInfo p_DB_batchInfo)
    {
        ContentValues t_batch = new ContentValues();
        t_batch.put("id", p_DB_batchInfo.m_id);
        t_batch.put("name", p_DB_batchInfo.m_batchName);
        t_batch.put("mExplain", p_DB_batchInfo.m_explain);
        t_batch.put("cover", p_DB_batchInfo.m_cover);
        t_batch.put("activation", p_DB_batchInfo.m_activation);
        m_db.insert(TABLE_BATCHES, null, t_batch);
    }

    public void insert( DBAudioInfo p_audioInfo)
    {
        ContentValues t_audios = new ContentValues();
        t_audios.put("md5", p_audioInfo.md5);
        t_audios.put("cardId", p_audioInfo.cardId);
        t_audios.put("path", p_audioInfo.path);
        t_audios.put("audioType", p_audioInfo.audioType);
        m_db.insert(TABLE_AUDIOS, null, t_audios);
    }

    public void insert( DBImageInfo p_DB_imageInfo)
    {
        ContentValues t_image = new ContentValues();
        t_image.put("md5", p_DB_imageInfo.m_md5);
        t_image.put("path", p_DB_imageInfo.m_path);
        m_db.insert(TABLE_IMAGES, null, t_image);
    }

    public void update( DBCardInfo p_cardInfo)
    {
        ContentValues values = new ContentValues();
        values.put("serviceId", p_cardInfo.m_serviceId);
        values.put("groupId", p_cardInfo.m_group);
        values.put("coverImage", p_cardInfo.m_coverImage);
        values.put("lineDrawing", p_cardInfo.m_lineDrawing);
        values.put("activation", p_cardInfo.m_activation ? 1 : 0);
        m_db.update(TABLE_ANIMAL, values, "cardId=?", new String[]{ "" + p_cardInfo.m_cardNumber } );
    }

    public void update( DBBatchInfo p_DB_batchInfo)
    {
        ContentValues t_batch = new ContentValues();
        t_batch.put("name", p_DB_batchInfo.m_batchName);
        t_batch.put("mExplain", p_DB_batchInfo.m_explain);
        t_batch.put("cover", p_DB_batchInfo.m_cover);
        t_batch.put("activation", p_DB_batchInfo.m_activation ? 1 : 0);
        m_db.update(TABLE_BATCHES, t_batch, "id=?", new String[]{ "" + p_DB_batchInfo.m_id } );
    }

    public void update( DBAudioInfo p_audioInfo)
    {
        ContentValues t_audios = new ContentValues();
        t_audios.put("cardId", p_audioInfo.cardId);
        t_audios.put("path", p_audioInfo.path);
        t_audios.put("audioType", p_audioInfo.audioType);

        m_db.update(TABLE_AUDIOS, t_audios, "md5=?", new String[]{ "" + p_audioInfo.md5 } );
    }


    public void update( DBImageInfo p_image )
    {
        ContentValues t_image = new ContentValues();
        t_image.put("path", p_image.m_path);

        m_db.update(TABLE_IMAGES, t_image, "md5=?", new String[]{ "" + p_image.m_md5 } );
    }

    public void delete(DBCardInfo p_cardInfo)
    {
        m_db.delete(TABLE_ANIMAL, "cardId=?", new String[]{ "" + p_cardInfo.m_cardNumber });
    }

    public void delete( DBAudioInfo p_audioInfo)
    {
        m_db.delete(TABLE_AUDIOS, "md5=?", new String[]{ "" + p_audioInfo.md5 });
    }

    public void delete( DBBatchInfo p_batch )
    {
        m_db.delete(TABLE_BATCHES, "id=?", new String[]{ p_batch.m_id });
    }

    public void delete( DBImageInfo p_image )
    {
        m_db.delete(TABLE_IMAGES, "md5=?", new String[]{ p_image.m_md5 });
    }


    public ArrayList<DBCardInfo> getCardList()
    {
        ArrayList<DBCardInfo> t_result = new ArrayList<>();

        Cursor cursor = m_db.query(TABLE_ANIMAL, null, null, null, null, null, "cardId");
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {

                //DBCardInfo t_animal = new DBCardInfo(cursor.getInt(0), )

                int t_cardId = cursor.getInt(0);
                String t_serviceId = cursor.getString(1);
                String t_coverImage = cursor.getString(2);
                String t_lineDrawing = cursor.getString(3);

                boolean t_activation = cursor.getInt(4) == 1;
                String t_groupId = cursor.getString(5);


                ArrayList<DBAudioInfo> t_DBAudioInfos = new ArrayList<>();

                Cursor t_audios_cursor = m_db.query(TABLE_AUDIOS, null, "cardId=?", new String[]{ "" + cursor.getInt(0)  }, null, null, null);

                if( t_audios_cursor.moveToFirst() )
                {
                    while(!t_audios_cursor.isAfterLast())
                    {
                        DBAudioInfo t_DBAudioInfo = new DBAudioInfo( t_audios_cursor.getString(0), t_audios_cursor.getInt(1), t_audios_cursor.getString(2), t_audios_cursor.getInt(3) );

                        t_DBAudioInfos.add(t_DBAudioInfo);

                        t_audios_cursor.moveToNext();
                    }
                }

                t_audios_cursor.close();

                t_result.add(new DBCardInfo( t_cardId, t_serviceId, t_groupId, t_coverImage, t_lineDrawing, t_activation, t_DBAudioInfos));

                cursor.moveToNext();
            }
        }
        cursor.close();

        return t_result;
    }

    public ArrayList<DBCardInfo> getCardListByBatche( String p_batcheId  )
    {
        ArrayList<DBCardInfo> t_result = new ArrayList<>();

        Cursor cursor = m_db.query(TABLE_ANIMAL, null, "groupId=?", new String[]{ p_batcheId }, null, null, "cardId");
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {

                //DBCardInfo t_animal = new DBCardInfo(cursor.getInt(0), )

                int t_cardId = cursor.getInt(0);
                String t_serviceId = cursor.getString(1);
                String t_coverImage = cursor.getString(2);
                String t_lineDrawing = cursor.getString(3);

                boolean t_activation = cursor.getInt(4) == 1;
                String t_groupId = cursor.getString(5);


                ArrayList<DBAudioInfo> t_DBAudioInfos = new ArrayList<>();

                Cursor t_audios_cursor = m_db.query(TABLE_AUDIOS, null, "cardId=?", new String[]{ "" + cursor.getInt(0)  }, null, null, null);

                if( t_audios_cursor.moveToFirst() )
                {
                    while(!t_audios_cursor.isAfterLast())
                    {
                        DBAudioInfo t_DBAudioInfo = new DBAudioInfo( t_audios_cursor.getString(0), t_audios_cursor.getInt(1), t_audios_cursor.getString(2), t_audios_cursor.getInt(3) );

                        t_DBAudioInfos.add(t_DBAudioInfo);

                        t_audios_cursor.moveToNext();
                    }
                }

                t_audios_cursor.close();

                t_result.add(new DBCardInfo( t_cardId, t_serviceId, t_groupId, t_coverImage, t_lineDrawing, t_activation, t_DBAudioInfos));

                cursor.moveToNext();
            }
        }
        cursor.close();

        return t_result;
    }

    public ArrayList<DBCardInfo> getActivationCardList()
    {
        ArrayList<DBCardInfo> t_result = new ArrayList<>();

        Cursor cursor = m_db.query(TABLE_ANIMAL, null, "activation=1", null , null, null, "cardId");
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {

                //DBCardInfo t_animal = new DBCardInfo(cursor.getInt(0), )

                int t_cardId = cursor.getInt(0);
                String t_serviceId = cursor.getString(1);
                String t_coverImage = cursor.getString(2);
                String t_lineDrawing = cursor.getString(3);

                boolean t_activation = cursor.getInt(4) == 1;
                String t_groupId = cursor.getString(5);


                ArrayList<DBAudioInfo> t_DBAudioInfos = new ArrayList<>();

                Cursor t_audios_cursor = m_db.query(TABLE_AUDIOS, null, "cardId=?", new String[]{ "" + cursor.getInt(0)  }, null, null, null);

                if( t_audios_cursor.moveToFirst() )
                {
                    while(!t_audios_cursor.isAfterLast())
                    {
                        DBAudioInfo t_DBAudioInfo = new DBAudioInfo( t_audios_cursor.getString(0), t_audios_cursor.getInt(1), t_audios_cursor.getString(2), t_audios_cursor.getInt(3) );

                        t_DBAudioInfos.add(t_DBAudioInfo);

                        t_audios_cursor.moveToNext();
                    }
                }

                t_audios_cursor.close();

                t_result.add(new DBCardInfo( t_cardId, t_serviceId, t_groupId, t_coverImage, t_lineDrawing, t_activation, t_DBAudioInfos));

                cursor.moveToNext();
            }
        }
        cursor.close();

        return t_result;
    }

    public DBCardInfo getCardById(int p_cardId )
    {
        Cursor cursor = m_db.query(TABLE_ANIMAL, null, "cardId = " + p_cardId, null, null, null, "cardId");

        return _getCardById( cursor );
    }

    public DBCardInfo getCardById( String p_cardId )
    {

        Cursor cursor = m_db.query(TABLE_ANIMAL, null, "serviceId = \"" + p_cardId + "\"", null, null, null, "cardId");

        return _getCardById( cursor );
    }

    private DBCardInfo _getCardById( Cursor p_cursor )
    {
        DBCardInfo t_result = null;

        if (p_cursor.moveToFirst() && !p_cursor.isAfterLast() ) {

            int t_cardId = p_cursor.getInt(0);
            String t_serviceId = p_cursor.getString(1);
            String t_coverImage = p_cursor.getString(2);
            String t_lineDrawing = p_cursor.getString(3);
            boolean t_activation = p_cursor.getInt(4) == 1;
            String t_groupId = p_cursor.getString(5);

            ArrayList<DBAudioInfo> t_audioList = new ArrayList<DBAudioInfo>();

            Cursor t_audios_cursor = m_db.query(TABLE_AUDIOS, null, "cardId = " + t_cardId, null, null, null, null);

            if( t_audios_cursor.moveToFirst() )
            {
                while(!t_audios_cursor.isAfterLast())
                {
                    t_audioList.add(new DBAudioInfo( t_audios_cursor.getString(0), t_audios_cursor.getInt(1), t_audios_cursor.getString(2), t_audios_cursor.getInt(3) ));
                    t_audios_cursor.moveToNext();
                }
            }

            t_audios_cursor.close();

            t_result = new DBCardInfo( t_cardId, t_serviceId, t_groupId, t_coverImage, t_lineDrawing, t_activation, t_audioList );
        }
        p_cursor.close();

        return t_result;
    }

    public DBBatchInfo getBatchInfo(String p_batchId )
    {
        DBBatchInfo t_result = null;

        String t_columns[] = { "id", "name", "mExplain", "cover", "activation", "type"};

        Cursor cursor = m_db.query(TABLE_BATCHES, t_columns, "id=?", new String[]{p_batchId}, null, null, null);

        if(cursor.moveToFirst() && !cursor.isAfterLast())
        {
            t_result = new DBBatchInfo( cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4) == 1 , cursor.getString(5));
        }

        cursor.close();

        return t_result;
    }

    public DBImageInfo getImage(String p_md5 )
    {
        DBImageInfo t_result = null;

        String t_columns[] = { "md5", "path" };

        Cursor cursor = m_db.query(TABLE_IMAGES, t_columns, "md5=?", new String[]{p_md5}, null, null, null);

        if(cursor.moveToFirst() && !cursor.isAfterLast())
        {
            t_result = new DBImageInfo( cursor.getString(0), cursor.getString(1)  );
        }

        cursor.close();

        return t_result;
    }

    public DBAudioInfo getAudio( String p_md5 )
    {
        DBAudioInfo t_result = null;

        String t_columns[] = { "md5", "cardId", "path", "audioType" };

        Cursor cursor = m_db.query(TABLE_AUDIOS, t_columns, "md5=?", new String[]{p_md5}, null, null, null);

        if(cursor.moveToFirst() && !cursor.isAfterLast())
        {
            t_result = new DBAudioInfo( cursor.getString(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3) );
        }

        cursor.close();

        return t_result;

    }

    public ArrayList<DBBatchInfo> getBatchList()
    {
        ArrayList<DBBatchInfo> t_results = new ArrayList<>();

        String t_columns[] = { "id", "name", "mExplain", "cover", "activation", "type"};

        Cursor cursor = m_db.query(TABLE_BATCHES, t_columns, null, null, null, null, null );

        if( cursor.moveToFirst() )
        {
            while(!cursor.isAfterLast())
            {
                t_results.add(new DBBatchInfo( cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4) == 1 , cursor.getString(5) ));
                cursor.moveToNext();
            }
        }

        cursor.close();

        return t_results;
    }

    public void clearDataBase()
    {
        m_db.execSQL("drop table " + TABLE_ANIMAL);
        m_db.execSQL("drop table " + TABLE_AUDIOS);
        m_db.execSQL("drop table " + TABLE_BATCHES);
        m_db.execSQL("drop table " + TABLE_IMAGES);
        m_db.execSQL(CREATE_TABLE_ANIMAL);
        m_db.execSQL(CREATE_TABLE_AUDIOS);
        m_db.execSQL(CREATE_TABLE_BATCHES);
        m_db.execSQL(CREATE_TABLE_IMAGES);
//        m_db.execSQL("delete from " + TABLE_ANIMAL);
//        m_db.execSQL("delete from " + TABLE_AUDIOS);
//        m_db.execSQL("delete from " + TABLE_BATCHES);
//        m_db.execSQL("delete from " + TABLE_IMAGES);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        if( sm_isInitTables )
        {
            return;
        }

        db.execSQL(CREATE_TABLE_ANIMAL);
        db.execSQL(CREATE_TABLE_AUDIOS);
        db.execSQL(CREATE_TABLE_BATCHES);
        db.execSQL(CREATE_TABLE_IMAGES);
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
