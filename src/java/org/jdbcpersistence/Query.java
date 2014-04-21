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
package org.jdbcpersistence;

/**
 * JDBCQuery object encapsulates a Data Manipulation Language SQL statement. It
 * is used to run query or update an underlying database. <BR> When the query is
 * a select statement the results can be returned ad <code>List</code> of
 * <code>Map</code>s or a <code> List</code> of entities. This is controlled by the
 * <code>resultType</code> argument passed into the query. <BR> When
 * <code>resultType</code> argument to the query is <ul> <li>
 * <code>java.utils.Map.class</code> the result will be a <code>List</code> of
 * <code>Map</code>s. </li> <li> <code>java.utils.List</code> the result will be
 * a list, where each item is an object returned by ResultSet.getObject(1)
 * method. </li> <li> one of the classes registered with
 * <code>JDBCPersistence</code> a <code>List</code> of instances of that class.
 * </li> </ul> <BR> JDBCQuery will use fine-tuned to a particular database SQL
 * statements when they are supplied. They can be supplied via registering
 * statement with <code>JDBCQuery</code> using <code>addFlavour</code> method.
 * <BR><BR>
 *
 * @author Alex Rojkov Date: 21-Aug-2005 Time: 9:19:05 PM
 * @see Connection#executeBatchUpdate(Query, Object[]...)
 * @see Connection#executeQuery(Query, java.util.List, Object...)
 * @see Connection#executeUpdate(Query, Object[])
 * @see Persistence
 */
public class Query<T>
{
  protected String _query;
  private final Class _resultType;
  private final Class _jdbcQueryReaderClass;
  private final boolean _nullable;

  /**
   * Constructor that accepts a default SQL statement and no resultType.
   * Typically this constructor would be used when the instance of
   * <code>JDBCQuery</code> being created will be used to update database
   *
   * @param query SQL statement
   */
  public Query(String query)
  {
    this(query, null, null, false);
  }

  /**
   * Constructor that accepts a default SQL statement and a resultType. This
   * constructor should be used when creating a select type of query. The
   * parameter <code>resultType</code> denotes what result the query will
   * return.
   *
   * @param query                SQL statement
   * @param resultType           regitered persistent class or <code>java.utils.Map</code>
   * @param jdbcQueryReaderClass
   */
  public Query(String query, Class resultType, Class jdbcQueryReaderClass)
  {
    this(query, resultType, jdbcQueryReaderClass, false);
  }

  /**
   * Constructor that accepts a default SQL statement and a resultType. This
   * constructor should be used when creating a select type of query. The
   * parameter <code>resultType</code> denotes what result the query will
   * return.
   *
   * @param query                SQL statement
   * @param resultType           regitered persistent class or <code>java.utils.Map</code>
   * @param jdbcQueryReaderClass
   * @param nullable             when true, query String is changed by placing IS NULL for
   *                             null values in params @see executeQuery
   */
  public Query(String query,
               Class resultType,
               Class jdbcQueryReaderClass,
               boolean nullable)
  {
    this._query = query;
    this._resultType = resultType;
    this._jdbcQueryReaderClass = jdbcQueryReaderClass;
    this._nullable = nullable;
  }

  /**
   * Returns <code>Class</code> associated with this <code>JDBCQuery</code>
   *
   * @return Class
   */
  public Class getResultType()
  {
    return _resultType;
  }

  /**
   * Returns query string
   *
   * @return query string
   */
  public String getQuery()
  {
    return _query;
  }

  /**
   * @return Class
   */
  public Class getJdbcResultSetReader()
  {
    return _jdbcQueryReaderClass;
  }

  public boolean isNullable()
  {
    return _nullable;
  }

  public String toString()
  {
    return "JDBCQuery[" + _query + ", " + _nullable + "] ";
  }
}
