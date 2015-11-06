package dev.zhiqin.sqlcommand.command;

import android.database.sqlite.SQLiteDatabase;

public class Drop<T> extends DBHolder<T> implements Command<Void> {

    public Drop(SQLiteDatabase database, String tableName) {
        super(new CommandBuilder<T>());
        commandBuilder.database = database;
        commandBuilder.tableInfo = new TableInfo<>(tableName, null, null);
    }

    public Drop(SQLiteDatabase database, Class<T> clazz) {
        super(new CommandBuilder<T>());
        commandBuilder.database = database;
        commandBuilder.tableInfo = TableInfo.TableAnnotationInfo.fromClazz(clazz);
    }

    @Override
    public Void execute() {
        commandBuilder.database.execSQL(commandBuilder.generateDropSql());
        return null;
    }

}
