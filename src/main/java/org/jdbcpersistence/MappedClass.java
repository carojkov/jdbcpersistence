package org.jdbcpersistence;

import java.lang.reflect.Method;

public interface MappedClass<T>
{
  String getSchema();

  MappedAttribute getColumn(String columnName);

  MappedAttribute[] getRegularColumns();

  MappedAttribute[] getColumns();

  MappedAttribute[] getIdentifyingColumns();

  MappedAttribute getVersionControlColumn();

  Class<T> getMappedClass();

  Class getPersistorClass();

  String getTableName();

  public interface MappedAttribute<T>
  {
    /**
     * Returns a name for the mapped column
     *
     * @return name
     */
    public String getName();

    /**
     * Returns order of the column in an identifying key
     *
     * @return value of 0 or greater if the column participates in an
     * identifying key, value of -1 is returned if the column does
     * not part of an identifying key
     */
    public int getIdentifyingOrder();

    /**
     * Sets order of the column in an identifying key
     *
     * @param identifyingOrder order of column in the key
     * @return <code>this</code> instance
     */
    public MappedAttribute setIdentifyingOrder(int identifyingOrder);

    /**
     * Tests if the column is part of an identifying key
     *
     * @return true when it is, and false when it is not
     */
    public boolean isIdentifying();

    /**
     * Tests if the column is used to track version of the data
     *
     * @return true if it is, false otherwise
     */
    public boolean isVersionControlColumn();

    /**
     * Designates the <code>Column</code> to be used for tracking version of
     * data
     *
     * @param versionControlColumn
     * @return <code>this</code> instance
     */
    public MappedAttribute setVersionControlColumn(boolean versionControlColumn);

    /**
     * Returns a getter method of the mapped class for this column
     *
     * @return a getter method for a property of a mapped entities
     */
    public Method getGetter();

    /**
     * Sets a getter method of the mapped class for this column
     *
     * @param getter the getter method
     * @return <code>this</code> instance
     */
    public MappedAttribute setGetter(Method getter);

    /**
     * Returns a setter method of the mapped class for this column
     *
     * @return a getter method for a property of a mapped entities
     */
    public Method getSetter();

    /**
     * Sets a getter method for mapped to this column entities's property
     *
     * @param setter method for the entities's property
     * @return <code>this</code> instance
     */
    public MappedAttribute setSetter(Method setter);

    /**
     * Tests for existance of a getter method for a property of a mapped
     * entities
     *
     * @return true if a getter method exists, false otherwise
     */
    public boolean hasGetter();

    /**
     * Test for existance of a setter method for a property of a mapped
     * entities
     *
     * @return true if a setter method exists, false otherwise
     */
    public boolean hasSetter();

    /**
     * Retuns SQL type of the column as specified in <code>java.sql.Types</code>
     * interface
     *
     * @return an integer representing SQL type
     */
    public int getSqlType();

    /**
     * Sets SQL type for the column
     *
     * @param sqlType the SQL Type
     * @return <code>this</code> instance
     */
    public MappedAttribute setSqlType(int sqlType);

    /**
     * Returns an order of the column in DDL as returned by
     * <code>java.sql.DatabaseMetaData</code>
     *
     * @return an integer reprenting order
     */
    public int getDdlOrder();

    /**
     * Sets an order of the column in DDL statement
     *
     * @param ddlOrder the order in DDL
     * @return an integer reprenting order
     */
    public MappedAttribute setDdlOrder(int ddlOrder);

    /**
     * Tests if the column accepts null values
     *
     * @return true when the column is nullable, false otherwise
     */
    public boolean isNullable();

    /**
     * Assignes nullability to <code>Column</code>
     *
     * @param nullable
     * @return <code>this</code> instance
     */
    public MappedAttribute setNullable(boolean nullable);

    public void init(Column columnAnn);

  }
}
