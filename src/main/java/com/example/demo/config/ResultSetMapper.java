package com.example.demo.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;

public class ResultSetMapper<T> {

    private final static Logger logger = Logger.getLogger(ResultSetMapper.class.getName());

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<T> mapRersultSetToObject(ResultSet rs, Class outputClass) {
        List<T> outputList = null;
        try {
            // make sure resultset is not null
            if (rs != null) {
                // check if outputClass has 'Entity' annotation
                if (outputClass.isAnnotationPresent(Entity.class)) {
                    // get the resultset metadata
                    ResultSetMetaData rsmd = rs.getMetaData();
                    // get all the attributes of outputClass
                    Field[] fields = outputClass.getDeclaredFields();
                    while (rs.next()) {
                        T bean = (T) outputClass.newInstance();
                        for (int _iterator = 0; _iterator < rsmd.getColumnCount(); _iterator++) {
                            // getting the SQL column name
                            String columnName = rsmd.getColumnName(_iterator + 1);                            
                            // reading the value of the SQL column
                            Object columnValue = rs.getObject(_iterator + 1);
                            // iterating over outputClass attributes to check if any attribute has 'Column' annotation with matching 'name' value
                            for (Field field : fields) {
                                if (field.isAnnotationPresent(Column.class)) {
                                    Column column = field.getAnnotation(Column.class);
                                    if (column.name().equalsIgnoreCase(columnName) && columnValue != null) {                  
                                        if(field.getType().isEnum()){ 
                                            Class clazz = field.getType();
                                            BeanUtils.setProperty(bean, field.getName(), Enum.valueOf(clazz, columnValue.toString()));
                                            
                                        }else{
                                            BeanUtils.setProperty(bean, field.getName(), columnValue);                                        
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        if (outputList == null) {
                            outputList = new ArrayList<T>();
                        }
                        outputList.add(bean);
                    }

                } else {
                    // throw some error
                }
            } else {
                return null;
            }
        } catch ( InvocationTargetException | IllegalAccessException | InstantiationException | SQLException e) {
            logger.log(Level.SEVERE, "Erro Mapping Resultset to Pojo.", e);       
        }
        return outputList;
    }
}
