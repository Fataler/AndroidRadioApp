package com.fcode.fpp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Fataler on 03.07.2016.
 */
public class BackgroundAudioService extends Service implements  MediaPlayer.OnPreparedListener {
    public static final String ACTION_PLAY = "com.action.PLAY";
    public static final String ACTION_STOP = "com.action.STOP";
    public static final String DEBUG="Music Service";

    MediaPlayer mediaPlayer = null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
       /* Uri myUri = Uri.parse("http://streaming.radio.co/sbfe60794e/listen");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(String.valueOf(myUri));
        } catch (IOException e) {
            Log.d(DEBUG,"1st time");
            e.printStackTrace();
        }*/


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            if (intent.getAction().equals(ACTION_PLAY)) {
                String url = "http://streaming.radio.co/sbfe60794e/listen";// initialize it here
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    try {
                        mediaPlayer.setDataSource(url);
                    } catch (IOException e) {
                        Log.d(DEBUG,"1st time");
                        e.printStackTrace();
                    }
                }
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
                // assign the song name to songName
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_play_circle_outline_white_24dp)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setOngoing(true)
                                .setContentTitle("FarPastPost Radio")
                                .setContentText("Radio is playing");
                int NOTIFICATION_ID = 12345;

                Intent targetIntent = new Intent(this, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nManager.notify(NOTIFICATION_ID, builder.build());

            }
        }
        if (intent.getAction().equals(ACTION_STOP)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();

        }
        return START_STICKY;
    }

    public void onDestroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        cancelNotification(this,12345);
    }
    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
