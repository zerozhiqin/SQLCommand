package dev.zhiqin.sqlcommand.command;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Create<T> extends DBHolder implements Command<Void> {

    public Create(SQLiteDatabase database, Class<T> clazz) {
        super(new CommandBuilder());
        commandBuilder.database = database;
        commandBuilder.tableInfo = TableInfo.TableAnnotationInfo.fromClazz(clazz);
    }

    public Create(SQLiteDatabase database, String tableName, String primaryKen, ArrayList<String> columnNames) {
        super(new CommandBuilder());
        commandBuilder.database = database;
        commandBuilder.tableInfo = new TableInfo<>(tableName, primaryKen, columnNames);
    }

    @Override
    public Void execute() {
        String sql = commandBuilder.generateCreateSql();
        commandBuilder.database.execSQL(sql);
        return null;
    }
}
