package com.sawspade.notificator.notificator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ubuntu on 2/8/17.
 */
class DBHelper extends SQLiteOpenHelper {

    final String LOG_TAG = "LOG_TAG";


    public DBHelper(Context context){
        super(context, "dict", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        Log.d(LOG_TAG, "--- onCreate database ---");
        db.execSQL("CREATE TABLE universal (" +
                "id integer primary key autoincrement," +
                "word varchar(255)," +
                "value mediumtext," +
                "status int" +
                ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }


}