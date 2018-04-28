package com.dadpat.goofypapa.dadpat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Lenovo on 2018/3/26.
 */

public class Db_Animals extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "animal.db";//数据库名字
    private static final int DATABASE_VERSION = 1;//数据库版本号
    private static final String TABLE_ANIMAL = "animal";
    private static final String CREATE_TABLE_ANIMAL = "create table if not exists " + TABLE_ANIMAL + " ("
            + "cardId integer primary key, "
            + "serviceId varchar, "
            + "coverImage varchar, "
            + "groupId varchar)";

    private static final String TABLE_AUDIOS = "audios";
    private static final String CREATE_TABLE_AUDIOS = "create table if not exists " + TABLE_AUDIOS + " ("
            + "id integer primary key autoincrement, "
            + "cardId integer, "
            + "url varchar, "
            + "md5 varchar, "
            + "audioType integer)";

    private static final String TABLE_BATCHES = "batches";
    private static final String CREATE_TABLE_BATCHES = "create table if not exists " + TABLE_BATCHES + " ("
            + "id varchar primary key, "
            + "name varchar, "
            + "mExplain varchar, "
            + "cover varchar)";

    private static final String TABLE_IMAGES = "images";
    private static final String CREATE_TABLE_IMAGES = "create table if not exists " + TABLE_IMAGES + " ("
            + "url varchar primary key, "
            + "md5 varchar)";

    private static final String TABLE_ANIMALS_IMAGE = "animalsImage";
    private static final String CREATE_TABLE_ANIMALS_IMAGE = "create table if not exists " + TABLE_ANIMALS_IMAGE + " ("
            + "cardId integer primary key, "
            + "serviceId varchar, "
            + "coverImage varchar, "
            + "groupId varchar)";


    private SQLiteDatabase m_db;

    private static boolean sm_isInitTables = false;


    public Db_Animals( Context p_context)
    {
        this(p_context, DATABASE_NAME, null, DATABASE_VERSION);
        m_db = getWritableDatabase();
        onCreate( m_db );
    }

    private Db_Animals(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);//调用到SQLiteOpenHelper中
    }

    public void insert( Animal p_animal )
    {
        ContentValues values = new ContentValues();
        values.put("cardId", p_animal.m_cardNumber);
        values.put("serviceId", p_animal.m_serviceId);
        values.put("coverImage", p_animal.m_coverImage);
        values.put("groupId", p_animal.m_group);
        m_db.insert(TABLE_ANIMAL, "cardId", values);
    }

    public void insert( AnimalAudio p_animalAudio )
    {
        ContentValues t_audios = new ContentValues();
        t_audios.put("cardId", p_animalAudio.cardId);
        t_audios.put("url", p_animalAudio.url);
        t_audios.put("md5", p_animalAudio.md5);
        t_audios.put("audioType", p_animalAudio.audioType);
        m_db.insert(TABLE_AUDIOS, null, t_audios);
    }

    public void insert( BatchInfo p_batchInfo )
    {
        ContentValues t_batch = new ContentValues();
        t_batch.put("id", p_batchInfo.m_id);
        t_batch.put("name", p_batchInfo.m_batchName);
        t_batch.put("mExplain", p_batchInfo.m_explain);
        t_batch.put("cover", p_batchInfo.m_cover);
        m_db.insert(TABLE_BATCHES, null, t_batch);
    }

    public void insert( ImageInfo p_imageInfo )
    {
        ContentValues t_image = new ContentValues();
        t_image.put("url", p_imageInfo.m_url);
        t_image.put("md5", p_imageInfo.m_md5);
        m_db.insert(TABLE_IMAGES, null, t_image);
    }

    public void insertAnimalImage( Animal p_animal )
    {
        ContentValues values = new ContentValues();
        values.put("cardId", p_animal.m_cardNumber);
        values.put("serviceId", p_animal.m_serviceId);
        values.put("coverImage", p_animal.m_coverImage);
        values.put("groupId", p_animal.m_group);
        m_db.insert(TABLE_ANIMALS_IMAGE, "cardId", values);
    }

    public void update( Animal p_animal )
    {
        ContentValues values = new ContentValues();
        values.put("serviceId", p_animal.m_serviceId);
        values.put("groupId", p_animal.m_group);
        values.put("coverImage", p_animal.m_coverImage);
        m_db.update(TABLE_ANIMAL, values, "cardId=?", new String[]{ "" + p_animal.m_cardNumber } );
    }

    public void update( AnimalAudio p_animalAudio )
    {
        ContentValues t_audios = new ContentValues();
        t_audios.put("cardId", p_animalAudio.cardId);
        t_audios.put("url", p_animalAudio.url);
        t_audios.put("md5", p_animalAudio.md5);
        t_audios.put("audioType", p_animalAudio.audioType);

        m_db.update(TABLE_AUDIOS, t_audios, "id=?", new String[]{ "" + p_animalAudio.id } );
    }

    public void update( BatchInfo p_batchInfo )
    {
        ContentValues t_batch = new ContentValues();
        t_batch.put("name", p_batchInfo.m_batchName);
        t_batch.put("mExplain", p_batchInfo.m_explain);
        t_batch.put("cover", p_batchInfo.m_cover);

        m_db.update(TABLE_BATCHES, t_batch, "id=?", new String[]{ "" + p_batchInfo.m_id } );
    }

    public void update( ImageInfo p_image )
    {
        ContentValues t_image = new ContentValues();
        t_image.put("md5", p_image.m_md5);

        m_db.update(TABLE_IMAGES, t_image, "url=?", new String[]{ "" + p_image.m_url } );
    }

    public void updateAnimalImage( Animal p_animal )
    {
        ContentValues values = new ContentValues();
        values.put("serviceId", p_animal.m_serviceId);
        values.put("groupId", p_animal.m_group);
        values.put("coverImage", p_animal.m_coverImage);
        m_db.update(TABLE_ANIMALS_IMAGE, values, "cardId=?", new String[]{ "" + p_animal.m_cardNumber } );
    }

    public void delete(Animal p_animal)
    {
        m_db.delete(TABLE_ANIMAL, "cardId=?", new String[]{ "" + p_animal.m_cardNumber });
    }

    public void delete( AnimalAudio p_animalAudio )
    {
        m_db.delete(TABLE_AUDIOS, "id=?", new String[]{ "" + p_animalAudio.id });
    }

    public void delete( BatchInfo p_batch )
    {
        m_db.delete(TABLE_BATCHES, "id=?", new String[]{ p_batch.m_id });
    }

    public void delete( ImageInfo p_image )
    {
        m_db.delete(TABLE_IMAGES, "url=?", new String[]{ p_image.m_url });
    }

    public void deleteAnimalImage( Animal p_animal )
    {
        m_db.delete(TABLE_ANIMALS_IMAGE, "cardId=?", new String[]{ "" + p_animal.m_cardNumber });
    }

    public ArrayList<Animal> getListInfo()
    {
        ArrayList<Animal> t_result = new ArrayList<>();

        Cursor cursor = m_db.query(TABLE_ANIMAL, null, null, null, null, null, "cardId");
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {

                //Animal t_animal = new Animal(cursor.getInt(0), )

                int t_cardId = cursor.getInt(0);
                String t_serviceId = cursor.getString(1);
                String t_coverImage = cursor.getString(2);
                String t_groupId = cursor.getString(3);


                ArrayList<AnimalAudio> t_animalAudios = new ArrayList<>();

                Cursor t_audios_cursor = m_db.query(TABLE_AUDIOS, null, "cardId=?", new String[]{ "" + cursor.getInt(0)  }, null, null, null);

                if( t_audios_cursor.moveToFirst() )
                {
                    while(!t_audios_cursor.isAfterLast())
                    {
                        AnimalAudio t_animalAudio = new AnimalAudio( t_audios_cursor.getInt(1), t_serviceId, t_audios_cursor.getString(2), t_audios_cursor.getString(3), t_audios_cursor.getInt(4) );
                        t_animalAudio.id = t_audios_cursor.getInt(0);

                        t_animalAudios.add(t_animalAudio);

                        t_audios_cursor.moveToNext();
                    }
                }

                t_audios_cursor.close();

                t_result.add(new Animal( t_cardId, t_serviceId, t_groupId, t_coverImage, t_animalAudios ));

                cursor.moveToNext();
            }
        }
        cursor.close();

        return t_result;
    }

    public Animal getAnimalByCardId( int p_cardId )
    {
        Animal t_result = null;

        Cursor cursor = m_db.query(TABLE_ANIMAL, null, "cardId = " + p_cardId, null, null, null, "cardId");

        if (cursor.moveToFirst() && !cursor.isAfterLast() ) {

            int t_cardId = cursor.getInt(0);
            String t_serviceId = cursor.getString(1);
            String t_coverImage = cursor.getString(2);
            String t_groupId = cursor.getString(3);

            ArrayList<AnimalAudio> t_audioList = new ArrayList<AnimalAudio>();

            Cursor t_audios_cursor = m_db.query(TABLE_AUDIOS, null, "cardId = " + p_cardId, null, null, null, null);

            if( t_audios_cursor.moveToFirst() )
            {
                while(!t_audios_cursor.isAfterLast())
                {
                    t_audioList.add(new AnimalAudio( t_cardId, t_serviceId, t_audios_cursor.getString(2), t_audios_cursor.getString(3), t_audios_cursor.getInt(4)));
                    t_audios_cursor.moveToNext();
                }
            }

            t_audios_cursor.close();

            t_result = new Animal( t_cardId, t_serviceId, t_groupId, t_coverImage, t_audioList );
        }
        cursor.close();

        return t_result;
    }

    public BatchInfo getBatchInfo( String p_batchId )
    {
        BatchInfo t_result = null;

        String t_columns[] = { "id", "name", "mExplain", "cover"};

        Cursor cursor = m_db.query(TABLE_BATCHES, t_columns, "id=?", new String[]{p_batchId}, null, null, null);

        if(cursor.moveToFirst() && !cursor.isAfterLast())
        {
            t_result = new BatchInfo( cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        }

        cursor.close();

        return t_result;
    }

    public ImageInfo getImage( String p_url )
    {
        ImageInfo t_result = null;

        String t_columns[] = { "url", "md5" };

        Cursor cursor = m_db.query(TABLE_IMAGES, t_columns, "url=?", new String[]{p_url}, null, null, null);

        if(cursor.moveToFirst() && !cursor.isAfterLast())
        {
            t_result = new ImageInfo( cursor.getString(0), cursor.getString(1)  );
        }

        cursor.close();

        return t_result;
    }

    public ArrayList<String> getAnimalGroupList()
    {
        ArrayList<String> t_results = new ArrayList<>();

        String t_columns[] = { "groupId"};

        Cursor cursor = m_db.query(TABLE_ANIMAL, t_columns, null, null, "groupId",null, null );

        if(cursor.moveToFirst())
        {
            while( !cursor.isAfterLast() )
            {
                String t_groupId = cursor.getString(0);
                t_results.add(t_groupId);
                cursor.moveToNext();
            }
        }

        cursor.close();

        return t_results;
    }

    public ArrayList<BatchInfo> getBatchList()
    {
        ArrayList<BatchInfo> t_results = new ArrayList<>();

        String t_columns[] = {"id", "name", "mExplain", "cover"};

        Cursor cursor = m_db.query(TABLE_BATCHES, t_columns, null, null, null, null, null );

        if( cursor.moveToFirst() )
        {
            while(!cursor.isAfterLast())
            {
                t_results.add(new BatchInfo( cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3) ));
                cursor.moveToNext();
            }
        }

        cursor.close();

        return t_results;
    }

    public ArrayList<Animal> getAnimalsImage( String p_groupId )
    {
        ArrayList<Animal> t_result = new ArrayList<>();

        Cursor cursor = m_db.query(TABLE_ANIMALS_IMAGE, null, "groupId=?", new String[]{ p_groupId }, null, null, "cardId");
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {

                int t_cardId = cursor.getInt(0);
                String t_serviceId = cursor.getString(1);
                String t_coverImage = cursor.getString(2);
                String t_groupId = cursor.getString(3);

                ArrayList<AnimalAudio> t_animalAudios = new ArrayList<>();

                t_result.add(new Animal( t_cardId, t_serviceId, t_groupId, t_coverImage, t_animalAudios ));

                cursor.moveToNext();
            }
        }

        cursor.close();

        return t_result;
    }

    public void clearAnimals()
    {
        m_db.execSQL("drop table " + TABLE_ANIMAL);
        m_db.execSQL("drop table " + TABLE_AUDIOS);
        m_db.execSQL("drop table " + TABLE_BATCHES);
        m_db.execSQL("drop table " + TABLE_IMAGES);
        m_db.execSQL("drop table " + TABLE_ANIMALS_IMAGE);
        m_db.execSQL(CREATE_TABLE_ANIMAL);
        m_db.execSQL(CREATE_TABLE_AUDIOS);
        m_db.execSQL(CREATE_TABLE_BATCHES);
        m_db.execSQL(CREATE_TABLE_IMAGES);
        m_db.execSQL(CREATE_TABLE_ANIMALS_IMAGE);
//        m_db.execSQL("delete from " + TABLE_ANIMAL);
//        m_db.execSQL("delete from " + TABLE_AUDIOS);
//        m_db.execSQL("delete from " + TABLE_BATCHES);
//        m_db.execSQL("delete from " + TABLE_IMAGES);
//        m_db.execSQL("delete from " + TABLE_ANIMALS_IMAGE);
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
        db.execSQL(CREATE_TABLE_ANIMALS_IMAGE);
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
