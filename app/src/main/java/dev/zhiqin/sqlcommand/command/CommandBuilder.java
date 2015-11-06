package dev.zhiqin.sqlcommand.command;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class CommandBuilder<T> {

    public static final int SQL_TYPE_SELECTED = 0;  // where(all)
    public static final int SQL_TYPE_DELETE = 1;    // where(all)
    public static final int SQL_TYPE_INSERT = 2;    // values columnNames
    public static final int SQL_TYPE_UPDATE = 3;    // values where columnNames
    public static final int SQL_TYPE_CREATE = 4;    // table columnNames
    public static final int SQL_TYPE_DROP = 5;      // table

    public int execType;

    SQLiteDatabase database;

    TableInfo<T> tableInfo;

    String where;

    String[] whereArgs;

    ArrayList<T> datas = new ArrayList<>();

    public String generateCreateSql() {
        StringBuilder sqlStrBuilder = new StringBuilder();
        sqlStrBuilder.append("CREATE TABLE ");
        sqlStrBuilder.append(tableInfo.tableName());
        sqlStrBuilder.append(" (");
        sqlStrBuilder.append(tableInfo.primaryKey()).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
        for (int i = 0; i < tableInfo.columnNames().size(); i++) {
            sqlStrBuilder.append(tableInfo.columnNames().get(i)).append(',');
        }
        sqlStrBuilder.setLength(sqlStrBuilder.length() - 1);
        sqlStrBuilder.append(")");
        return sqlStrBuilder.toString();
    }

    public String generateDropSql() {
        StringBuilder sqlStrBuilder = new StringBuilder();
        sqlStrBuilder.append("drop table if exists ");
        sqlStrBuilder.append(tableInfo.tableName());
        return sqlStrBuilder.toString();
    }

    public String generateInsertSql() {
        StringBuilder sqlStrBuilder = new StringBuilder();
        sqlStrBuilder.append("INSERT INTO ").append(tableInfo.tableName()).append("(");
        for (String field : tableInfo.columnNames()) {
            sqlStrBuilder.append(field).append(",");
        }
        sqlStrBuilder.setLength(sqlStrBuilder.length() - 1);
        sqlStrBuilder.append(") VALUES(");
        for (int i = 0; i < tableInfo.columnNames().size(); i++) {
            sqlStrBuilder.append("?,");
        }
        sqlStrBuilder.setLength(sqlStrBuilder.length() - 1);
        sqlStrBuilder.append(")");
        Log.v("MM", sqlStrBuilder.toString());
        return sqlStrBuilder.toString();
    }

    public String generateUpdateSql() {
        StringBuilder sqlStrBuilder = new StringBuilder();
        sqlStrBuilder.append("UPDATE ").append(tableInfo.tableName()).append(" SET ");
        for (String field : tableInfo.columnNames()) {
            sqlStrBuilder.append(field).append("=?, ");
        }
        sqlStrBuilder.setLength(sqlStrBuilder.length() - 2);

        if (where != null && !"".equals(where)) {
            sqlStrBuilder.append(" WHERE ").append(where);
        }
        return sqlStrBuilder.toString();
    }

    public String generateDeleteSql() {
        StringBuilder sqlStrBuilder = new StringBuilder();
        sqlStrBuilder.append("DELETE FROM ").append(tableInfo.tableName());

        if (where != null && !"".equals(where)) {
            sqlStrBuilder.append(" WHERE ").append(where);
        }
        return sqlStrBuilder.toString();
    }

    public String generateSelectSql() {
        StringBuilder sqlStrBuilder = new StringBuilder();
        sqlStrBuilder.append("SELECT * FROM ").append(tableInfo.tableName());
        if (where != null && !"".equals(where)) {
            sqlStrBuilder.append(" WHERE ").append(where);
        }
        return sqlStrBuilder.toString();
    }
}
