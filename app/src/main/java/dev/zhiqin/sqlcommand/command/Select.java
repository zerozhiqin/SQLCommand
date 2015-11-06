package dev.zhiqin.sqlcommand.command;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Select<T> extends DBHolder<T> implements Command<ArrayList<T>> {

    public Select(SQLiteDatabase database, Class<T> clazz) {
        super(new CommandBuilder());
        commandBuilder.database = database;
        commandBuilder.tableInfo = TableInfo.TableAnnotationInfo.fromClazz(clazz);
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

    public Where.SelectWhere<T> where(String where, String... whereArgs) {
        return new Where.SelectWhere<>(commandBuilder, where, whereArgs);
    }
}
