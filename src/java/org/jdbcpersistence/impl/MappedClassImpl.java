/**
 * JDBCPersistence framework for java
 *   Copyright (C) 2004-2014 Alex Rojkov
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    You can contact me by email jdbcpersistence   a t   gmail    d o t    com
 * */
package org.jdbcpersistence.impl;

import org.jdbcpersistence.Column;
import org.jdbcpersistence.Entity;
import org.jdbcpersistence.Id;
import org.jdbcpersistence.MappedClass;
import org.jdbcpersistence.Persistor;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The MappedClass class associates ('maps') a entities with a table and
 * <code>Persistor</code>.
 * <pre>
 * Example:
 * <code>
 *    MappedClass jdbcMap = jdbcPersistence.registerPersistent(FooBean.class,
 * "FOO_TABLE", null);
 * </code>
 * </pre>
 *
 * @see org.jdbcpersistence.Persistence#register(Class
 * @see org.jdbcpersistence.MappedClass.MappedAttribute
 */
final class MappedClassImpl<T> implements MappedClass<T>
{
  final static Map<Class,Integer> typesMap = new HashMap<Class,Integer>();

  private final Class<T> _mappedClass;
  private Class<? extends Persistor> _persistorClass;
  private MappedAttribute[] _columns = null;
  private MappedAttribute[] _regularColumns = null;
  private MappedAttribute[] _identifyingColumns = null;
  private MappedAttribute _versionControlColumn = null;
  private Entity _entity;

  public MappedClassImpl(final Class<T> clazz, Entity entity)
  {
    _mappedClass = clazz;
    _entity = entity;

    init();
  }

  private void init()
  {
    if (_entity == null)
      return;

    _persistorClass = _entity.persistor();

    Class cl = _mappedClass;

    Map<String,Method> getters = new HashMap<String,Method>();
    Map<String,Method> setters = new HashMap<String,Method>();

    do {
      Method[] methods = cl.getMethods();

      for (Method method : methods) {
        if (Object.class.equals(method.getDeclaringClass()))
          continue;

        String methodName = method.getName();

        if (methodName.startsWith("is")) {
          getters.put(methodName.substring(2), method);
        }
        else if (methodName.startsWith("get")) {
          getters.put(methodName.substring(3), method);
        }
        else if (methodName.startsWith("write")) {
          getters.put(methodName.substring(5), method);
        }
      }

      for (Method method : methods) {
        if (Object.class.equals(method.getDeclaringClass()))
          continue;

        String methodName = method.getName();

        if (methodName.startsWith("set")) {
          setters.put(methodName.substring(3), method);
        }
        else if (methodName.startsWith("read")) {
          setters.put(methodName.substring(4), method);
        }
      }
    } while ((cl = cl.getSuperclass()) != null && !Object.class.equals(cl));

    Set<String> properties = new HashSet<String>();
    properties.addAll(getters.keySet());
    properties.addAll(setters.keySet());

    _columns = new MappedAttribute[properties.size()];

    int i = 0;
    for (Iterator<String> iterator = properties.iterator();
         iterator.hasNext(); ) {
      final String property = iterator.next();

      Method getter = getters.get(property);
      Method setter = setters.get(property);

      Class setterType = null;
      Class getterType = null;
      if (setter != null)
        setterType = setter.getParameterTypes()[0];

      if (getter != null)
        getterType = getter.getReturnType();

      if (void.class.equals(getterType)
          && getter.getParameterTypes().length == 1)
        getterType = getter.getParameterTypes()[0];

      if (Writer.class.equals(getterType)
          && Reader.class.equals(setterType)) {
      }
      else if (setterType != null
               && getterType != null
               && !getterType.isAssignableFrom(setterType)) {
        throw new IllegalArgumentException("getter "
                                           + getter
                                           + " is not compatible with setter "
                                           + setter);
      }

      Column columnAnn = null;

      if (getter != null)
        columnAnn = getter.getAnnotation(Column.class);

      if (columnAnn == null && setter != null) {
        columnAnn = setter.getAnnotation(Column.class);
      }
      else if (setter != null
               && setter.getAnnotation(Column.class)
                  != null) {
        throw new IllegalArgumentException(
          "@Column can be declared either on getter or setter: " + setter);
      }

      String columnName = null;

      if (columnAnn != null && !columnAnn.name().isEmpty())
        columnName = columnAnn.name();

      if (columnName == null)
        columnName = property.toUpperCase();

      MappedAttribute column = new MappedAttributeImpl(columnName);

      column.setGetter(getters.get(property));
      column.setSetter(setters.get(property));

      Id idAnn = null;

      if (getter != null)
        idAnn = getter.getAnnotation(Id.class);

      if (idAnn == null && setter != null)
        idAnn = setter.getAnnotation(Id.class);
      else if (setter != null && setter.getAnnotation(Id.class) != null)
        throw new IllegalArgumentException(
          "@Id can be declared either on getter or setter: " + setter);

      if (idAnn != null)
        column.setIdentifyingOrder(idAnn.value());

      boolean isVersion = columnAnn != null && columnAnn.version();

      if ((getter != null || setter != null) && isVersion)
        column.setVersionControlColumn(true);

      if (columnAnn != null)
        column.init(columnAnn);

      _columns[i++] = column;
    }

    int pkSize = 0;
    int recordSize = 0;

    for (MappedAttribute column : _columns) {
      if (column.isIdentifying())
        pkSize++;
      else
        recordSize++;
    }

    _identifyingColumns = new MappedAttribute[pkSize];
    _regularColumns = new MappedAttribute[recordSize];
    i = 0;
    for (MappedAttribute column : _columns) {
      int id = column.getIdentifyingOrder();

      if (id == -1) {
        _regularColumns[i++] = column;
      }
      else if (id + 1 > pkSize) {
        throw new IllegalArgumentException("@Id.value (" + id + ") for column '"
                                           + column.getName()
                                           + "' must be less than number of columns in primary key");
      }
      else if (_identifyingColumns[id] != null) {
        throw new IllegalArgumentException("columns "
                                           + _identifyingColumns[id].getName()
                                           + " and "
                                           + column.getName()
                                           + " must have different @Id.value");
      }

      else {
        _identifyingColumns[id] = column;
      }
    }
  }

