package dev.zhiqin.sqlcommand;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import dev.zhiqin.sqlcommand.command.Create;
import dev.zhiqin.sqlcommand.command.Delete;
import dev.zhiqin.sqlcommand.command.Drop;
import dev.zhiqin.sqlcommand.command.Insert;
import dev.zhiqin.sqlcommand.command.Select;
import dev.zhiqin.sqlcommand.command.Update;

public class SQL {
    SQLiteDatabase database;

    SQL(SQLiteDatabase database) {
        this.database = database;
    }

    public static SQL with(SQLiteDatabase database) {
        return new SQL(database);
    }

    public <T> Create<T> create(Class<T> clazz) {
        Create<T> create = new Create<T>(database, clazz);
        return create;
    }

    public Create<?> create(String tableName, String primaryKen, ArrayList<String> columnNames) {
        Create<?> create = new Create<>(database, tableName, primaryKen, columnNames);
        return create;
    }


    public <T> Drop<T> drop(Class<T> clazz) {
        Drop<T> drop = new Drop<T>(database, clazz);
        return drop;
    }

    public Drop<?> drop(String tableName) {
        Drop<?> drop = new Drop<>(database, tableName);
        return drop;
    }

    public <T> Select<T> select(Class<T> clazz) {
        Select<T> select = new Select<>(database, clazz);
        return select;
    }

    public <T> Update.UpdateSingle<T> update(T data) {
        return new Update.UpdateSingle<>(database, data);
    }

    public <T> Update<T> update(ArrayList<T> datas) {
        return new Update<>(database, datas);
    }

    public <T> Delete<T> delete(ArrayList<T> datas) {
        return new Delete<>(database, datas);
    }

    public <T> Delete.DeleteSingle<T> delete(T datas) {
        return new Delete.DeleteSingle<>(database, datas);
    }

    public <T> Delete.DeleteWithArgument<T> delete(String tableName) {
        return new Delete.DeleteWithArgument<>(database, tableName);
    }

    public <T> Insert<T> insert(ArrayList<T> datas) {
        return new Insert<>(database, datas);
    }

    public <T> Insert.InsertSingle<T> insert(T data) {
        return new Insert.InsertSingle<>(database, data);
    }


    public static final String TAG = "DATABASE";

    public static void printDB(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            Log.v(TAG, name);
        }
    }

    public static void printTable(SQLiteDatabase db, String table) {
        Cursor cursor = db.rawQuery("select * from " + table, null);
        StringBuilder sb = null;
        Log.i(TAG, "==================TABLE : " + table + "==================");
        while (cursor.moveToNext()) {
            if (sb == null) {
                sb = new StringBuilder();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    sb.append(cursor.getColumnName(i)).append('\t');
                }
                Log.i(TAG, sb.toString());
                Log.i(TAG, "- - - - - - - - - - - - - - - - - - - - - - - - ");
                sb.setLength(0);
            }
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                sb.append(cursor.getString(i)).append('\t');
            }
            Log.i(TAG, sb.toString());
            sb.setLength(0);
        }
        Log.i(TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }
}
