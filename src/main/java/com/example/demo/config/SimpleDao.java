package com.example.demo.config;

import java.util.List;

public interface SimpleDao<T> {

    /**
     * Persist a given Pojo,
     *
     * @param object is the pojo to be persisted
     * @return an Object representing the primary key returned from the
     * insertion
     */
    public Object persist(T object);

    /**
     * Delete a given Pojo,
     *
     * @param object is the pojo to be deleted *
     */
    public void delete(T object);

    /**
     * Provides with a full List of Pojos from the table
     * @return 
     */
    public List<T> findAll();

    /**
     * Provides with a Pojo looked up by its given primary Key
     *
     * @param value is the value of the primary key
     * @return a deserialised Pojo
     */
    public T findByPrimaryKey(Object value);

    /**
     * Provides with a Pojo looked up by a given key field and value.
     * A key field is supposed to be unique so it will only return one result.
     * 
     * @param key
     * @param value
     * @return 
     */
    public T findByUniqueRef(String key, Object value);
    
    /**
     * Provides with a list of Pojo looked up by a given key field and value.
     * 
     * @param key
     * @param value
     * @return 
     */
    public List<T> findByRef(String key, Object value);
}
