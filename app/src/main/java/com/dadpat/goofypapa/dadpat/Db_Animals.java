package com.dadpat.goofypapa.dadpat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lenovo on 2018/3/26.
 */

public class Db_Animals extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "animal.db";//数据库名字
    private static final int DATABASE_VERSION = 1;//数据库版本号
    private static final String CREATE_TABLE = "create table animal ("
            + "id integer primary key autoincrement,"
            + "cardId int, "
            + "serviceId text)";

    public Db_Animals( Context p_context )
    {
        this(p_context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private Db_Animals(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);//调用到SQLiteOpenHelper中
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
