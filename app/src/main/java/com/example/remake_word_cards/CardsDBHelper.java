package com.example.remake_word_cards;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CardsDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "cardsDB";
    public static final String TABLE_CARDS = "cards";
    public static final int DB_VERSION = 1;

    public static final String KEY_ID = "_id";
    public static final String KEY_WORD = "word";
    public static final String KEY_TRAN_1 = "tran_1";
    public static final String KEY_TRAN_2 = "tran_2";
    public static final String KEY_TRAN_3 = "tran_3";
    public static final String KEY_TRAN_4 = "tran_4";
    public static final String KEY_TRAN_5 = "tran_5";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_ST_CATEGORY = "st_category";
    public static final String KEY_CUSTOM_CATEGORY = "custom_category";


    //Конструктор
    public CardsDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase dbCards) {
        dbCards.execSQL("create table " + TABLE_CARDS + "(" + KEY_ID + " integer primary key," + KEY_WORD + " text,"
                + KEY_TRAN_1 + " text," + KEY_TRAN_2 + " text," + KEY_TRAN_3 + " text," + KEY_TRAN_4 + " text,"
                + KEY_TRAN_5 + " text," + KEY_DESCRIPTION + " text," + KEY_ST_CATEGORY + " text,"
                + KEY_CUSTOM_CATEGORY + " text" + ") ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase dbCards, int oldVersion, int newVersion) {
        dbCards.execSQL("drop table if exists " + TABLE_CARDS);

        onCreate(dbCards);
    }

}
