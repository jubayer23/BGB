package com.creative.litcircle.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.creative.litcircle.model.User;
import com.creative.litcircle.model.UserLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fahad_000 on 7/8/2015.
 */
public class SqliteDb extends SQLiteOpenHelper {


    SQLiteDatabase db;


    public SqliteDb(Context context) {
        super(context, DbConfig.DB_NAME, null, DbConfig.DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, DbConfig.TABLE_NAMES, DbConfig.TABLE_ITEM, DbConfig.TABLE_TYPE, DbConfig.TABLE_PROPERTY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version, int current_version) {

    }


    public void open() {
        db = this.getWritableDatabase();
    }


    public void executeQuery(String query) {
        db.execSQL(query);
    }


    /******************************************
     * CAMERA
     ************************************/


    public long addLocation(Location location) {
        if (db == null) {
            db = getWritableDatabase();
        }

        ContentValues values = new ContentValues();
        values.put(DbConfig.LOCATION_LAT, location.getLatitude());
        values.put(DbConfig.LOCATION_LANG, location.getLongitude());
        long id = db.insert(DbConfig.TABLE_LOCATION, null, values);
        Log.d("DEBUG_DB", "location inserted into database");
        return id;
    }

    public UserLocation getLocation() {
        UserLocation camera = null;
        if (db == null) {
            db = getReadableDatabase();
        }
        String sql = "select * from " + DbConfig.TABLE_LOCATION + " LIMIT 1";
        Cursor c = db.rawQuery(sql, null);
        if (c != null && c.moveToFirst()) {
            camera = new UserLocation();
            camera.setId(c.getInt(c.getColumnIndex(DbConfig.ID)));
            camera.setLat(c.getDouble(c.getColumnIndex(DbConfig.LOCATION_LAT)));
            camera.setLang(c.getDouble(c.getColumnIndex(DbConfig.LOCATION_LANG)));
        }
        return camera;
    }

    public List<UserLocation> getAllPendingLocations() {
        if (db == null) {
            db = getWritableDatabase();
        }
        List<UserLocation> list = new ArrayList<>();
        UserLocation camera = null;
        Cursor c = db.rawQuery("select * from " + DbConfig.TABLE_LOCATION, null);
        if (c != null && c.moveToFirst()) {
            do {
                camera = new UserLocation();
                camera.setId(c.getInt(c.getColumnIndex(DbConfig.ID)));
                camera.setLat(c.getDouble(c.getColumnIndex(DbConfig.LOCATION_LAT)));
                camera.setLang(c.getDouble(c.getColumnIndex(DbConfig.LOCATION_LANG)));
                list.add(camera);
            } while (c.moveToNext());
        }
        return list;
    }

    public boolean deleteLocation(int id) {
        if (db == null) {
            db = getReadableDatabase();
        }
        return db.delete(DbConfig.TABLE_LOCATION, DbConfig.ID + "=" + id, null) > 0;
    }

    public void truncateTable() {
        if (db == null) {
            db = getReadableDatabase();
        }
        String sql = "Delete from " + DbConfig.TABLE_LOCATION;
        db.execSQL(sql);
        db.execSQL("VACUUM");
        sql = "DELETE FROM SQLITE_SEQUENCE WHERE name=" + "'" + DbConfig.TABLE_LOCATION + "'";
        db.execSQL(sql);
    }


    public void createTable(SQLiteDatabase db, String[] TableName, String[][] tableItem, String[][] tableType, String[][] tableProperty) {
        String[] keyWithType = new String[TableName.length];
        for (int k = 0; k < TableName.length; k++) {
            for (int l = 0; l < tableItem[k].length; l++) {
                if (l != 0) {
                    keyWithType[k] = keyWithType[k] + ", " + tableItem[k][l] + " " + tableType[k][l] + tableProperty[k][l];
                } else {
                    keyWithType[k] = tableItem[k][l] + " " + tableType[k][l] + tableProperty[k][l];
                }
            }
        }
        for (int i = 0; i < TableName.length; i++) {
            String createTableQuery = "CREATE TABLE " + TableName[i] + " (" + keyWithType[i] + ")";
            db.execSQL(createTableQuery);
        }
    }


}

