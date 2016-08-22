package com.fcode.fpp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String SONG = "SongInfo";
    private Menu menu;
    //UI PART
    ImageButton startPlaybackButton;
    ImageView trackArt;
    TextView trackInfoView;
    Intent playbackServiceIntent;
    Boolean isPlaying;
    boolean exit;
    DBHelper DB;
    //RelativeLayout relMain;
    CoordinatorLayout mainCoordinator;
    //ASYNC PART
    String myJSON;

    List<String> PathList; // ERROR
    String track, art, trackName;
    private static final String TAG_TRACK = "title";
    private static final String TAG_ART = "artwork_url";


    private static final String TAG_CURRENT = "current_track";
    JSONObject trackinfo = null;
    Toolbar toolbar;


    public  void initviews(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //relMain = (RelativeLayout) findViewById(R.id.relMain);
        mainCoordinator = (CoordinatorLayout) findViewById(R.id.mainCoordinator);
        trackInfoView = (TextView) findViewById(R.id.trackInfo);
        startPlaybackButton = (ImageButton) this.findViewById(R.id.StartPlaybackButton);

        trackArt = (ImageView) findViewById(R.id.logo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initviews();
        PathList = new ArrayList<String>();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("RADIO");
        DB = new DBHelper(this);
        //getSupportActionBar().setSubtitle("suka");

        if (savedInstanceState != null) {
            trackName = savedInstanceState.getString("trackName");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //init ImageView
        Picasso.with(MainActivity.this)
                .load(R.drawable.fpp_logo)
                .fit()
                .centerInside()
                .placeholder(R.drawable.fpp_logo)
                .error(R.drawable.fpp_logo)
                .into(trackArt);

        if (isMyServiceRunning(BackgroundAudioService.class)) {
            isPlaying = true;

        } else {
            isPlaying = false;

        }
        if (isPlaying) {
            startPlaybackButton.setImageResource(R.drawable.vector_drawable_ic_pause_svg);
            handler.removeCallbacks(runnableCode);
            handler.post(runnableCode);
            checkIcon();

        } else {
            startPlaybackButton.setImageResource(R.drawable.vector_drawable_ic_play_svg);
            handler.removeCallbacks(runnableCode);
        }

        startPlaybackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    if(Util.isOnline(MainActivity.this)){
                        Intent i = new Intent(MainActivity.this, BackgroundAudioService.class);
                        i.setAction(BackgroundAudioService.ACTION_PLAY);

                        startService(i);
                        handler.post(runnableCode);

                        startPlaybackButton.setImageResource(R.drawable.vector_drawable_ic_pause_svg);
                        isPlaying = true;
                    }else{
                        Snackbar
                                .make(mainCoordinator, "Connection error! You are offline.", Snackbar.LENGTH_LONG)
                                .setAction("CHECK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                                    }
                                })

                                .show();
                    }
                } else {

                    startPlaybackButton.setImageResource(R.drawable.vector_drawable_ic_play_svg);
                    Intent i = new Intent(MainActivity.this, BackgroundAudioService.class);
                    i.setAction(BackgroundAudioService.ACTION_STOP);
                    stopService(i);

                    handler.removeCallbacks(runnableCode);
                    isPlaying = false;
                }
            }
        });


        playbackServiceIntent = new Intent(this, BackgroundAudioService.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (exit) {
                isPlaying = false;
                Intent i = new Intent(MainActivity.this, BackgroundAudioService.class);
                i.setAction(BackgroundAudioService.ACTION_STOP);
                stopService(i);
                handler.removeCallbacks(runnableCode);
                /*Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);*/
                finish(); // finish activity

            } else {

                Snackbar.make(mainCoordinator, "Press Back again to Exit.", Snackbar.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3 * 1000);

            }

        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("trackName", trackName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        checkIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_fav) {
            if (track != null) {
                if (DB.trackExist(DBHelper.TABLE_TRACKS, trackName)) {
                    //Toast.makeText(MainActivity.this, "Track already added", Toast.LENGTH_SHORT).show();
                    Snackbar.make(mainCoordinator, "Track already added", Snackbar.LENGTH_SHORT).show();
                    menu.getItem(0).setIcon(R.drawable.ic_star_white_24dp);
                } else {
                    DB.insertTrack(art, track.replace("\n", " "));
                    //Toast.makeText(MainActivity.this, "Track added as favorite.", Toast.LENGTH_SHORT).show();
                    Snackbar.make(mainCoordinator, "Track added as favorite.", Snackbar.LENGTH_SHORT).show();
                    menu.getItem(0).setIcon(R.drawable.ic_star_white_24dp);
                }
                return true;
            } else {
                //Toast.makeText(MainActivity.this, "There is nothing to add as favorite.", Toast.LENGTH_SHORT).show();
                Snackbar.make(mainCoordinator, "There is nothing to add as favorite.", Snackbar.LENGTH_SHORT).show();
            }
        }

        if (id == R.id.action_share) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    sharetrack();
                } else {
                    Toast.makeText(MainActivity.this, "Press Yes, for sharing activation.", Toast.LENGTH_SHORT).show();
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                }
            } else {
                sharetrack();
            }


            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void sharetrack() {
        if (track != null) {

            String path = MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), ((BitmapDrawable) trackArt.getDrawable()).getBitmap(), "Image Description", null);
            Uri uri = Uri.parse(path);
            PathList.add(path);
            String shareBody = "Hey! Just found cool track: " + track.replace("\n", " - ") + " @FarPastPost RadioApp.";
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Track sharing");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
        } else {
            //Toast.makeText(MainActivity.this, "There is nothing to share.", Toast.LENGTH_SHORT).show();
            Snackbar.make(mainCoordinator, "There is nothing to share.", Snackbar.LENGTH_SHORT).show();

        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_radio) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_fav) {
            Intent i = new Intent(this, FavoriteActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnableCode);
        for (String i : PathList) {
            File fdelete = new File(i);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    Log.d("Deleting", "file Deleted :" + i);
                } else {
                    Log.d("Deleting", "file not Deleted :" + i);
                }
            }

        }
        super.onDestroy();
    }

    public void getData() {
        class DownloadSongInfo extends AsyncTask<String, Void, String> {

            //private ProgressDialog pDialog;

            protected void onPreExecute() {
                super.onPreExecute();


            }

            @Override
            protected String doInBackground(String... args) {

                URL url = null;
                InputStream inputStream = null;
                String result = null;
                try {
                    url = new URL("https://public.radio.co/stations/sbfe60794e/status");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-type", "application/json");

                    inputStream = urlConnection.getInputStream();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    // Oops
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (Exception squish) {
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                //Log.d("tag",result);
                showData();
            }
        }
        DownloadSongInfo g = new DownloadSongInfo();
        g.execute();
    }

    public void showData() {
        JSONObject jsonObj = null;
        TextView trackArtist = (TextView) findViewById(R.id.trackartist);

        if(Util.isOnline(MainActivity.this)){

            try {
                jsonObj = new JSONObject(myJSON);
                trackinfo = jsonObj.getJSONObject(TAG_CURRENT);

                track = trackinfo.getString(TAG_TRACK).replace(" - ", "\n");

                String artist = track.substring(0, track.indexOf("\n"));
                trackName = track.substring(track.indexOf("\n")).replace("\n", "");
                art = trackinfo.getString(TAG_ART).replace("100x100bb", "400x400bb");
                if (trackInfoView != null) {
                    trackInfoView.setText(trackName);
                }
                if ((art != null) && (trackInfoView.getText().toString().equals(trackName))) {


                    Picasso.with(MainActivity.this)
                            .load(art)
                            .fit()
                            .centerInside()
                            .placeholder(R.drawable.fpp_logo)
                            .error(R.drawable.fpp_logo)
                            .into(trackArt);
                    Palette palette = Palette.from(((BitmapDrawable) trackArt.getDrawable()).getBitmap()).generate();
                    int defaultcolor = 0x000000;
                    // relMain.setBackgroundColor(palette.getDarkMutedColor(defaultcolor));
                    // toolbar.setBackgroundColor(palette.getDarkMutedColor(defaultcolor));
                }

                if (trackArtist != null) {
                    trackArtist.setText(artist);
                }


                Log.d(SONG, track + " " + art + " " + artist + " " + trackName);
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(MainActivity.this, "Data load error! Check your internet connection.", Toast.LENGTH_SHORT).show();
                Snackbar.make(mainCoordinator, "Data load error! Check your internet connection.", Snackbar.LENGTH_SHORT).show();

            }
        }else{
            Snackbar.make(mainCoordinator, "Connection lost.", Snackbar.LENGTH_LONG).show();

        }

    }

    //util shiet
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMyServiceRunning(BackgroundAudioService.class)) {
            isPlaying = true;
            Log.d(SONG, "We are running");
            checkIcon();
        } else {
            isPlaying = false;
            Log.d(SONG, "We are not running");
        }
    }

    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            getData();
            checkIcon();
            //Toast.makeText(MainActivity.this, "ebola 5s", Toast.LENGTH_SHORT).show();
            Log.d("Handlers", "Called on main thread");
            // Repeat this the same runnable code block again another 10 seconds
            //handler.post(runnableCode);
            handler.postDelayed(runnableCode, 10 * 1000);
        }
    };

    public void checkIcon() {
        if ((menu != null) && (trackName != null)) {

            if (DB.trackExist(DBHelper.TABLE_TRACKS, trackName.replace("\n", ""))) {
                menu.getItem(0).setIcon(R.drawable.ic_star_white_24dp);
            } else {
                menu.getItem(0).setIcon(R.drawable.ic_star_border_white_24dp);
            }
        }
    }


}
