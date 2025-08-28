package com.example.oplog.func;

public interface ParseFunction {
    default boolean executeBefore() {
        return false;
    }

    String name();

    String apply(String value);
}
