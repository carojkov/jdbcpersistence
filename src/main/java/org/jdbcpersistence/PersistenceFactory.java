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

import org.jdbcpersistence.impl.PersistenceImpl;

/**
 * This class is used for obtaining instances of <code>JDBCPersistence</code>.
 * <pre>
 * Example:
 * <code>
 *    JDBCPersistence jdbcPersistence = JDBCPersistenceFactory.getInstance().newJdbcPersistence();
 * </code>
 * </pre>
 * Once an instance of <code>JDBCPersistence</code> is obtained by an
 * application it is to be held on to by the application for the rest of
 * application's lifecycle.
 *
 * @see Persistence
 */
public final class PersistenceFactory
{
  private final static PersistenceFactory INSTANCE
    = new PersistenceFactory();

  /**
   * Returns a singleton instance of <code>JDBCPersistenceFactory</code>
   *
   * @return <code>JDBCPersistenceFactory</code> object
   */
  public static PersistenceFactory getInstance()
  {
    return INSTANCE;
  }

  /**
   * Returns a new, not configured, instance of <code>JDBCPersistence</code>
   *
   * @return <code>JDBCPersistence</code>object
   * @see Persistence#init(javax.sql.DataSource, java.util.Properties)
   */
  public Persistence newJdbcPersistence()
  {
    return new PersistenceImpl();
  }
}
