package dev.zhiqin.sqlcommand.command;

public class DBHolder<T> {
    CommandBuilder<T> commandBuilder;

    public DBHolder(CommandBuilder<T> builder) {
        commandBuilder = builder;
    }
}
