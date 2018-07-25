package com.example.demo.utils;

public class DatabaseUtils {
     /**
     *
     * @return
     */
    public static String getSqlTypeFromJavaType(String aClass) {
                
        if (aClass != null) {
            switch (aClass.substring(0, aClass.length() - 2)) {
                case "java.lang.String": {
                    return "varchar";
                }
                case "java.math.BigDecimal": {
                    return "decimal";
                }
                case "java.lang.Boolean": {
                    return "bit";
                }
                case "boolean": {
                    return "bit";
                }
                case "java.langInteger": {
                    return "smallint";
                }
                case "int": {
                    return "smallint";
                }
                case "java.lang.Long": {
                    return "bigint";
                }
                case "long": {
                    return "bigint";
                }
                case "double": {
                    return "float";
                }
                case "java.lang.Double": {
                    return "float";
                }
                case "java.sql.Date": {
                    return "date";
                }
                case "java.sql.Time": {
                    return "time";
                }
                case "java.sql.Timestamp": {
                    return "timestamp";
                }
            }
        }

        return "";
    }
}