  /**
   * Returns the name of the underlying schema
   *
   * @return name of the underlying schema
   */
  @Override
  public String getSchema()
  {
    return _entity.schema();
  }

  /**
   * Returns <code>MappedClass.MappedAttribute</code> object mapped to the supplied in the
   * argument columnName
   *
   * @param columnName
   * @return Column
   */
  @Override
  public MappedAttribute getColumn(final String columnName)
  {
    for (MappedAttribute column : _columns) {
      if (columnName.equals(column.getName()))
        return column;
    }

    return null;
  }

  /**
   * Returns an unordered array of <b>unidentifying</b> columns mapped into
   * <code>this</code> instance of <code>MappedClass</code>
   *
   * @return an array of <code>MappedClass.MappedAttribute</code>
   */
  @Override
  public MappedAttribute[] getRegularColumns()
  {
    if (_regularColumns != null)
      return _regularColumns;

    List temp = new ArrayList();
    MappedAttribute[] columns = getColumns();
    for (int i = 0; i < columns.length; i++) {
      MappedAttribute column = columns[i];
      if (column.isIdentifying()) continue;
      temp.add(column);
    }
    _regularColumns = new MappedAttribute[temp.size()];
    temp.toArray(_regularColumns);

    return _regularColumns;
  }

  /**
   * Retuns an unordered array of all columns mapped into <code>this</code>
   * instance of <code>MappedClass</code>
   *
   * @return an array of <code>MappedClass.MappedAttribute</code>
   */
  @Override
  public MappedAttribute[] getColumns()
  {
    return _columns;
  }

  /**
   * Returns an unordered array of <b>identifying</b> columns mapped into
   * <code>this</code> instance of <code>MappedClass</code>
   *
   * @return an array of <code>MappedClass.MappedAttribute</code>
   */
  @Override
  public MappedAttribute[] getIdentifyingColumns()
  {
    return _identifyingColumns;
  }

