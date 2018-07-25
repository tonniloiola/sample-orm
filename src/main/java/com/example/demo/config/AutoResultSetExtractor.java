package com.example.demo.config;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

@SuppressWarnings("rawtypes")
public class AutoResultSetExtractor <T> implements ResultSetExtractor {
    
	private Class clazz;
    
    public AutoResultSetExtractor(Class outputClass){
        this.clazz  = outputClass;
    }
    
    @Override
    public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
        ResultSetMapper<T> resultSetMapper = new ResultSetMapper<T>();
        return resultSetMapper.mapRersultSetToObject(rs, clazz);
    }
     
    
    
    
}
