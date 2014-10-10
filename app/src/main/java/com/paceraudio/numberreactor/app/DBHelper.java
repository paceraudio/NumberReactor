package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jeffwconaway on 10/9/14.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "scoreDataBase.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_GAME_PERFORMANCE = "gamePerformance";
    private static final String C_GAME_NUMBER = "gameNumber";
    private static final String C_DATE_PLAYED = "datePlayed";
    private static final String C_LEVEL_REACHED = "levelReached";
    private static final String C_POINTS_SCORED = "pointsScored";

    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String sqlCreateTableGamePerformance = "CREATE TABLE"
                + TABLE_GAME_PERFORMANCE + "(" + C_GAME_NUMBER
                + " integer primary key autoincrement, " + C_DATE_PLAYED
                + " text not null, " + C_LEVEL_REACHED
                + " text not null, " + C_POINTS_SCORED
                + " text not null); ";
        sqLiteDatabase.execSQL(sqlCreateTableGamePerformance);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        final String sqlDropTableGamePerformance = "DROP IF TABLE EXISTS " + TABLE_GAME_PERFORMANCE + ":";
        sqLiteDatabase.execSQL(sqlDropTableGamePerformance);
        onCreate(sqLiteDatabase);
    }
}
