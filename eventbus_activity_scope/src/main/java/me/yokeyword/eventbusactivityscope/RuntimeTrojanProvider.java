package me.yokeyword.eventbusactivityscope;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Internal class to initialize EventBusActivityScope.
 * @hide
 *
 * Created by YoKey on 17/10/17.
 */
public class RuntimeTrojanProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        EventBusActivityScope.init(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
