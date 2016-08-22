package com.fcode.fpp;

/**
 * Created by Fataler on 07.07.2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * Created by Ruslan on 21.12.2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "FPP_fav_tracks.db";

    public static final String TABLE_TRACKS = "tracks";

    //tracks
    public static final String TRACKS_TABLE_NAME = "agents";
    public static final String TRACKS_COLUMN_ID = "id";
    public static final String TRACKS_COLUMN_ART = "art";
    public static final String TRACKS_COLUMN_TRACK = "track";



    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                //agents
                "create table tracks " +
                        "(id integer primary key, art text,track text)");



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        List<String> tables = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table';", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String tableName = cursor.getString(1);
            if (!tableName.equals("android_metadata") &&
                    !tableName.equals("sqlite_sequence"))
                tables.add(tableName);
            cursor.moveToNext();
        }
        cursor.close();

        for (String tableName : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
        }
        onCreate(db);
    }

    public boolean insertTrack(String art, String track) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRACKS_COLUMN_ART, art);
        contentValues.put(TRACKS_COLUMN_TRACK, track);

        db.insert(TABLE_TRACKS, null, contentValues);
        Log.d("INSERT", art + " " + track );
        return true;
    }
    public boolean trackExist(String table, String track) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "";
        switch (table) {
            case TABLE_TRACKS:
                query = "select * from tracks where track LIKE ? ";
                break;

        }
        String[] a = new String[1];
        a[0]  ="%"+track+"%";
        Cursor res = db.rawQuery(query , a);
        //res.isNull(0);
        if (res.getCount()>0){
            Log.d("true",query+track+" "+String.valueOf(res.getCount()));
            return true;
        }else{
            Log.d("false",query+track+" "+String.valueOf(res.getCount()));
            return false;
        }

    }



    public Cursor get(String table, int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "";
        switch (table) {
            case TABLE_TRACKS:
                query = "select * from tracks where id=";
                break;

        }
        Cursor res = db.rawQuery(query + id + "", null);
        return res;

    }


    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_TRACKS);
        return numRows;
    }

    public boolean updateAgent(Integer id, String name, String phone, String email, String address, String place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("address", address);
        contentValues.put("place", place);
        db.update("agents", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer delete(String table, Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        switch (table) {
            case TABLE_TRACKS:

                return db.delete(TABLE_TRACKS,
                        "id = ? ",
                        new String[]{Integer.toString(id)});


        }

        return id;
    }


    public ArrayList<HashMap<String, Object>> getAll(String table) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = new HashMap<String, Object>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res;
        switch (table) {
            case TABLE_TRACKS:
                res = db.rawQuery("select * from tracks", null);
                res.moveToFirst();

                while (res.isAfterLast() == false) {
                    map = new HashMap<String, Object>();
                    map.put("id", res.getString(res.getColumnIndex(TRACKS_COLUMN_ID)));
                    map.put("art", res.getString(res.getColumnIndex(TRACKS_COLUMN_ART)));
                    map.put("track", res.getString(res.getColumnIndex(TRACKS_COLUMN_TRACK)));

                    data.add(map);
                    res.moveToNext();
                }
                return data;

        }
        return data;
    }

}