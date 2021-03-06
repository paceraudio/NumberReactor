package com.pacerdevelopment.numberreactor.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pacerdevelopment.numberreactor.app.application.ApplicationState;
import com.pacerdevelopment.numberreactor.app.util.GameStats;

import java.util.ArrayList;

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

    Context mContext;
    ApplicationState mState;

    public DBHelper(Context ctx) {

        super(ctx, DB_NAME, null, DB_VERSION);
        this.mContext = ctx;
        mState = (ApplicationState) mContext.getApplicationContext();
        mContext.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String sqlCreateTableGamePerformance = "CREATE TABLE "
                + TABLE_GAME_PERFORMANCE + "(" + C_GAME_NUMBER
                + " integer primary key autoincrement, " + C_DATE_PLAYED
                + " text not null, " + C_LEVEL_REACHED
                + " integer, " + C_POINTS_SCORED
                + " integer); ";
        sqLiteDatabase.execSQL(sqlCreateTableGamePerformance);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        final String sqlDropTableGamePerformance = "DROP IF TABLE EXISTS " +
                TABLE_GAME_PERFORMANCE + ":";
        sqLiteDatabase.execSQL(sqlDropTableGamePerformance);
        onCreate(sqLiteDatabase);
    }

    public void insertNewGameRowInDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String date = mState.obtainGameDate();
        cv.put(C_DATE_PLAYED, date);
        cv.put(C_LEVEL_REACHED, 1);
        cv.put(C_POINTS_SCORED, 0);
        db.insert(TABLE_GAME_PERFORMANCE, null, cv);
        db.close();
    }

    public ArrayList<GameStats> queryAllFromDb() {
        ArrayList<GameStats> arrayList = new ArrayList<GameStats>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_GAME_PERFORMANCE;
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();
        do {
            int num = c.getInt(0);
            String date = c.getString(1);
            int level = c.getInt(2);
            int points = c.getInt(3);

            GameStats gameStats = new GameStats(num, date, level, points);
            arrayList.add(gameStats);
        }
        while (c.moveToNext());
        db.close();
        return arrayList;
    }

    public void updateScoreDB(int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_POINTS_SCORED, score);
        db.update(TABLE_GAME_PERFORMANCE, cv, C_GAME_NUMBER + " = (select max(" + C_GAME_NUMBER +
                ") from " + TABLE_GAME_PERFORMANCE + ")", null);
        db.close();
    }

    public void updateLevelReached(int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_LEVEL_REACHED, level);
        db.update(TABLE_GAME_PERFORMANCE, cv, C_GAME_NUMBER + " = (select max(" + C_GAME_NUMBER +
                ") from " + TABLE_GAME_PERFORMANCE + ")", null);
        db.close();
    }
}
