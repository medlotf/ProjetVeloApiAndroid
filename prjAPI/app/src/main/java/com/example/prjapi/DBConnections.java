package com.example.prjapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import org.json.JSONObject;

import java.util.ArrayList;

public class DBConnections extends SQLiteOpenHelper {

    public static final String DbName="mnProjetAndroid.db";
    public static final int Version=1;

    public DBConnections(Context context){
        super(context,DbName,null,Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table IF NOT EXISTS villes (name TEXT primary key)");
        db.execSQL("create table IF NOT EXISTS stations (number TEXT,stationName TEXT,positionLat TEXT,positionLarg TEXT,constractName TEXT,foreign key(constractName) references villes(name))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop table if EXISTS villes");
        db.execSQL("Drop table if EXISTS stations");
        onCreate(db);
    }

    public void InsertVilles(String v){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();

        contentValues.put("name",v);
        db.insert("villes",null,contentValues);
    }

    public ArrayList getAllVilles(){
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from villes",null);
        res.moveToFirst();
        while(res.isAfterLast()==false){
            array_list.add(res.getString(res.getColumnIndex("name")));
            res.moveToNext();
        }
        return array_list;
    }

    public void InsertStations(String num,String stName,String lat,String larg,String constName){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();

        contentValues.put("number",num);
        contentValues.put("stationName",stName);
        contentValues.put("positionLat",lat);
        contentValues.put("positionLarg",larg);
        contentValues.put("constractName",constName);

        db.insert("stations",null,contentValues);

    }

    public ArrayList getStations(){
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from stations",null);
        res.moveToFirst();
        while(res.isAfterLast()==false){
            array_list.add(res.getString(res.getColumnIndex("stationName")));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList getStation(String sName,String cName){
        ArrayList data = new ArrayList();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from stations where stationName='"+ sName +"' AND constractName='"+cName+"'",null);
        res.moveToFirst();
        data.add(res.getString(res.getColumnIndex("number")));
        data.add(res.getString(res.getColumnIndex("stationName")));
        data.add(res.getString(res.getColumnIndex("positionLat")));
        data.add(res.getString(res.getColumnIndex("positionLarg")));
        data.add(res.getString(res.getColumnIndex("constractName")));

        return data;
    }
}
