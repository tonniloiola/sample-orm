package com.example.demo.config;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.dao.DataAccessException;
import java.lang.reflect.ParameterizedType;

public abstract class SimpleDaoImpl<T> extends BaseAbstractDao implements SimpleDao<T> {

    private Class<T> typeParameterClass;

    {
        initClazz();
    }

    @SuppressWarnings("unchecked")
    private void initClazz() {
        this.typeParameterClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).
                getActualTypeArguments()[0];
    }

    /**
     * {@inheritDoc }
     *
     * @param object
     */
    @Override
    public void delete(T object) {
        StringWriter deleteWriter = new StringWriter();
//        List<Object> params = new ArrayList<>();
        String pkName = null;
        Object pkValue = null;

        // check if outputClass has 'Entity' annotation
        if (typeParameterClass.isAnnotationPresent(Entity.class)) {

            Entity[] entity = typeParameterClass.getDeclaredAnnotationsByType(Entity.class);
            deleteWriter.append("DELETE FROM " + entity[0].name() + " WHERE ");

            // get all the attributes of outputClass
            Field[] fields = typeParameterClass.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    // check if this is the primary member
                    if (column.primary() == true) {
                        try {
                            pkName = column.name();
                            pkValue = PropertyUtils.getProperty(object, field.getName());
                            deleteWriter.append(pkName + "=?");

                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                            logger.error("Could not parse Primary Key value from bean", ex);
                        }

                    }
                }
            }
        }
        // execute the query
        if (pkName != null && pkValue != null) {
            logger.info("RUNNING DELETE QUERY " + deleteWriter.toString());
            this.getJdbcTemplate().update(deleteWriter.toString(), pkValue);
        }

    }

    /**
     * {@inheritDoc }
     *
     * @param object
     * @return
     */
    @Override
    public Object persist(T object) {

        return this.persistObject(object, typeParameterClass);
    }

    /**
     * {@inheritDoc }
     *
     * @return
     */
    @Override
    public List<T> findAll() {
        String getAllQuery = buildFindAllQuery();
        if (getAllQuery != null) {
            @SuppressWarnings("unchecked")
			List<T> list = (List<T>) this.getJdbcTemplate().query(getAllQuery, new AutoResultSetExtractor<T>(typeParameterClass));
            return list = aggregateChildObjects(list, typeParameterClass);
        }
        return null;
    }

    /**
     * {@inheritDoc }
     *
     * @param value
     * @return
     */
    @Override
    public T findByPrimaryKey(Object value) {
        return this.findByPrimaryKey(value, typeParameterClass);
    }
    
    /**
     * Persist child objects that have to be persisted before the parent object
     *
     * @param object
     * @return
     */
    private Object persistRelationshipPreInsertion(Object object, Class<?> entityClass) {
        try {
            object = persistRelationships(object,entityClass, true, null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(SimpleDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    /**
     * Persist child objects that have to be persisted after the parent object
 with its parent primary member
     *
     * @param object
     * @param id
     */
    private void persistRelationshipPostInsertion(Object object, Object id, Class<?> entityClass) {
        try {
            persistRelationships(object, entityClass, false, id);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(SimpleDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param object
     * @param entityClass
     * @return
     */
    @SuppressWarnings("resource")
	private Object persistObject(Object object, Class<?> entityClass) {
        
        object = persistRelationshipPreInsertion(object, entityClass);
        
        StringWriter insertWriter = new StringWriter();
        StringWriter updateWriter = new StringWriter();
        List<Object> params = new ArrayList<>();
        String pkName = null;
        Object pkValue = null;

        // check if outputClass has 'Entity' annotation
        if (entityClass.isAnnotationPresent(Entity.class)) {

            Entity[] entity = entityClass.getDeclaredAnnotationsByType(Entity.class);
            insertWriter.append("INSERT INTO " + entity[0].name() + " (");
            updateWriter.append("UPDATE " + entity[0].name() + " SET ");

            // get all the attributes of outputClass
            Field[] fields = entityClass.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                if (field.isAnnotationPresent(Column.class)) {
                    try {
                        Column column = field.getAnnotation(Column.class);

                        // check if this is the primary member
                        if (column.primary() == true) {
                            pkName = column.name();
                            pkValue = PropertyUtils.getProperty(object, field.getName());

                        } else {
                            Object param = PropertyUtils.getProperty(object, field.getName());
                            if (param != null) {
                                insertWriter.append(column.name());
                                updateWriter.append(column.name() + "=?");
                                params.add(param);
                                // append a comma after each parameter we will clean this after
                                insertWriter.append(", ");
                                updateWriter.append(", ");
                            }

                        }

                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                        logger.error("Error generating automatic INSERT Query.", ex);
                    }
                }
            }

            // complete the insert query
            insertWriter.append(") VALUES (");
            insertWriter = appendQuestionMarks(insertWriter, params.size());
            if (pkName != null) {
                insertWriter.append("RETURNING " + pkName);
            }

            // complete the update query
            updateWriter.append(" WHERE " + pkName + " = ?");
        }

        // check if this is an INSERT or UPDATE
        Object record = findByPrimaryKey(pkValue);
        Long id = null;

        if (record != null) {
            id = doUpdate(updateWriter, params, pkValue);

        } else {
            id = doInsert(insertWriter, params);
        }
        
        if (id != null) {
            persistRelationshipPostInsertion(object, id, entityClass);
        }
        
        return id;
    }

    /**
     *
     * @param insertWriter
     * @param params
     * @return
     * @throws DataAccessException
     */
    @SuppressWarnings("deprecation")
	private Long doInsert(StringWriter insertWriter, List<Object> params) throws DataAccessException {
        Long id;
        String insert = QueryUtils.removeUnusedCommas(insertWriter.toString());
        logger.debug("INSERT QUERY " + insert);
        Object[] args = params.toArray(new Object[params.size()]);
        logger.debug("RUNNING INSERT WITH ARGS " + args.length);
        id = getJdbcTemplate().queryForLong(insert, args);
        return id;
    }

    /**
     *
     * @param updateWriter
     * @param params
     * @param pkValue
     * @return
     * @throws DataAccessException
     */
    private Long doUpdate(StringWriter updateWriter, List<Object> params, Object pkValue) throws DataAccessException {
        Long id;
        String query = QueryUtils.removeUnusedCommas(updateWriter.toString());
        logger.debug("UPDATE QUERY " + query);
        params.add(pkValue);
        Object[] args = params.toArray(new Object[params.size()]);
        logger.debug("RUNNING UPDATE WITH ARGS " + args.length);
        getJdbcTemplate().update(query, args);
        id = (Long) pkValue;
        return id;
    }

    /**
     *
     * @param pkToReturn
     * @param fields
     * @param writer
     */
    private StringWriter appendQuestionMarks(StringWriter writer, int paramSize) {
        // append the question marks for the parameters to be inserted
        for (int i = 0; i < paramSize; i++) {
            writer.append("?");
            if (i != paramSize - 1) {
                writer.append(", ");
            } else {
                writer.append(") ");
            }
        }
        return writer;
    }

    

    /**
     *
     * @param value
     * @param _typeParameterClass
     * @return
     */
    @SuppressWarnings("unchecked")
	public T findByPrimaryKey(Object value, Class<?> _typeParameterClass) {
        String getById = buildPrimaryKeyQuery(_typeParameterClass);
        if (getById != null) {
            List<T> list = (List<T>) this.getJdbcTemplate().query(getById, new AutoResultSetExtractor<T>(_typeParameterClass), value);
            list = aggregateChildObjects(list, _typeParameterClass);
            return (T) ((list != null) ? list.get(0) : list);
        }
        return null;
    }

    /**
     * {@inheritDoc }
     *
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
	@Override
    public T findByUniqueRef(String key, Object value) {
        List<T> list = this.findByRef(key, value, typeParameterClass);
        return (T) ((list != null) ? list.get(0) : list);
    }

    /**
     * {@inheritDoc }
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public List<T> findByRef(String key, Object value) {
        return this.findByRef(key, value, typeParameterClass);
    }

    /**
     *
     * @param key
     * @param value
     * @param _typeParameterClass
     * @return
     */
    @SuppressWarnings("unchecked")
	private List<T> findByRef(String key, Object value, Class<?> _typeParameterClass) {
        String getByRef = buildFindByRefQuery(key, _typeParameterClass);
        if (getByRef != null) {
            List<T> list = (List<T>) this.getJdbcTemplate().query(getByRef, new AutoResultSetExtractor<T>(_typeParameterClass), value);
            return aggregateChildObjects(list, _typeParameterClass);
        }
        return null;
    }

    /**
     *
     * @param _typeParameterClass
     * @return
     */
    private String buildPrimaryKeyQuery(Class<?> _typeParameterClass) {
        StringWriter writer = new StringWriter();

        if (_typeParameterClass.isAnnotationPresent(Entity.class)) {
            Entity[] entity = _typeParameterClass.getDeclaredAnnotationsByType(Entity.class);
            Field[] fields = _typeParameterClass.getDeclaredFields();

            // find the primary Key
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column.primary() == true) {
                        String pk = column.name();
                        // build query 
                        writer.append("SELECT * from " + entity[0].name() + " WHERE ");
                        writer.append(pk + "=?");
                    }
                }
            }
        }
        return writer.toString();
    }

    /**
     *
     * @param columnName
     * @return
     */
    private String buildFindByRefQuery(String columnName, Class<?> _typeParameterClass) {
        StringWriter writer = new StringWriter();

        if (_typeParameterClass.isAnnotationPresent(Entity.class)) {
            Entity[] entity = _typeParameterClass.getDeclaredAnnotationsByType(Entity.class);
            // build query 
            writer.append("SELECT * from " + entity[0].name() + " WHERE ");
            writer.append(columnName + "=?");
        }
        return writer.toString();
    }

    /**
     *
     * @return
     */
    private String buildFindAllQuery() {
        StringWriter writer = new StringWriter();

        if (typeParameterClass.isAnnotationPresent(Entity.class)) {
            Entity[] entity = typeParameterClass.getDeclaredAnnotationsByType(Entity.class);
            // build query 
            writer.append("SELECT * from " + entity[0].name());
        }
        return writer.toString();
    }

    /**
     * Find if the class has replationships, if so then it persist them
     *
     * @param object
     */
    @SuppressWarnings("unchecked")
	private Object persistRelationships(Object object, Class<?> entityClass, boolean pre, Object insertedId) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        // check if outputClass has 'Entity' annotation
        if (entityClass.isAnnotationPresent(Entity.class)) {

            // get all the attributes of outputClass
            Field[] fields = entityClass.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                if (field.isAnnotationPresent(Relationship.class)) {

                    // Find out what is the type of Object we will persist
                    Class<?> clazz = Class.forName(field.getType().getName());
                    Object child = null;
                    try {
                        child = PropertyUtils.getProperty(object, field.getName());
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        Logger.getLogger(SimpleDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (child != null) {
                        // get the name of the member property to update
                        Relationship relationship = field.getAnnotation(Relationship.class);
                        if (relationship.single() && pre) {

                            // persist the child Entity
                            Long targetValue = (Long) persistObject(child, clazz);

                            // check if this has a foreign member member
                            String targetMember = relationship.member();

                            // set the id of the related Entity through the "member" property of the Relationship Annotation
                            PropertyUtils.setProperty(object, targetMember, targetValue);
                        } else if (relationship.multiple() && !pre) {
                            String targetMember = relationship.member();

                            // persist all items in the list
                            List<Object> childAsList = (List<Object>) PropertyUtils.getProperty(object, field.getName());
                            for (Object item : childAsList) {
                                Class<?> itemClazz = Class.forName(item.getClass().getName());

                                PropertyUtils.setProperty(item, targetMember, insertedId);

                                // persist the child Entity
                                @SuppressWarnings("unused")
								Long targetValue = (Long) persistObject(item, itemClazz);
                            }
                        }

                    }
                }
            }
        }

        return object;
    }

    /**
     *
     * @param list
     * @return
     */
    private List<T> aggregateChildObjects(List<T> list, Class<?> _typeParameterClass) {

        if (list != null) {

            for (T item : list) {
                // find the Relationship member
                if (_typeParameterClass.isAnnotationPresent(Entity.class)) {

                    // get all the attributes of outputClass
                    Field[] fields = _typeParameterClass.getDeclaredFields();

                    for (int i = 0; i < fields.length; i++) {
                        final Field field = fields[i];
                        if (field.isAnnotationPresent(Relationship.class)) {
                            // get the name of the member property to update
                            Relationship relationship = field.getAnnotation(Relationship.class);

                            // check if this has a foreign member member
                            String targetMember = relationship.member();

                            try {

                                // Is 1-1 relationship, so we just get one Entity from the given foreign key
                                if (relationship.single()) {
                                    // set the id of the related Entity through the "member" property of the Relationship Annotation
                                    Object foreignKey = PropertyUtils.getProperty(item, targetMember);

                                    // callup a  find method for the child' object type
                                    Class<?> clazz = Class.forName(field.getType().getName());

                                    Object child = this.findByPrimaryKey(foreignKey, clazz);

                                    // set the result to the current item of the list
                                    PropertyUtils.setProperty(item, field.getName(), child);

                                    // is a 1-M relationship, so we have to find all the elements related to this Entity primary key
                                } else if (relationship.multiple()) {
                                    Object primaryKey = findPrimaryKey(item);
                                    Class<?> clazz = null;

                                    // with Lists it's a bit harder to find the Entity Class to work with
                                    java.lang.reflect.Type type = field.getGenericType();
                                    if (type instanceof java.lang.reflect.ParameterizedType) {
                                        java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
                                        clazz = Class.forName(pt.getActualTypeArguments()[0].getTypeName());
                                    }
                                    @SuppressWarnings("unchecked")
									List<Object> listOfChildern = (List<Object>) this.findByRef(relationship.column(), primaryKey, clazz);
                                    // set the result to the current item of the list
                                    PropertyUtils.setProperty(item, field.getName(), listOfChildern);
                                }

                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ex) {
                                logger.error(ex);
                            }
                        }
                    }
                }

            }
        }

        return list;
    }

    /**
     *
     * @param item
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private Object findPrimaryKey(T item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (typeParameterClass.isAnnotationPresent(Entity.class)) {
            Field[] fields = typeParameterClass.getDeclaredFields();

            // find the primary Key
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column.primary() == true) {
                        return PropertyUtils.getProperty(item, field.getName());
                    }
                }
            }
        }
        return null;
    }

}
