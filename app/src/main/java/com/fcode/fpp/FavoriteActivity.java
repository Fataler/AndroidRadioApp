package com.fcode.fpp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BottomSheetBehavior<RelativeLayout> mBehavior;
    DBHelper DB;
    FavoriteListAdapter adapter;
    ListView fav_list;
    ArrayList<HashMap<String, Object>> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        mBehavior = BottomSheetBehavior.from((RelativeLayout) findViewById(R.id.fav_rel_layout));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DB= new DBHelper(this);

        data = DB.getAll(DBHelper.TABLE_TRACKS);
        fav_list= (ListView) findViewById(R.id.fav_list);
        adapter= new FavoriteListAdapter(
                this,
                data,
                R.layout.favorite_list,
                new String[]{DBHelper.TRACKS_COLUMN_TRACK},
                new int[]{R.id.fav_track}
        );

        if (fav_list != null) {

            fav_list.setAdapter(adapter);

        }
        /*fav_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String textToCopy=fav_list.getItemAtPosition(position).toString();
                Toast.makeText(FavoriteActivity.this, textToCopy+" \n copied to Clipboard.", Toast.LENGTH_SHORT).show();
            }
        });*/


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public class FavoriteListAdapter extends SimpleAdapter {
        DBHelper DB;
        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public FavoriteListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);

        }

        public View getView(final int position, final View convertView, ViewGroup parent){
            // here you let SimpleAdapter built the view normally.
            notifyDataSetChanged();
            View v = super.getView(position, convertView, parent);

            // Then we get reference for Picasso
            ImageView img = (ImageView) v.getTag();
            if(img == null){
                img = (ImageView) v.findViewById(R.id.fav_art);
                v.setTag(img); // <<< THIS LINE !!!!
            }
            // get the url from the data you passed to the `Map`
            String url = (String) ((Map)getItem(position)).get(DBHelper.TRACKS_COLUMN_ART);
            // do Picasso
            Picasso
                    .with(v.getContext())
                    .load(url)
                    .placeholder(R.drawable.fpp_logo)
                    .error(R.drawable.fpp_logo)
                    .into(img);



            final ImageButton fav_del = (ImageButton) v.findViewById(R.id.fav_delete);
            DB=new DBHelper(v.getContext());

            final String id = (String) ((Map)getItem(position)).get(DBHelper.TRACKS_COLUMN_ID);

            fav_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(FavoriteActivity.this)
                        .setTitle("Favorite delete.")
                        .setMessage("Delete this track from Your favorite list?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("DEBUG","delete?"+id);
                                DB.delete(DBHelper.TABLE_TRACKS, Integer.valueOf(id));
                                data.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                    }

            });
            final TextView fav_track= (TextView) v.findViewById(R.id.fav_track);
            fav_track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String textToCopy=fav_track.getText().toString();
                    ClipboardManager clipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("TextToCopy", textToCopy);
                    clipMan.setPrimaryClip(clip);
                    Toast.makeText(FavoriteActivity.this, textToCopy+" \ncopied to clipboard.", Toast.LENGTH_SHORT).show();
                }
            });
            // return the view
            return v;
        }


    }

}
