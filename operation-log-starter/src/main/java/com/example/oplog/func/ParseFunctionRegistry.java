package com.example.oplog.func;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseFunctionRegistry {
    private final Map<String, ParseFunction> map = new HashMap<>();

    public ParseFunctionRegistry(List<ParseFunction> fns) {
        if (fns != null) for (var f : fns) if (f.name() != null && !f.name().isBlank()) map.put(f.name(), f);
    }

    public ParseFunction get(String name) {
        return map.get(name);
    }

    public boolean isBefore(String name) {
        return map.containsKey(name) && map.get(name).executeBefore();
    }
}
