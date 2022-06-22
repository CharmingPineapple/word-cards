package com.example.remake_word_cards;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameResultDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "gameResultDB";
    public static final String TABLE_RESULT = "results";
    public static final int DB_VERSION = 3;

    public static final String KEY_ID = "_id";
    public static final String KEY_CATEGORY_NAME = "category_name";
    public static final String KEY_TRANSLATES_AMOUNT = "translates_amount";
    public static final String KEY_RIGHT_ANSWER_AMOUNT = "right_answer_amount";


    //Конструктор
    public GameResultDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase dbResults){
        dbResults.execSQL("create table " + TABLE_RESULT + "(" + KEY_ID + " integer primary key,"
                + KEY_CATEGORY_NAME + " text," + KEY_TRANSLATES_AMOUNT + " text," + KEY_RIGHT_ANSWER_AMOUNT + " text" + ") ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase gameResultDB, int oldVersion, int newVersion){
        gameResultDB.execSQL("drop table if exists " + TABLE_RESULT);

        onCreate(gameResultDB);
    }

}
