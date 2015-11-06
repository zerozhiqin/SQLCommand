package dev.zhiqin.sqlcommand.command;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Update<T> extends DBHolder<T> implements Command<Integer> {

    public Update(SQLiteDatabase database, ArrayList<T> datas) {
        super(new CommandBuilder<T>());
        commandBuilder.database = database;
        commandBuilder.tableInfo = TableInfo.TableAnnotationInfo.fromClazz((Class<T>) datas.get(0).getClass());
        commandBuilder.datas.addAll(datas);
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
            SQLiteStatement statement = database.compileStatement(commandBuilder.generateUpdateSql());
            for (T object : commandBuilder.datas) {
                statement.clearBindings();
                int i = 0;
                for (; i < tableInfo.columnFields.size(); i++) {
                    Field field = tableInfo.columnFields.get(i);
                    field.setAccessible(true);
                    Object value = field.get(object);
                    statement.bindString(i + 1, value.toString());
                }
                String pkValue = tableInfo.primaryKeyField.get(object).toString();
                statement.bindString(i + 1, pkValue);
                res = statement.executeUpdateDelete();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return res;
    }

    public Where.UpdateWhere<T> where(String where, String[] whereArgs) {
        return new Where.UpdateWhere<>(commandBuilder, where, whereArgs);
    }

    public static class UpdateSingle<T> extends Update<T> {

        public UpdateSingle(SQLiteDatabase database, T data) {
            super(database, buildList(data));
        }

        static <T> ArrayList<T> buildList(T object) {
            ArrayList<T> arr = new ArrayList<>();
            arr.add(object);
            return arr;
        }
    }

}
