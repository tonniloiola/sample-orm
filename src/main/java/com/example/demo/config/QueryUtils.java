package com.example.demo.config;

public class QueryUtils {

    public static String removeUnusedCommas(String query) {
        query=query.replaceAll(", ,", " ");
        int index = query.indexOf(", )");
        do {
            if (index > 0) {
                query = query.substring(0, index) + query.substring(index + 2);
                index = query.indexOf(", )");
            }
        } while (index > 0);
        
        query = query.replaceAll("\\s+", " ");
        query=query.replaceAll(", WHERE", " WHERE");
        
        return query;
    }
}
