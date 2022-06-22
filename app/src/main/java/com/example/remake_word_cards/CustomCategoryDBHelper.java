package com.example.remake_word_cards;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CustomCategoryDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "customCategoryDB";
    public static final String TABLE_CATEGORY = "categories";
    public static final int DB_VERSION = 1;

    public static final String KEY_ID = "_id";
    public static final String KEY_CATEGORY_NAME = "category_name";


    //Конструктор
    public CustomCategoryDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase dbCategories){
        dbCategories.execSQL("create table " + TABLE_CATEGORY + "(" + KEY_ID + " integer primary key," + KEY_CATEGORY_NAME + " text" + ") ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase categoryDB, int oldVersion, int newVersion){
        categoryDB.execSQL("drop table if exists " + TABLE_CATEGORY);

        onCreate(categoryDB);
    }

}
