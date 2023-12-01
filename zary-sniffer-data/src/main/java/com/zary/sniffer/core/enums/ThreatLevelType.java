package com.zary.sniffer.core.enums;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ThreatLevelType {
    低风险(0, 69),
    中风险(70, 79),
    高风险(80, 100);

    int start;
    int end;

    ThreatLevelType(int s, int e) {
        this.start = s;
        this.end = e;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public static String getName(int v) {
        for (ThreatLevelType type : ThreatLevelType.values()) {
            if (type.getStart() <= v && v < type.getEnd()) {
                return type.name();
            }
        }
        return "";
    }

    public static Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap();
        ThreatLevelType[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            ThreatLevelType type = var1[var3];
            map.put(type.name(), type.getStart() + "-" + type.getEnd());
        }

        return map;
    }
}
