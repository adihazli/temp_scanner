package com.sato.tempscanner.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaScannerConnection;
import android.os.Environment;

import com.sato.tempscanner.ScanDataActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class SqliteDBHelper extends SQLiteOpenHelper {

    String strDBName;
    SQLiteDatabase db;
    String strpath;

    public SqliteDBHelper(Context context, String strDBName, String strpath) {
        super(context, strpath +"/"+ strDBName, null, 1);
        this.strDBName = strDBName;
        this.strpath = strpath;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            System.out.println("path db : " + strpath+"/"+strDBName);
            checkDB = SQLiteDatabase.openDatabase(strpath+"/"+strDBName, null, SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            System.out.println("database doesn't exist yet");
        }
        return checkDB != null;
    }

    public void createDatabaseTable(String strTablename, String strTableColumnDetails) {
        String sqlStatement = "CREATE TABLE "+strTablename+" (" +strTableColumnDetails+")";
        db = this.getWritableDatabase();
        if (checkDatabase()) {
            if (!checkTableExistence(strTablename)) {
                db.execSQL(sqlStatement);
            } else {
                System.out.println("table already Created");
            }
        } else {
            db.execSQL(sqlStatement);
            System.out.println("Database Doesn't exist");
        }
        db.close();
    }

    public boolean checkTableExistence(String strTablename) {
        Boolean isAvailable = false;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while (cursor.moveToNext()) {
            if (strTablename.equals(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(0)))) ) {
                isAvailable = true;
            }
        }
        return isAvailable;
    }

    public boolean forceUpdate(String strTablename,String name,String id,String checkIn,String checkout) {
        Boolean isAvailable = false;
        db = this.getReadableDatabase();
        System.out.println("UPDATE "+strTablename+" SET ScanDateOut = '"+checkout+"' WHERE Name = '"+name+"' AND  IDNo = '"+id+"' AND  ScanDateIN = '"+checkIn+"'");
        Cursor cursor = db.rawQuery("UPDATE "+strTablename+" SET ScanDateOut = '"+checkout+"' WHERE Name = '"+name+"' AND IDNo = '"+id+"' AND ScanDateIN = '"+checkIn+"'", null);
        while (cursor.moveToNext()) {
            if (strTablename.equals(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(0)))) ) {
                isAvailable = true;
            }
        }
        return isAvailable;
    }

    public boolean visitorUpdate(String strTablename,String name,String id,String today) {
        Boolean isAvailable = false;
        db = this.getReadableDatabase();
        //System.out.println("UPDATE "+strTablename+" SET ScanDateOut = '"+checkout+"' WHERE Name = '"+name+"' AND  IDNo = '"+id+"' AND  substr(ScanDateIN,1,10) ='"+today.substring(0,10)+"'");
        Cursor cursor = db.rawQuery("UPDATE "+strTablename+" SET ScanDateOut = '"+today+"' WHERE Name = '"+name+"' AND IDNo = '"+id+"' AND substr(ScanDateIN,1,10) ='"+today.substring(0,10)+"' and ScanDateOut = ' '", null);
        while (cursor.moveToNext()) {
            if (strTablename.equals(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(0)))) ) {
                isAvailable = true;
            }
        }
        return isAvailable;
    }

    public boolean insertTableData(String tableName, ContentValues cValues) {
        db = this.getWritableDatabase();
        boolean isSuccess = false;
        if (db.insert(tableName, null, cValues) != -1 )  {
            isSuccess = true;
        }
        db.close();
        return isSuccess;
    }

    public void exportToCSV(String tblName,String filename){
        db = this.getWritableDatabase();
        Cursor c = null;

        try {
            c = db.rawQuery("select * from "+tblName, null);
            int rowcount = 0;
            int colcount = 0;
            File sdCardDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // the name of the file to export with
            File saveFile = new File(sdCardDir, filename);
            FileWriter fw = new FileWriter(saveFile);

            BufferedWriter bw = new BufferedWriter(fw);
            rowcount = c.getCount();
            colcount = c.getColumnCount();
            if (rowcount > 0) {
                c.moveToFirst();

                for (int i = 0; i < colcount; i++) {
                    if (i != colcount - 1) {

                        bw.write(c.getColumnName(i) + ",");

                    } else {

                        bw.write(c.getColumnName(i));

                    }
                }
                bw.newLine();

                for (int i = 0; i < rowcount; i++) {
                    c.moveToPosition(i);

                    for (int j = 0; j < colcount; j++) {
                        if (j != colcount - 1)
                            bw.write(c.getString(j) + ",");
                        else
                            bw.write(c.getString(j));
                    }
                    bw.newLine();
                }
                bw.flush();

                System.out.println("Export Succesfully");

            }
        } catch (Exception ex) {
            if (db.isOpen()) {
                db.close();
                System.out.println("Error : "+ex.getMessage());

            }

        } finally {

        }

    }

    public boolean insertTableDataTrans(String tableName, ArrayList arrContentValue) {
        db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();
        boolean isSuccess = false;

        for (int i = 0; i < arrContentValue.size(); i++) {
            if (db.insert(tableName, null, (ContentValues) arrContentValue.get(i)) != -1 )  {
                isSuccess = true;
            } else { isSuccess = false; }
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return isSuccess;
    }

    public boolean multiInsertTableData(String tableName, ContentValues cValues, ArrayList arrList) {
        db = this.getWritableDatabase();
        db.beginTransaction();
        boolean isSuccess = false;
        for (int i = 0; i < arrList.size(); i++) {
            if (db.insert(tableName, null, (ContentValues) arrList.get(i)) != -1 )  {
                isSuccess = true;
            }
        }

        if (isSuccess) {
            db.setTransactionSuccessful();
        }

        db.endTransaction();
        db.close();
        return false;
    }

    public int updateTableData(String tblName, ContentValues cValues , String whereClause, String[] whereArgs) {
        db = this.getWritableDatabase();
        int intResult = this.getWritableDatabase().update(tblName, cValues, whereClause, whereArgs);
        this.getWritableDatabase().close();
        return intResult;
    }

    public ArrayList getTableDataFromSqlite(String sqlStatement) {
        ArrayList arrayList = new ArrayList();
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sqlStatement, null);
        while (cursor.moveToNext()){
            HashMap<String, Object> thisHashMap = new HashMap();
            if (cursor.getColumnCount() > 0) {
                for (int cursorColumn = 0 ;cursorColumn < cursor.getColumnCount(); cursorColumn++) {
                    thisHashMap.put(cursor.getColumnNames()[cursorColumn], cursor.getString(cursorColumn));
                }
            }
            arrayList.add(thisHashMap);
        }
        db.close();
        cursor.close();
        return arrayList;
    }

    public int deleteTableData(String tblName, String whereClause, String[] whereArgs){
        int intResult = this.getWritableDatabase().delete(tblName, whereClause, whereArgs);
        this.getWritableDatabase().close();
        return intResult;
    }

    public int countTableData(String sqlStatement) {
        Cursor cursor = this.getReadableDatabase().rawQuery(sqlStatement, null);
        System.out.println("cursor.getCount() : " + cursor.getCount());
        int intCount = cursor.getCount();
        cursor.close();
        return  intCount;
    }

    public ArrayList sqlStatement(String sqlStatement) {
        ArrayList arrayList = new ArrayList();
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sqlStatement, null);
        while (cursor.moveToNext()){
            HashMap<String, Object> thisHashMap = new HashMap();
            if (cursor.getColumnCount() > 0) {
                for (int cursorColumn = 0 ;cursorColumn < cursor.getColumnCount(); cursorColumn++) {
                    thisHashMap.put(cursor.getColumnNames()[cursorColumn], cursor.getString(cursorColumn));
                }
            }
            arrayList.add(thisHashMap);
        }
        db.close();
        cursor.close();
        return arrayList;
    }


}
