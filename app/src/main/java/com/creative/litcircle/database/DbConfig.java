package com.creative.litcircle.database;


public class DbConfig {

    public static final String DB_NAME = "bgb_sylhet_sector.db";
    public static final int DB_VERSION = 1; //prev-db-version-descending{1, 200, 201, 202, 203, 204, 205, 206,207}
    public static final String DB_PASSWORD = "K3eQ71y";//K3eQ71y

    public static final String ID = "id";

    public static final String TABLE_LOCATION = "user_location";
    public static final String LOCATION_LAT = "lat";
    public static final String LOCATION_LANG = "lang";
    public static final String[] LocationColumnName = {ID, LOCATION_LAT, LOCATION_LANG};
    public static final String[] LocationColumnType = {"INTEGER", "REAL", "REAL"};
    public static final String[] LocationColumnProperty = {" PRIMARY KEY autoincrement", "", ""};


    public static final String[] TABLE_NAMES = {TABLE_LOCATION};
    public static final String[][] TABLE_ITEM = {LocationColumnName};
    public static final String[][] TABLE_TYPE = {LocationColumnType};
    public static final String[][] TABLE_PROPERTY = {LocationColumnProperty};

}
