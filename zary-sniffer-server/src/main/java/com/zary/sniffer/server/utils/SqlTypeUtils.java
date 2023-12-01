package com.zary.sniffer.server.utils;


import com.zary.sniffer.core.enums.EnumSqlType;

import java.util.List;

/**
 * @Author weiyi
 * @create 2020/3/26 17:29
 */
public class SqlTypeUtils {
    public static String getSqlTypes(List<String> opts) {
        if (opts == null || opts.size() == 0) {
            return "";
        }
        List<String> dmlActions = EnumSqlType.getActions("DML");
        List<String> ddlActions = EnumSqlType.getActions("DDL");
        List<String> dclActions = EnumSqlType.getActions("DCL");

        for (String opt : opts) {
            if (dclActions.contains(opt)) {
                return "DCL";
            } else if (ddlActions.contains(opt)) {
                return "DDL";
            } else if (dmlActions.contains(opt)) {
                return "DML";
            }

            // switch (opt) {
            //     case "SELECT":
            //     case "INSERT":
            //     case "UPDATE":
            //     case "DELETE":
            //     case "REPLACE":
            //         sqlLangs.add("DML");
            //         break;
            //     case "CREATE":
            //     case "ALTER":
            //     case "DROP":
            //     case "TRUNCATE":
            //         sqlLangs.add("DDL");
            //         break;
            //     case "COMMIT":
            //     case "GRANT":
            //     case "DENY":
            //     case "ROLLBACK":
            //     case "REFERENCES":
            //         sqlLangs.add("DCL");
            //         break;
            //     default:
            //         break;
        }
        return "";
    }
}
