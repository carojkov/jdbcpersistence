/**
 * JDBCPersistence framework for java
 *   Copyright (C) 2004-2010 Alex Rojkov
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

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.List;

/**
 * The main interface of JDBCPersistence that provides methods to manipulate
 * underlying object representations in a database.
 * <pre>
 * Example:
 * <code>
 *    Connection conn = jdbcPersistence.getConnection();
 *    User user = new UserImpl();
 *    user.setid("the-unique-id");
 *    user.setName('the-unique-name");
 *    user.setFirstName("John");
 *    user.setLastName("Doe");
 *    conn.insert(user);
 * </code>
 * </pre>
 * <p/>
 *
 * @author Alex Rojkov Date: 21-Aug-2005 Time: 10:26:28 AM
 * @see Persistence#getConnection()
 * @see Query
 */
public interface Connection
  extends java.sql.Connection
{
  /**
   * Inserts data encapsulated by a persistent object into database.
   *
   * @param object object encapsulating the data to be inserted
   * @throws SQLException any <code>SQLException</code> that should occur
   *                      during the insert propogates up the call chain
   */
  public int insert(Object object)
    throws SQLException;

  /**
   * Inserts a batch of <code>Object</code>s into database. Uses batch
   * functionality provided by <code>java.sql.PreparedStatement</code>.
   *
   * @param batch array of <code>Object</code>s to insert
   * @return an array of update counts containing one element for each object
   *         in the batch. The elements of the array are ordered according to
   *         the order in which objects appeared in the batch.
   * @throws SQLException                  any <code>SQLException</code> that should occur
   *                                       during the insert propogates up the call chain
   * @throws java.sql.BatchUpdateException any <code>BatchUpdateException</code>
   *                                       that should occur during the insert propogates up the call chain
   * @see java.sql.PreparedStatement#executeBatch()
   */
  public int[] insert(Object... batch)
    throws SQLException, BatchUpdateException;

  /**
   * Updates an object's representation in database.
   *
   * @param object object a representation of which to update
   * @throws SQLException any <code>SQLException</code> that should occur
   *                      during the insert propogates up the call chain
   */
  public int update(Object object)
    throws SQLException;

  /**
   * Updates a batch of <code>Object</code>s in database. Uses batch
   * functionality provided by <code>java.sql.PreparedStatement</code>.
   *
   * @param batch array of <code>Object</code>s to update
   * @return an array of update counts containing one element for each object
   *         in the batch. The elements of the array are ordered according to
   *         the order in which objects appeared in the batch.
   * @throws SQLException         any <code>SQLException</code> that should occur
   *                              during the insert propogates up the call chain
   * @throws BatchUpdateException any <code>BatchUpdateException</code> that
   *                              should occur during the insert propogates up the call chain
   * @see java.sql.PreparedStatement#executeBatch()
   */
  public int[] update(Object... batch)
    throws SQLException, BatchUpdateException;

  /**
   * Deletes an object's representation in database.
   *
   * @param object an object a representation of which to delete
   * @throws SQLException any <code>SQLException</code> that should occur
   *                      during the insert propogates up the call chain
   */
  public int delete(Object object)
    throws SQLException;

  /**
   * Deletes a batch of <code>Object</code>s from database. Uses batch
   * functionality provided by <code>java.sql.PreparedStatement</code>.
   *
   * @param batch array of <code>Object</code>s to delete
   * @return an array of update counts containing one element for each object
   *         in the batch. The elements of the array are ordered according to
   *         the order in which objects appeared in the batch.
   * @throws SQLException         any <code>SQLException</code> that should occur
   *                              during the insert propogates up the call chain
   * @throws BatchUpdateException any <code>BatchUpdateException</code> that
   *                              should occur during the insert propogates up the call chain
   * @see java.sql.PreparedStatement#executeBatch()
   */
  public int[] delete(Object... batch)
    throws SQLException, BatchUpdateException;

  /**
   * Loads object's representation from database, creates an instance of
   * mapped object, sets it's fields and return the instance of mapped
   * object.
   *
   * @param primaryKey array of objects where each object represents a column
   *                   of a primary key in the order of columns in primary key
   * @return an instance of mapped entities with all the fields initialized with
   *         the values read from database or null if no such object was
   *         found
   * @throws SQLException any <code>SQLException</code> that should occur
   *                      during the insert propogates up the call chain
   */
  public <T> T load(Class<T> clazz, Object... primaryKey)
    throws SQLException;

  /**
   * Executes query and returns a <code>List</code> of results that might be a
   * collection of entities or a collection of <code>Map<code>s as specified by
   * the <code>query</code> object
   *
   * @param query  query that will be executed.
   * @param params parameters to the query, should be given in the order in
   *               which they are specified in the query
   * @param result an <code>java.util.List</code> object or null. If null is
   *               passed then an instance of an ArrayList will be created to hold the
   *               results.
   * @return <code>List</code> of entities or <code>Map</code> objects.
   * @throws SQLException any <code>SQLException</code> that should occur
   *                      during the insert propogates up the call chain
   * @see Query
   */
  public List executeQuery(Query query, List result, Object... params)
    throws SQLException;

  /**
   * Executes update and returns a number of rows affected by the update
   *
   * @param query  update that will be executed.
   * @param params parameters to the update statement, should be given in the
   *               order in which they are specified in the statement
   * @return <code>List</code> of entities or <code>Map</code> objects.
   * @throws SQLException any <code>SQLException</code> that should occur
   *                      during the insert propogates up the call chain
   * @see Query
   */
  public int executeUpdate(Query query, Object... params)
    throws SQLException;

  /**
   * Executes batch update and returns a number of rows affected by the
   * update
   *
   * @param query  <code>org.jdbcpersistence.jdbcQuery</code> object to
   *               execute.
   * @param params an array of arrays of parameters to the jdbcQuery
   * @return array of int indicating the number of records updated
   * @throws SQLException         any <code>SQLException</code> that should occur
   *                              during the insert propogates up the call chain
   * @throws BatchUpdateException any <code>BatchUpdateException</code> that
   *                              should occur during the insert propogates up the call chain
   * @see java.sql.PreparedStatement#executeBatch()
   */
  public int[] executeBatchUpdate(Query query, Object[]... params)
    throws SQLException, BatchUpdateException;
}
