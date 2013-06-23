package org.jdbcpersistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * An interface that is used by the <code>org.jdbcpersistence.JDBCQuery</code>
 * to read data from a <code>java.sql.ResultSet</code> returned by that
 * <code>JDBCQuery</code> User: alex Date: Aug 23, 2006 Time: 9:00:37 PM
 *
 * @see Query
 */
public interface ResultSetReader<T> {
  /**
   * Reads data from the result set and creates an instance of mapped entities for
   * each row. The entities instances are then packaged into an instance of
   * <code>java.utils.List</code> which is then returned
   *
   * @param rs     an instance of <code>java.sql.ResultSet</code>
   * @param result reference to null or an instance of <code>java.util.List</code>
   *               that will be populated and returned
   * @return a result <code>java.utils.List</code> of entities
   * @throws SQLException any <code>SQLException</code> that should occur
   *                      during the insert propogates up the call chain
   */
  List<T> read(ResultSet rs, List<T> result)
    throws SQLException;
}
