package dev.zhiqin.sqlcommand.command;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Where<T> extends DBHolder<T> {


    public Where(CommandBuilder<T> builder, String where, String[] whereArgs) {
        super(builder);
        commandBuilder.where = where;
        commandBuilder.whereArgs = whereArgs;
    }

    public static class SelectWhere<T> extends Where<T> implements Command<ArrayList<T>> {

        public SelectWhere(CommandBuilder<T> builder, String where, String[] whereArgs) {
            super(builder, where, whereArgs);
        }

        @Override
        public ArrayList<T> execute() {
            Cursor cursor = commandBuilder.database.rawQuery(commandBuilder.generateSelectSql(), commandBuilder.whereArgs);
            ArrayList<T> result = new ArrayList<>();

            TableInfo.TableAnnotationInfo<T> tableInfo = (TableInfo.TableAnnotationInfo<T>) commandBuilder.tableInfo;
            while (cursor.moveToNext()) {
                try {
                    T object = tableInfo.type.newInstance();
                    tableInfo.primaryKeyField.set(object, cursor.getLong(cursor.getColumnIndex(tableInfo.primaryKey())));

                    ArrayList<Field> fields = tableInfo.columnFields;
                    ArrayList<String> names = tableInfo.columnNames();

                    for (int i = 0; i < fields.size(); i++) {
                        Field field = fields.get(i);
                        String name = names.get(i);
                        int index = cursor.getColumnIndex(name);
                        Type type = field.getType();
                        if (type == String.class) {
                            field.set(object, cursor.getString(index));
                        } else if (type == byte[].class) {
                            field.set(object, cursor.getBlob(index));
                        } else if (type == Double.class || type == double.class) {
                            field.set(object, cursor.getDouble(index));
                        } else if (type == Float.class || type == float.class) {
                            field.set(object, cursor.getFloat(index));
                        } else if (type == Integer.class || type == int.class) {
                            field.set(object, cursor.getInt(index));
                        } else if (type == Long.class || type == long.class) {
                            field.set(object, cursor.getLong(index));
                        } else if (type == Short.class || type == short.class) {
                            field.set(object, cursor.getShort(index));
                        } else if (type == Boolean.class || type == boolean.class) {
                            field.set(object, "true".equalsIgnoreCase(cursor.getString(index)));
                        } else if (type == Character.class || type == char.class) {
                            String str = cursor.getString(index);
                            if (str != null && str.length() > 0) {
                                field.setChar(object, str.charAt(0));
                            }
                        } else {
                        }
                    }
                    result.add(object);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }
    }

    public static class DeleteWhere<T> extends Where<T> implements Command<Integer> {

        public DeleteWhere(CommandBuilder<T> builder, String where, String[] whereArgs) {
            super(builder, where, whereArgs);
        }

        @Override
        public Integer execute() {
            int res = -1;
            SQLiteDatabase database = commandBuilder.database;
            database.beginTransaction();
            SQLiteStatement statement = database.compileStatement(commandBuilder.generateDeleteSql());
            for (int j = 0; j < commandBuilder.whereArgs.length; j++) {
                statement.bindString(j + 1, commandBuilder.whereArgs[j]);
            }
            res = statement.executeUpdateDelete();
            database.setTransactionSuccessful();
            database.endTransaction();

            return res;
        }
    }

    public static class UpdateWhere<T> extends Where<T> implements Command<Integer> {

        public UpdateWhere(CommandBuilder<T> builder, String where, String[] whereArgs) {
            super(builder, where, whereArgs);
        }

        @Override
        public Integer execute() {
            int res = -1;

            try {
                TableInfo.TableAnnotationInfo<T> tableInfo = (TableInfo.TableAnnotationInfo<T>) commandBuilder.tableInfo;
                SQLiteDatabase database = commandBuilder.database;
                database.beginTransaction();
                SQLiteStatement statement = database.compileStatement(commandBuilder.generateUpdateSql());
                for (T object : commandBuilder.datas) {
                    int i = 0;
                    for (; i < tableInfo.columnFields.size(); i++) {
                        Field field = tableInfo.columnFields.get(i);
                        field.setAccessible(true);
                        Object value = field.get(object);
                        statement.bindString(i + 1, value.toString());
                    }
                    for (int j = 0; i < tableInfo.columnFields.size() + commandBuilder.whereArgs.length; i++, j++) {
                        statement.bindString(i + 2, commandBuilder.whereArgs[j]);
                    }
                    res = statement.executeUpdateDelete();
                }
                database.setTransactionSuccessful();
                database.endTransaction();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return res;
        }
    }

}
