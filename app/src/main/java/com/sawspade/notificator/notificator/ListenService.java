package com.sawspade.notificator.notificator;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;



public class ListenService extends Service {




    DBHelper dbHelper;
    NotificationManager manager;
    Notification myNotication;
    ClipboardManager clipboardManager;

    long lastClipChange = 0;


    final String LOG_TAG = "LOG_TAG";




    public void onCreate(){
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();




        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.webstorm)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVibrate(new long[]{0})
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

        final Intent resultIntent = new Intent(this, Main2Activity.class);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        final Context context = this;


        dbHelper = new DBHelper(this);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager
                .addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                    @Override
                    public void onPrimaryClipChanged() {

                        Log.d(LOG_TAG, "--------------------------BEGIN-----------------------------------");


                        long time_now = System.currentTimeMillis();
                        Log.d(LOG_TAG, "time_now = " + time_now);
                        Log.d(LOG_TAG, "last_chg = " + lastClipChange);


                        ClipData clipData = clipboardManager.getPrimaryClip();

                        Log.d(LOG_TAG, "raw_clip_data = " + clipData.toString());

                        CharSequence text = clipData.getItemAt(0).getText();

                        if (text != null) {
                            if (time_now - lastClipChange < 500) {
                                Log.d(LOG_TAG, "duplicate = " + text + "  :  " + time_now);
                                lastClipChange = time_now;
                                return;
                            }
                        }else{
                            Log.d(LOG_TAG, "text = null");
                            return;
                        }

                        lastClipChange = time_now;



                        Log.d("LOG_TAG", "clipData = " + text + " " + time_now);
                        Toast.makeText(context , "clipData = " + text, Toast.LENGTH_LONG).show();

                        stackBuilder.addParentStack(Main2Activity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                        mBuilder.setContentIntent(resultPendingIntent);
                        mBuilder.setContentTitle(text);


                        String value = get_sql(text.toString());
                        if (value == null)
                            return;

                        updateWord(text.toString());

                        mBuilder.setContentText(value);

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int mId = 1000303;
                        mNotificationManager.cancel(mId);
                        mNotificationManager.notify(mId, mBuilder.build());


                        Log.d(LOG_TAG, "---------------------------END------------------------------------");




                    }
                });

    }

    public void showTranslation(String text){

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.webstorm)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVibrate(new long[]{0})
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

        final Intent resultIntent = new Intent(this, Main2Activity.class);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(Main2Activity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setContentTitle(text);

        String value = get_sql(text);
        if (value == null)
            return;



        mBuilder.setContentText(value);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int mId = 1000303;
        mNotificationManager.cancel(mId);
        mNotificationManager.notify(mId, mBuilder.build());




    }



    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(LOG_TAG, "onStartCommand");
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_LONG).show();
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    public void onDestroy(){
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        Toast.makeText(this, "destroy", Toast.LENGTH_LONG).show();
    }

    public long counter = 0;
    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;
    public void startTimer(){
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 15 * 60 * 1000);
    }
    public void initializeTimerTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                long now_time = System.currentTimeMillis();
                Log.d(LOG_TAG, "in timer +++++ " + (counter++));
                if (now_time - lastClipChange < 120000) {
                    Log.d(LOG_TAG, "skip");
                    return;
                }

                String word = getRandomWord();
                Log.d(LOG_TAG, "random_word = " + word);


                showTranslation(word);
            }
        };

    }






    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void someTask(){


    }


    public String get_sql(final String search){

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



        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                StringBuilder result = new StringBuilder();
                try {
                    URL url = new URL("http://aesc.fsrbit.ru/notificator.php?word=" + search);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    try {
                        JSONObject json = new JSONObject(result.toString());

                        System.out.println("ok");

                        //JSONArray dict = json.getJSONArray("dict");
                        //JSONArray terms = dict.getJSONObject(0).getJSONArray("terms");
                        //String value = "";

                        //for (int i=0;i<Math.min(terms.length(), 3);i++){
                        //    if (i!=0)
                        //        value += ", ";
                        //    value += terms.get(i);
                        //}

                        JSONArray sents = json.getJSONArray("sentences");
                        String value = ((JSONObject)(sents.getJSONObject(0))).get("trans").toString();




                        insertWord(search, value);
                        showTranslation(search);


                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }




            }
        }).start();
        return null;

    }

    public String getRandomWord(){


        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT word FROM universal WHERE status>0", null);
        int count = c.getCount();

        int idx = (int)(Math.random() * count);


        int i=0;
        String result = "not found";
        if (c != null)
            if (c.moveToFirst()){
               while (true){
                if (idx == i) {
                    result = c.getString(c.getColumnIndex("word"));
                    break;
                }
                c.moveToNext();
                i = i + 1;
               }
            }

        db.close();
        return result;

    }

    public void updateWord(String word){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE universal SET status=1 WHERE status=0 and word=\'" + word + "\';");
        db.close();

    }

    public void insertWord(String word, String value){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO universal (word, value, status) VALUES ('" + word + "', '" + value + "', 0);" );
        db.close();
    }
}
