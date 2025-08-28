package com.example.oplog.func;

public class MaskFunction implements ParseFunction {
    @Override
    public String name() {
        return "mask";
    }

    @Override
    public String apply(String value) {
        if (value == null || value.isBlank()) return "";
        String v = value.replaceAll(" +", "");
        if (v.length() >= 7) return v.substring(0, 3) + "****" + v.substring(v.length() - 4);
        return "***";
    }
}
