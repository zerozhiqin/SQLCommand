package dev.zhiqin.sqlcommand.command;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

public class Delete<T> extends DBHolder<T> implements Command<Integer> {

    public Delete(SQLiteDatabase database, ArrayList<T> datas) {
        super(new CommandBuilder<T>());
        commandBuilder.database = database;
        if (datas != null) {
            commandBuilder.tableInfo = TableInfo.TableAnnotationInfo.fromClazz((Class<T>) datas.get(0).getClass());
            commandBuilder.datas.addAll(datas);
        }
    }

    @Override
    public Integer execute() {
        int res = -1;
        try {
            TableInfo.TableAnnotationInfo<T> tableInfo = (TableInfo.TableAnnotationInfo<T>) commandBuilder.tableInfo;
            SQLiteDatabase database = commandBuilder.database;
            database.beginTransaction();
            String pkName = tableInfo.primaryKey;
            commandBuilder.where = pkName + "=?";
            tableInfo.primaryKeyField.setAccessible(true);
            SQLiteStatement statement = database.compileStatement(commandBuilder.generateDeleteSql());
            for (T object : commandBuilder.datas) {
                String pkValue = tableInfo.primaryKeyField.get(object).toString();
                statement.bindString(1, pkValue);
                res = statement.executeUpdateDelete();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static class DeleteSingle<T> extends Delete<T> {

        public DeleteSingle(SQLiteDatabase database, T datas) {
            super(database, Update.UpdateSingle.buildList(datas));
        }
    }

    public static class DeleteWithArgument<T> extends DBHolder<T> {

        public DeleteWithArgument(SQLiteDatabase database, String tableName) {
            super(new CommandBuilder<T>());
            commandBuilder.database = database;
            commandBuilder.tableInfo = new TableInfo<>();
            commandBuilder.tableInfo.tableName = tableName;
        }

        public Where.DeleteWhere<T> where(String where, String... whereArgs) {
            return new Where.DeleteWhere<>(commandBuilder, where, whereArgs);
        }
    }

}
