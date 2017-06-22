package com.sawspade.notificator.notificator;


import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.nio.channels.FileChannel;

public class MainActivity extends ActionBarActivity {
    final String LOG_TAG = "LOG_TAG";
    Button btnShow, btnBackUp, btnCharge;
    TextView tv;
    EditText et;


    DBHelper dbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialise();

        startService(new Intent(this, ListenService.class));


        dbHelper = new DBHelper(this);
        final Context ctx = this;

        btnBackUp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View arg0) {

                try {
                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();
                    String state = Environment.getExternalStorageState();

                    int permission = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED)
                        System.out.println("bad");


                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        String currentDBPath = "/data/user/0/" + getPackageName() + "/databases/dict";
                        String backupDBPath = "bu.db";
                        File currentDB = new File(currentDBPath);
                        File backupDB = new File(sd, backupDBPath);

                        if (currentDB.exists()) {
                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            //FileChannel dst = new FileOutputStream(backupDB).getChannel();
                            FileChannel dst = new FileOutputStream("/storage/emulated/0/bu.db").getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }














            }
        });

        btnCharge.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View arg0) {
                //manager.cancel(11);

                charge_sql();

            }
        });

        tv.setText(count_of_sql() + " rows");

    }

    private void initialise() {
        //btnShow = (Button) findViewById(R.id.btnShowNotification);
        btnBackUp = (Button) findViewById(R.id.btnBackUp);
        btnCharge = (Button) findViewById(R.id.btnCharrge);
        et = (EditText) findViewById(R.id.editText2);
        tv = (TextView) findViewById(R.id.textView2);

    }


    public int count_of_sql(){

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("universal", new String[]{"count(*) as count"}, null, null, null, null, null);

        if (c != null)
            if (c.moveToFirst()){
                int count = c.getInt(c.getColumnIndex("count"));
                dbHelper.close();
                return count;
            }

        dbHelper.close();
            return -1;
    }



    public void charge_sql(){






        new Thread(new Runnable() {
            @Override
            public void run() {

                ContentValues cv = new ContentValues();
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                BufferedReader reader = null;
                try{
                    reader = new BufferedReader(
                            new InputStreamReader(getAssets().open("universal"), "UTF-16"));
                    String mLine;

                    int i=0;

                    int count = 0;
                    String word;
                    String value;
                    long rowId;
                    while ((mLine = reader.readLine()) != null){

                        if (i == 1){

                            count++;
                            final int j = count;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText("" + j);
                                }
                            });

                        }

                        if (i == 0){
                            cv.put("word", mLine.replaceAll("\'", "\\\'"));
                        }else{
                            cv.put("value", mLine.replaceAll("\'", "\\\'"));
                            cv.put("status", 0);
                            rowId = db.insert("universal", null, cv);
                            Log.d(LOG_TAG, "" + count);
                            cv.clear();
                        }
                        i = 1 - i;
                    }

                } catch (IOException e){
                    Log.d(LOG_TAG, e.toString());
                } finally {
                    if (reader != null){
                        try{
                            reader.close();
                        } catch (IOException e){
                            Log.d(LOG_TAG, e.toString());
                        }

                    }
                }


                dbHelper.close();




            }
        }).start();





    }


    public String get_sql(String search){

        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query("universal", new String[] {"value"}, "word=\'" + search.toLowerCase().replaceAll("\'", "\\\'") + "\'", null, null, null, null, 1 + "");


        if (c != null)
            if (c.moveToFirst()){
                String val = c.getString(c.getColumnIndex("value"));
                dbHelper.close();
                return val;
            }

        dbHelper.close();
        return null;

    }













}
