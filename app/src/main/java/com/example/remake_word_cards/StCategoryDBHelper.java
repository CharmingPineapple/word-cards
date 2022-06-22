package com.example.remake_word_cards;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StCategoryDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "stCategoryDB";
    public static final String TABLE_CATEGORY = "categories";
    public static final int DB_VERSION = 1;

    public static final String KEY_ID = "_id";
    public static final String KEY_CATEGORY_NAME = "category_name";


    //Конструктор
    public StCategoryDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase dbCategories){
        dbCategories.execSQL("create table " + TABLE_CATEGORY + "(" + KEY_ID + " integer primary key," + KEY_CATEGORY_NAME + " text" + ") ");
        insertCategory(dbCategories, "Noun");
        insertCategory(dbCategories, "Verb");
        insertCategory(dbCategories, "Adjective");
        insertCategory(dbCategories, "Adverb");
        insertCategory(dbCategories, "Other");
    }

    private static void insertCategory(SQLiteDatabase db, String category_name){
        ContentValues contentValues = new ContentValues();
        contentValues.put(StCategoryDBHelper.KEY_CATEGORY_NAME, category_name);
        db.insert(StCategoryDBHelper.TABLE_CATEGORY, null, contentValues);
        contentValues.clear();
    }

    @Override
    public void onUpgrade(SQLiteDatabase categoryDB, int oldVersion, int newVersion){
        categoryDB.execSQL("drop table if exists " + TABLE_CATEGORY);

        onCreate(categoryDB);
    }

}
