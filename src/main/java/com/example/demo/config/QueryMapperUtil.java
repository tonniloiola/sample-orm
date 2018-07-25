package com.example.demo.config;


import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtils;

import com.example.demo.utils.DatabaseUtils;


public class QueryMapperUtil {

    /**
     * Provides with the new parameterised query
     *
     * @param query
     * @return
     */
    public static String parseQuery(String query) {
        List<String> params = findParamsInQuery(query);
        query = newQueryBuilder(query, params);
        return query;
    }

    /**
     * Automatically Map the Parameters from a class for a given query
     *
     * @param query
     * @param oc
     */
    public static Object[] mapObjectForQuery(String query, Object oc) {
        List<String> params = findParamsInQuery(query);
        List<Object> newParams = new ArrayList<>();

        try {
            for (String item : params) {
                Object o = PropertyUtils.getProperty(oc, item);

                if (o != null) {
                    if (o.getClass().isArray()) {
//                        Object[] array = (Object[]) o;
                        //newParams.add(AppUtils.toSQLCompatibleArray(o.getClass(), array));
                        String[] test = {"1", "2", "3"};
                        newParams.add(test);
                    } else if (o.getClass().isEnum()) {
                        Identifiable aEnum = (Identifiable) o;
                        newParams.add(aEnum.getIdentifier());
                    } else {
                        newParams.add(o);
                    }
                } else {
                    newParams.add(null);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            Logger.getLogger(QueryMapperUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        // return the parameters as an Array of Objects
        return newParams.toArray(new Object[newParams.size()]);
    }

    /**
     *
     * @param query
     * @param oc
     * @param conn
     * @return
     */
    public static Object[] mapObjectForQuery(String query, Object oc, Connection conn) {
        List<String> params = findParamsInQuery(query);
        List<Object> newParams = new ArrayList<>();

        try {
            for (String item : params) {
                Object o = PropertyUtils.getProperty(oc, item);

                if (o != null) {
                    if (o.getClass().isArray()) {
                        Object[] array = (Object[]) o;
                        Array sqlArray = conn.createArrayOf(DatabaseUtils.getSqlTypeFromJavaType(o.getClass().getCanonicalName()), array);
                        newParams.add(sqlArray);
                    } else if (o.getClass().isEnum()) {
                        Identifiable aEnum = (Identifiable) o;
                        newParams.add(aEnum.getIdentifier());
                    } else {
                        newParams.add(o);
                    }
                } else {
                    newParams.add(null);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException |  SQLException | NoSuchMethodException ex ) {
            Logger.getLogger(QueryMapperUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        // return the parameters as an Array of Objects
        return newParams.toArray(new Object[newParams.size()]);
    }

    /**
     *
     * @param query
     * @return
     */
    private static List<String> findParamsInQuery(String query) {
        List<String> parameters = new ArrayList<>();
        String noBrackets = query.replaceAll("\\(", "( ");
        String[] vars = noBrackets.split(" ");
        for (String var : vars) {
            if (var.startsWith(":")) {
                String properVar = var.substring(1);
                properVar = properVar.replaceAll("[^a-zA-Z0-9]", "");
                if (properVar != null) {
                    parameters.add(properVar);
                }
            }
        }

        return parameters;
    }

    /**
     *
     * @param query
     * @param params
     * @return
     */
    private static String newQueryBuilder(String query, List<String> params) {
        for (String item : params) {
            String before = query;
            query = query.replace(":" + item + ",", "?,");
            if (query.equals(before)) {
                query = query.replace(":" + item + ")", "?)");
            }
            if (query.equals(before)) {
                query = query.replace(":" + item, "?");
            }
        }
        return query;
    }


}
