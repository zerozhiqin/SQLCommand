package dev.zhiqin.sqlcommand.command;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Insert<T> extends DBHolder<T> implements Command<Long> {

    public Insert(SQLiteDatabase database, ArrayList<T> datas) {
        super(new CommandBuilder<T>());
        commandBuilder.database = database;
        commandBuilder.tableInfo = TableInfo.TableAnnotationInfo.fromClazz((Class<T>) datas.get(0).getClass());
        commandBuilder.datas.addAll(datas);
    }

    @Override
    public Long execute() {
        long res = -1;
        try {
            TableInfo.TableAnnotationInfo<T> tableInfo = (TableInfo.TableAnnotationInfo<T>) commandBuilder.tableInfo;
            SQLiteDatabase database = commandBuilder.database;
            tableInfo.primaryKeyField.setAccessible(true);
            database.beginTransaction();
            SQLiteStatement statement = database.compileStatement(commandBuilder.generateInsertSql());
            for (T object : commandBuilder.datas) {
                statement.clearBindings();
                int i = 0;
                for (; i < tableInfo.columnFields.size(); i++) {
                    Field field = tableInfo.columnFields.get(i);
                    field.setAccessible(true);
                    Object value = field.get(object);
                    statement.bindString(i + 1, value.toString());
                }
                res = statement.executeInsert();
                tableInfo.primaryKeyField.setLong(object, res);
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static class InsertSingle<T> extends Insert<T> {

        public InsertSingle(SQLiteDatabase database, T data) {
            super(database, buildList(data));
        }

        static <T> ArrayList<T> buildList(T object) {
            ArrayList<T> arr = new ArrayList<>();
            arr.add(object);
            return arr;
        }
    }

}