  /**
   * Returns <code>MappedClass.MappedAttribute</code> that performs function of tracking
   * version information
   *
   * @return a <code>MappedClass.MappedAttribute</code>
   */
  @Override
  public MappedAttribute getVersionControlColumn()
  {
    if (_versionControlColumn == null) {
      MappedAttribute[] columns = getRegularColumns();
      for (int i = 0; i < columns.length; i++) {
        MappedAttribute column = columns[i];
        if (column.isVersionControlColumn()) {
          _versionControlColumn = column;
          break;
        }
      }
    }

    return _versionControlColumn;
  }

  /**
   * Returns <code>Class<code> of the associated entities, abstract class or an
   * interface
   *
   * @return mapped class
   */
  @Override
  public final Class<T> getMappedClass()
  {
    return _mappedClass;
  }

  /**
   * Returns <code>Class</code> responsible for persistence of an associated
   * entities
   *
   * @return a class implementing Persistor if one was supplied or null
   */
  @Override
  public Class getPersistorClass()
  {
    return _persistorClass;
  }

  /**
   * Returns name of the associated table
   *
   * @return name of table
   */
  @Override
  public String getTableName()
  {
    return _entity.name();
  }

  /**
   * Class <code>MappedClass.MappedAttribute</code> is used to configure and hold mapping
   * information for a database column.
   */
  final class MappedAttributeImpl implements MappedAttribute<T>
  {
    private String _name;
    private int _identifyingOrder = -1;
    private boolean _versionControlColumn = false;
    private Method _getter;
    private Method _setter;
    private int _sqlType = Integer.MIN_VALUE;
    private int _ddlOrder = -1;
    private boolean _isNullable = true;

    private MappedAttributeImpl(String name)
    {
      this._name = name;
    }

    /**
     * Returns a name for the mapped column
     *
     * @return name
     */
    public String getName()
    {
      return _name;
    }

    /**
     * Returns order of the column in an identifying key
     *
     * @return value of 0 or greater if the column participates in an
     * identifying key, value of -1 is returned if the column does
     * not part of an identifying key
     */
    public int getIdentifyingOrder()
    {
      return _identifyingOrder;
    }

    /**
     * Sets order of the column in an identifying key
     *
     * @param identifyingOrder order of column in the key
     * @return <code>this</code> instance
     */
    public MappedAttribute setIdentifyingOrder(int identifyingOrder)
    {
      this._identifyingOrder = identifyingOrder;
      return this;
    }

    /**
     * Tests if the column is part of an identifying key
     *
     * @return true when it is, and false when it is not
     */
    public boolean isIdentifying()
    {
      return _identifyingOrder != -1;
    }

    /**
     * Tests if the column is used to track version of the data
     *
     * @return true if it is, false otherwise
     */
    public boolean isVersionControlColumn()
    {
      return _versionControlColumn;
    }

    /**
     * Designates the <code>Column</code> to be used for tracking version of
     * data
     *
     * @param versionControlColumn
     * @return <code>this</code> instance
     */
    public MappedAttribute setVersionControlColumn(boolean versionControlColumn)
    {
      this._versionControlColumn = versionControlColumn;

      return this;
    }

    /**
     * Returns a getter method of the mapped class for this column
     *
     * @return a getter method for a property of a mapped entities
     */
    public Method getGetter()
    {
      return _getter;
    }

    /**
     * Sets a getter method of the mapped class for this column
     *
     * @param getter the getter method
     * @return <code>this</code> instance
     */
    public MappedAttribute setGetter(Method getter)
    {
      this._getter = getter;

      return this;
    }

    /**
     * Returns a setter method of the mapped class for this column
     *
     * @return a getter method for a property of a mapped entities
     */
    public Method getSetter()
    {
      return _setter;
    }

    /**
     * Sets a getter method for mapped to this column entities's property
     *
     * @param setter method for the entities's property
     * @return <code>this</code> instance
     */
    public MappedAttribute setSetter(Method setter)
    {
      this._setter = setter;

      return this;
    }

