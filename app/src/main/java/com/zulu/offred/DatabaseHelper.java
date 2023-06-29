package com.zulu.offred;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydatabase.db";
    private static final String ComentTABLE_NAME = "comments";
    private static final String ArticleTABLE_NAME = "articles";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_STRING1 = "article";
    private static final String COLUMN_STRING2 = "info";
    private static final String COLUMN_STRING3 = "comment";
    private static final String COLUMN_STRING4 = "title";
    private static final String COLUMN_STRING5 = "data";
    private static final String COLUMN_STRING6 = "link";
    private static final String COLUMN_STRING7 = "media";
    private static final String COLUMN_STRING8 = "sub";

    private static final String COLUMN_INT1 = "indent";
    private static final int DATABASE_VERSION = 1;



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + ComentTABLE_NAME + " (" +
                COLUMN_ID + " TEXT unique, " +
                COLUMN_STRING1 + " TEXT, " +
                COLUMN_STRING2 + " TEXT, " +
                COLUMN_STRING3 + " TEXT," +
                COLUMN_INT1 + " INTEGER" +
                ")";
        db.execSQL(createTableQuery);
        String createartTableQuery = "CREATE TABLE IF NOT EXISTS " + ArticleTABLE_NAME + " (" +
                COLUMN_ID + " TEXT unique, " +
                COLUMN_STRING4 + " TEXT, " +
                COLUMN_STRING5 + " TEXT, " +
                COLUMN_STRING6 + " TEXT," +
                COLUMN_STRING7 + " TEXT," +
                COLUMN_STRING8 + " TEXT" +
                ")";
        db.execSQL(createartTableQuery);
        String createsubsQuery = "INSERT OR IGNORE INTO "+ArticleTABLE_NAME+"("+COLUMN_ID+","+COLUMN_STRING4+", "+COLUMN_STRING5+", "+COLUMN_STRING6+", "+COLUMN_STRING7+", "+COLUMN_STRING8+") VALUES("+
                "'base','Subreddits','Lastupdated:never','','','subs'"+")";
        db.execSQL(createsubsQuery);
        String createdefaultsQuery = "INSERT OR IGNORE INTO "+ComentTABLE_NAME+"("+COLUMN_ID+", "+COLUMN_STRING1+", "+COLUMN_STRING2+", "+COLUMN_STRING3+", "+COLUMN_INT1+") VALUES("+
                "'allsub','base','Lastupdated:never','all',0)";
        db.execSQL(createdefaultsQuery);
        createdefaultsQuery = "INSERT OR IGNORE INTO "+ComentTABLE_NAME+"("+COLUMN_ID+", "+COLUMN_STRING1+", "+COLUMN_STRING2+", "+COLUMN_STRING3+", "+COLUMN_INT1+") VALUES("+
                "'picsub','base','Lastupdated:never','pics',0)";
        db.execSQL(createdefaultsQuery);
        createdefaultsQuery = "INSERT OR IGNORE INTO "+ComentTABLE_NAME+"("+COLUMN_ID+", "+COLUMN_STRING1+", "+COLUMN_STRING2+", "+COLUMN_STRING3+", "+COLUMN_INT1+") VALUES("+
                "'funnysub','base','Lastupdated:never','funny',0)";
        db.execSQL(createdefaultsQuery);
        createdefaultsQuery = "INSERT OR IGNORE INTO "+ComentTABLE_NAME+"("+COLUMN_ID+", "+COLUMN_STRING1+", "+COLUMN_STRING2+", "+COLUMN_STRING3+", "+COLUMN_INT1+") VALUES("+
                "'newssub','base','Lastupdated:never','news',0)";
        db.execSQL(createdefaultsQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropcTableQuery = "DROP TABLE IF EXISTS " + ComentTABLE_NAME;
        db.execSQL(dropcTableQuery);
        String dropaTableQuery = "DROP TABLE IF EXISTS " + ArticleTABLE_NAME;
        db.execSQL(dropaTableQuery);
        onCreate(db);
    }

    public void insertComment(String id,String article, String info, String comment, int indent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_STRING1, article);
        values.put(COLUMN_STRING2, info);
        values.put(COLUMN_STRING3, comment);
        values.put(COLUMN_INT1, indent);
        db.insertWithOnConflict(ComentTABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
    public void insertArticle(String id, String title, String data, String link, String media, String sub) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_STRING4, title);
        values.put(COLUMN_STRING5, data);
        values.put(COLUMN_STRING6, link);
        values.put(COLUMN_STRING7, media);
        values.put(COLUMN_STRING8, sub);
        db.insertWithOnConflict(ArticleTABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
    public void startFetch( String sub) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = { "%" + sub + "sub%" };
        ContentValues values = new ContentValues();
        values.put(COLUMN_STRING2, "Updating...");
        db.update(ComentTABLE_NAME,values,COLUMN_ID + " LIKE ?",selectionArgs);
        db.close();
    }

    public List<MainActivity.article> fetchArticles(String sub) {
        List<MainActivity.article> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_STRING4, COLUMN_STRING5, COLUMN_STRING6,COLUMN_STRING7, COLUMN_STRING8};
        String selection = COLUMN_STRING8 + " LIKE ?";
        String[] selectionArgs = { "%" + sub + "%" };
        Cursor cursor = db.query(ArticleTABLE_NAME, columns, selection, selectionArgs,null, null, null);
        System.out.println("got arts for sub:"+sub);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String string0 = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String string1 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING4));
                @SuppressLint("Range") String string2 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING5));
                @SuppressLint("Range") String string3 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING6));
                @SuppressLint("Range") String string4 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING7));
                @SuppressLint("Range") String string5 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING8));
                System.out.println(string0+":"+string1+":"+string2+":"+string3+":"+string4+":"+string5);
                MainActivity.article newArt = new MainActivity.article(string0,string1,string2,string3,string4,string5);

                dataList.add(newArt);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dataList;
    }

    public List<MainActivity.coms> fetchComments(String art) {
        List<MainActivity.coms> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_STRING1, COLUMN_STRING2, COLUMN_STRING3,COLUMN_INT1};
        String selection = COLUMN_STRING1 + " LIKE ?";
        String[] selectionArgs = { "%" + art + "%" };

        Cursor cursor = db.query(ComentTABLE_NAME, columns, selection, selectionArgs,null, null, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String string0 = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String string1 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING1));
                @SuppressLint("Range") String string2 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING2));
                @SuppressLint("Range") String string3 = cursor.getString(cursor.getColumnIndex(COLUMN_STRING3));
                @SuppressLint("Range") int string4 = cursor.getInt(cursor.getColumnIndex(COLUMN_INT1));

                MainActivity.coms newCom = new MainActivity.coms(string0,string3,string2,string1,string4);

                dataList.add(newCom);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dataList;
    }

    public void clearDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        String dropcTableQuery = "DROP TABLE IF EXISTS " + ComentTABLE_NAME;
        db.execSQL(dropcTableQuery);
        String dropaTableQuery = "DROP TABLE IF EXISTS " + ArticleTABLE_NAME;
        db.execSQL(dropaTableQuery);
        onCreate(db);
    }

    public void clearSub(String sub){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_STRING1, COLUMN_STRING2, COLUMN_STRING3,COLUMN_INT1};
        String selection = COLUMN_STRING8 + " LIKE ?";
        String[] selectionArgs = { "%" + sub + "%" };
        int cursor = db.delete(ArticleTABLE_NAME, selection, selectionArgs);
        System.out.println("deleted from adb:"+cursor);
        selection = COLUMN_STRING1 + " LIKE ?";
        int cursor2 = db.delete(ComentTABLE_NAME, selection, selectionArgs);

        System.out.println("deleted from cdb:"+cursor2);
    }
}
