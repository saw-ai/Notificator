package com.sawspade.notificator.notificator;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends ActionBarActivity {
    final String LOG_TAG = "LOG_TAG";
    Button btnShow, btnClear, btnCharge;
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


        /*



        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View arg0) {
                //API level 11
                Log.d("LOG", "click");





                stackBuilder.addParentStack(Main2Activity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                int mId = 1000303;
                mNotificationManager.notify(mId, mBuilder.build());


            }
        });


*/
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View arg0) {
                //manager.cancel(11);

                String search = et.getText().toString();
                String answer = get_sql(search.toLowerCase());
                Log.d(LOG_TAG, "answer = " + answer);
                tv.setText(answer);

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
        btnShow = (Button) findViewById(R.id.btnShowNotification);
        btnClear = (Button) findViewById(R.id.btnClearNotification);
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