    /**
     * Tests for existance of a getter method for a property of a mapped
     * entities
     *
     * @return true if a getter method exists, false otherwise
     */
    public boolean hasGetter()
    {
      return _getter != null;
    }

    /**
     * Test for existance of a setter method for a property of a mapped
     * entities
     *
     * @return true if a setter method exists, false otherwise
     */
    public boolean hasSetter()
    {
      return _setter != null;
    }

    /**
     * Retuns SQL type of the column as specified in <code>java.sql.Types</code>
     * interface
     *
     * @return an integer representing SQL type
     */
    public int getSqlType()
    {
      return _sqlType;
    }

    /**
     * Sets SQL type for the column
     *
     * @param sqlType the SQL Type
     * @return <code>this</code> instance
     */
    public MappedAttribute setSqlType(int sqlType)
    {
      this._sqlType = sqlType;

      return this;
    }

    /**
     * Returns an order of the column in DDL as returned by
     * <code>java.sql.DatabaseMetaData</code>
     *
     * @return an integer reprenting order
     */
    public int getDdlOrder()
    {
      return _ddlOrder;
    }

    /**
     * Sets an order of the column in DDL statement
     *
     * @param ddlOrder the order in DDL
     * @return an integer reprenting order
     */
    public MappedAttribute setDdlOrder(int ddlOrder)
    {
      this._ddlOrder = ddlOrder;

      return this;
    }

    /**
     * Tests if the column accepts null values
     *
     * @return true when the column is nullable, false otherwise
     */
    public boolean isNullable()
    {
      return _isNullable;
    }

    /**
     * Assignes nullability to <code>Column</code>
     *
     * @param nullable
     * @return <code>this</code> instance
     */
    public MappedAttribute setNullable(boolean nullable)
    {
      _isNullable = nullable;

      return this;
    }

    public void init(Column columnAnn)
    {
      initSqlType(columnAnn);
    }

    private void initSqlType(Column columnAnn)
    {
      if (columnAnn.sqlType() > Integer.MIN_VALUE) {
        _sqlType = columnAnn.sqlType();

        return;
      }

      Class type = null;
      if (_setter != null)
        type = _setter.getParameterTypes()[0];

      if (type == null && _getter != null)
        type = _getter.getReturnType();

      Integer sqlType = typesMap.get(type);

      _sqlType = sqlType.intValue();
    }

    public String toString()
    {
      return "MappedClassImpl.MappedAttribute[" + _name + "]";
    }
  }

  static {
    typesMap.put(boolean.class, Types.BOOLEAN);
    typesMap.put(Boolean.class, Types.BOOLEAN);

    typesMap.put(byte.class, Types.TINYINT);
    typesMap.put(Byte.class, Types.TINYINT);

    typesMap.put(short.class, Types.SMALLINT);
    typesMap.put(Short.class, Types.SMALLINT);

    typesMap.put(char.class, Types.INTEGER);
    typesMap.put(Character.class, Types.INTEGER);

    typesMap.put(int.class, Types.INTEGER);
    typesMap.put(Integer.class, Types.INTEGER);

    typesMap.put(long.class, Types.BIGINT);
    typesMap.put(Long.class, Types.BIGINT);

    typesMap.put(float.class, Types.FLOAT);
    typesMap.put(Float.class, Types.FLOAT);

    typesMap.put(double.class, Types.DOUBLE);
    typesMap.put(Double.class, Types.DOUBLE);

    typesMap.put(byte[].class, Types.BINARY);
    typesMap.put(char[].class, Types.CLOB);

    typesMap.put(java.util.Date.class, Types.DATE);
    typesMap.put(java.sql.Date.class, Types.DATE);

    typesMap.put(java.sql.Time.class, Types.TIME);
    typesMap.put(Timestamp.class, Types.TIMESTAMP);

    typesMap.put(BigDecimal.class, Types.DECIMAL);
    typesMap.put(String.class, Types.VARCHAR);
  }
}
