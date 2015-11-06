package dev.zhiqin.sqlcommand.command;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.WeakHashMap;

import dev.zhiqin.sqlcommand.annotation.Column;
import dev.zhiqin.sqlcommand.annotation.PrimaryKey;
import dev.zhiqin.sqlcommand.annotation.Table;


public class TableInfo<T> {
    String tableName;
    String primaryKey;
    ArrayList<String> columnNames = new ArrayList<>();

    public TableInfo() {
    }

    public TableInfo(String tableName, String primaryKey, ArrayList<String> columnNames) {
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        this.columnNames = columnNames;
    }

    public String tableName() {
        return tableName;
    }

    public String primaryKey() {
        return primaryKey;
    }

    public ArrayList<String> columnNames() {
        return columnNames;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    private static WeakHashMap<String, TableAnnotationInfo<?>> weakHashMap = new WeakHashMap<>();

    public static <T> TableAnnotationInfo<T> fromClazz(Class<T> clazz) {
        TableAnnotationInfo<T> tableInfo = (TableAnnotationInfo<T>) weakHashMap.get(clazz.getName());
        if (tableInfo == null) {
            tableInfo = TableAnnotationInfo.buildFromClazz(clazz);
            weakHashMap.put(clazz.getName(), tableInfo);
        }

        return tableInfo;
    }

    public static class TableAnnotationInfo<T> extends TableInfo<T> {
        Field primaryKeyField;
        ArrayList<Field> columnFields = new ArrayList<>();
        Class<T> type;

        private static <T> TableAnnotationInfo<T> buildFromClazz(Class<T> clazz) {
            TableAnnotationInfo<T> tableInfo = new TableAnnotationInfo<>();
            tableInfo.type = clazz;
            Table table = clazz.getAnnotation(Table.class);
            if (table == null) {
                throw new IllegalArgumentException("class " + clazz.getSimpleName() + " not a Table");
            } else {
                tableInfo.setTableName(table.value());
            }

            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                Column column = field.getAnnotation(Column.class);
                if (primaryKey != null) {
                    tableInfo.setPrimaryKey(primaryKey.value());
                    tableInfo.primaryKeyField = field;
                } else if (column != null) {
                    if ("".equals(column.value())) {
                        throw new IllegalArgumentException("empty column name");
                    } else {
                        tableInfo.columnFields.add(field);
                        tableInfo.columnNames.add(column.value());
                    }
                }
            }
            return tableInfo;
        }


    }
}
