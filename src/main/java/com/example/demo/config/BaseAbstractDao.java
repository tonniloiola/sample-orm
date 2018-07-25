package com.example.demo.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public abstract class BaseAbstractDao extends NamedParameterJdbcDaoSupport {

    private String selectSql = "SELECT nextval(?)";
    protected String sequence;
    @Autowired
    
    @Qualifier("dataSource")
    protected DataSource dataSource;

    @PostConstruct
    void init() {
        setDataSource(dataSource);
    }

    @SuppressWarnings("deprecation")
	protected synchronized Long getNextId(String sequence) {
        return this.getJdbcTemplate().queryForLong(selectSql, sequence);
    }

    protected synchronized Long getNextId() {
        return this.getNextId(sequence);
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    protected static synchronized Date mapDate(String name, ResultSet rs, int row) throws SQLException {
        return rs.getObject(name) != null ? new Date(rs.getTimestamp(name).getTime()) : null;
    }
}
