package com.apps.vincentperez.mapminder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder

class DatabaseHandler : SQLiteOpenHelper {


    companion object {

        val Tag = "DatabaseHandler"
        val DBName = "mapminderDB"
        val DBVersion = 4

        val tableName = "reminder"
        val ConID = "id"
        val Title = "title"
        val Address = "address"
        val Latitude = "lat"
        val Longitude = "lon"
        val Content = "content"
    }

    var context: Context? = null
    var sqlObj: SQLiteDatabase

    constructor(context: Context) : super(context, DBName, null, DBVersion) {

        this.context = context;
        sqlObj = this.writableDatabase;
    }

    override fun onCreate(p0: SQLiteDatabase?) {

        //SQL for creating table
        var sql1: String = "CREATE TABLE IF NOT EXISTS " + tableName + " " +
                "(" + ConID + " INTEGER PRIMARY KEY," +
                Title + " TEXT, " + Address + " TEXT," + Latitude + " REAL, " + Longitude +
                " REAL, " + Content + " TEXT );"

        p0!!.execSQL(sql1);
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

        p0!!.execSQL("Drop table IF EXISTS " + tableName)
        onCreate(p0)

    }

    fun AddReminder(values: ContentValues): Long {

        //var Msg: String = "error";
        val ID = sqlObj!!.insert(tableName, "", values)

        /*if (ID > 0) {
            Msg = "ok"
        }*/
        return ID
    }

    fun FetchMarker(id: Long): Reminder? {
        var reminder: Reminder? = null
        val sqb = SQLiteQueryBuilder()
        sqb.tables = tableName
        val cols = arrayOf("id", "title", "address", "lat", "lon", "content")

        val cur = sqb.query(sqlObj, cols, "id = " + id, null, null, null, "id desc")

        if (cur.moveToFirst()) {
            val id = cur.getLong(cur.getColumnIndex("id"))
            val title = cur.getString(cur.getColumnIndex("title"))
            val address = cur.getString(cur.getColumnIndex("address"))
            val lat = cur.getDouble(cur.getColumnIndex("lat"))
            val lon = cur.getDouble(cur.getColumnIndex("lon"))
            val content = cur.getString(cur.getColumnIndex("content"))
            reminder = Reminder(id, title, address, lat, lon, content)
        }
        return reminder
    }

    fun FetchMarkers(keyword: String): ArrayList<Reminder> {

        var arraylist = ArrayList<Reminder>()
        var reminder: Reminder? = null
        val sqb = SQLiteQueryBuilder()
        sqb.tables = tableName
        val cols = arrayOf("id", "title", "address", "lat", "lon", "content")
        val rowSelArg = arrayOf(keyword)

        val cur = sqb.query(sqlObj, cols, "title like ?", rowSelArg, null, null, "id desc")

        if (cur.moveToFirst()) {
            do {
                val id = cur.getLong(cur.getColumnIndex("id"))
                val title = cur.getString(cur.getColumnIndex("title"))
                val address = cur.getString(cur.getColumnIndex("address"))
                val lat = cur.getDouble(cur.getColumnIndex("lat"))
                val lon = cur.getDouble(cur.getColumnIndex("lon"))
                val content = cur.getString(cur.getColumnIndex("content"))
                arraylist.add(Reminder(id, title, address, lat, lon, content))
            } while (cur.moveToNext())
        }
        return arraylist
    }

    fun fetchContent(id: Long): String {
        var content: String = ""
        val sqb = SQLiteQueryBuilder()
        sqb.tables = tableName
        val cols = arrayOf("id", "content")


        val cur = sqb.query(sqlObj, cols, "id = " + id.toString(), null, null, null, "id desc")

        if (cur.moveToFirst()) {
                content = cur.getString(cur.getColumnIndex("content"))
        }

        return content
    }


    fun UpdateMarker(values: ContentValues, id: Long): Int {

        var selectionArs = arrayOf(id.toString())

        val i = sqlObj!!.update(tableName, values, "id=?", selectionArs)
        if (i > 0) {
            return 1
        } else {
            return -1
        }
    }

    fun RemoveMarker(id: Long): Int {

        var selectionArs = arrayOf(id.toString())

        val i = sqlObj!!.delete(tableName, "id=?", selectionArs)
        if (i > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}