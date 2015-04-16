package no.gruppe2.shera.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.ListIterator;

import no.gruppe2.shera.dto.Event;

/**
 * Created by pernille.sethre on 13.03.2015.
 */
public class SqlLiteDBHandler extends SQLiteOpenHelper {

    static String TABLE_EVENTS = "Events";
    static String KEY_EVENT_ID = "_ID";
    static String KEY_OWN_EVENT = "OwnEvent";
    static int DATABASE_VERSION = 1;
    static String DATABASE_NAME = "SHERADatabase";

    public SqlLiteDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_EVENTS + "(" + KEY_EVENT_ID +
                " VARCHAR(40) PRIMARY KEY, " + KEY_OWN_EVENT + " INTEGER " + ")";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    public void eventJoined(String eventID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT_ID, eventID);
        values.put(KEY_OWN_EVENT, 0);
        db.insert(TABLE_EVENTS, null, values);
        db.close();
    }

    public void eventCreated(String eventID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT_ID, eventID);
        values.put(KEY_OWN_EVENT, 1);
        db.insert(TABLE_EVENTS, null, values);
        db.close();
    }

    public ArrayList<String> getOwnEvents() {
        ArrayList<String> idList = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_OWN_EVENT + " = 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                idList.add(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        db.close();
        return idList;
    }

    public ArrayList<String> getJoinedEvents() {
        ArrayList<String> idList = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_OWN_EVENT + " = 0";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                idList.add(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        db.close();
        return idList;
    }

    public ArrayList<String> getAllEvents() {
        ArrayList<String> idList = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                idList.add(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        db.close();
        return idList;
    }

    public void deleteEventID(String eventID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, KEY_EVENT_ID + "= ?", new String[]{
                String.valueOf(eventID)});
        db.close();
    }

    public int countOwnEvents() {
        String countQuery = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_OWN_EVENT + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        db.close();
        return cursor.getCount();
    }

    public int countJoinedEvents() {
        String countQuery = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_OWN_EVENT + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        db.close();
        return cursor.getCount();
    }

    public void updateSqlLite(ArrayList<Event> events, String userID) {

        ArrayList<String> arrayList = getAllEvents();

        if (!arrayList.isEmpty()) {
            for (int i = 0; i < arrayList.size(); i++) {
                deleteEventID(arrayList.get(i));
            }
        }

        ListIterator<Event> eventIterator = events.listIterator();

        while (eventIterator.hasNext()) {
            Event e = eventIterator.next();

            if (e.getUserID() == Long.parseLong(userID)) {
                eventCreated(e.getEventID());
            } else {
                if (e.getParticipantsList() != null) {
                    ListIterator<Long> userIterator = e.getParticipantsList().listIterator();

                    while (userIterator.hasNext()) {
                        Long eventUserID = userIterator.next();
                        if (eventUserID == Long.parseLong(userID)) {
                            eventJoined(e.getEventID());
                        }
                    }
                }
            }
        }

    }

}
