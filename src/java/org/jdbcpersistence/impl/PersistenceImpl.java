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

import org.jdbcpersistence.Connection;
import org.jdbcpersistence.*;
import org.jdbcpersistence.impl.gen.JDBCPersistorGenerator;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PersistenceImpl implements Persistence
{
  public static final String DB_ORACLE = "oracle";
  //
  private static Logger log
    = Logger.getLogger(PersistenceImpl.class.getName());

  private static byte[] DUMMY_BYTES = new byte[0];

  private HashMap<Class,MappedClass> _jdbcMaps = new HashMap<>();
  private boolean _initialized = false;
  private String _dbname = null;
  private boolean _locatorsUpdateCopy = false;

  private final Map<Class,Persistor> _jdbcPersistors = new HashMap<>();

  private final Map<Class,Class> _abstractBeanImplementations
    = new HashMap<>();

  private final Map<Class,byte[]> _abstractBeanBytes
    = new HashMap<>();

  private final Map _jdbcQueryResultReaders = new HashMap();
  //
  ConnectionProvider _connectionProvider = null;
  private PersistenceClassLoader _cl = null;

  public PersistenceImpl()
  {
    try {
      _cl
        = new PersistenceClassLoader(Thread.currentThread()
                                           .getContextClassLoader());
    } catch (Throwable e) {
      _cl
        = new PersistenceClassLoader(PersistenceImpl.class.getClassLoader());
    }
  }

  ResultSetReader getJdbcQueryResultReader(Query query)
  {
    ResultSetReader
      resultSetReader
      = (ResultSetReader) _jdbcQueryResultReaders.get(query.getQuery());
    return resultSetReader;
  }

  public ResultSetReader getJdbcQueryResultReader(Query query,
                                                  ResultSetMetaData rsMeta)
  {
    String q = query.getQuery();

    ResultSetReader
      resultSetReader
      = (ResultSetReader) _jdbcQueryResultReaders.get(q);

    if (resultSetReader == null) {
      synchronized (_jdbcQueryResultReaders) {
        resultSetReader = (ResultSetReader) _jdbcQueryResultReaders.get(q);
        if (resultSetReader == null) {
          Class resultSetReaderClass = query.getJdbcResultSetReader();
          try {
            if (resultSetReaderClass == null) {
              Class queryResultType = query.getResultType();
              Class beanClass = _abstractBeanImplementations.get(
                queryResultType);
              if (beanClass == null) {
                prepare(queryResultType);
                beanClass = _abstractBeanImplementations.get(
                  queryResultType);
              }
              MappedClass jdbcMap = _jdbcMaps.get(queryResultType);
              String[] columnNames = SQLUtils.getColumns(rsMeta);
              resultSetReaderClass
                = PersistorGenerator.generateResultSetReader(
                beanClass,
                jdbcMap,
                columnNames,
                q,
                false,
                false,
                _cl);
            }
            resultSetReader
              = (ResultSetReader) resultSetReaderClass.newInstance();
            _jdbcQueryResultReaders.put(q, resultSetReader);
          } catch (RuntimeException e) {
            throw e;
          } catch (Throwable t) {
            throw new RuntimeException(t);
          }
        }
      }
    }
    return resultSetReader;
  }

  <T> Persistor<T> getJdbcPersistor(final Class<T> clazz)
  {
    Persistor jdbcPersistor = (Persistor) _jdbcPersistors.get(clazz);
    if (jdbcPersistor == null) {
      synchronized (this) {
        jdbcPersistor = (Persistor) _jdbcPersistors.get(clazz);
        if (jdbcPersistor == null) {
          prepare(clazz);
        }
      }
      jdbcPersistor = (Persistor) _jdbcPersistors.get(clazz);
      return jdbcPersistor;
    }
    return jdbcPersistor;
  }

  public void unprepare()
  {
    _jdbcPersistors.clear();
    _abstractBeanImplementations.clear();
    _abstractBeanBytes.clear();
    _jdbcQueryResultReaders.clear();
  }

  void prepare(final Class clazz)
  {
    Connection conn = null;
    DatabaseMetaData dbMetaData = null;
    try {
      conn = getConnection();
      dbMetaData = conn.getMetaData();
      if (!_initialized) {
        try {
          //this.locatorsUpdateCopy = dbMetaData.locatorsUpdateCopy();
          this._locatorsUpdateCopy = false;
        } catch (AbstractMethodError e) {
          //probably Oracle...so default value is fine
        }
        _dbname = dbMetaData.getDatabaseProductName().toLowerCase().intern();
      }
      MappedClass jdbcMap = (MappedClass) _jdbcMaps.get(clazz);
      if (jdbcMap == null) {
        throw new RuntimeException("Class " +
                                   clazz +
                                   " has no mapping. Please register the class.");
      }

      synchronized (this) {
        Persistor jdbcPersistor;
        try {
          Class classImpl = clazz;
          if (clazz.isInterface() ||
              Modifier.isAbstract(clazz.getModifiers())) {
            BeanGenerator beanGenerator = new BeanGenerator(clazz);
            byte[] classBytes = beanGenerator.generate();
            String name = beanGenerator.getBeanClassNameDotted();
            classImpl = _cl.define(name, classBytes);
            _abstractBeanBytes.put(clazz, classBytes);
            _abstractBeanImplementations.put(clazz, classImpl);
            _jdbcMaps.put(classImpl, jdbcMap);
          }
          else {
            _abstractBeanImplementations.put(clazz, clazz);
            _abstractBeanBytes.put(clazz, DUMMY_BYTES);
          }
          Class persistorClass = jdbcMap.getPersistorClass();

          if (persistorClass == null ||
              (Persistor.class.isAssignableFrom(persistorClass) &&
               Modifier.isAbstract(persistorClass.getModifiers()))) {

            boolean oracle = _dbname.indexOf(DB_ORACLE) != -1;

            JDBCPersistorGenerator generator
              = new JDBCPersistorGenerator(classImpl,
                                           jdbcMap,
                                           _locatorsUpdateCopy,
                                           oracle,
                                           _cl,
                                           false);

            generator.generate();
            persistorClass = generator.getPersistor();
          }
          jdbcPersistor = (Persistor) persistorClass.newInstance();
          _jdbcPersistors.put(clazz, jdbcPersistor);
          _jdbcPersistors.put(classImpl, jdbcPersistor);
          //
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InstantiationException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    } finally {
      try {
        close(conn);
      } catch (Throwable e) {
      }
    }
  }

  @Override
  public <T> T newInstance(Class<T> clazz)
  {
    Class t = (Class) _abstractBeanImplementations.get(clazz);
    if (t == null) {
      synchronized (this) {
        t = (Class) _abstractBeanImplementations.get(clazz);
        if (t == null) {
          prepare(clazz);
        }
      }
    }
    t = _abstractBeanImplementations.get(clazz);
    try {
      return (T) t.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] getImplementation(Class entityClass)
  {
    byte[] bytes = _abstractBeanBytes.get(entityClass);
    if (bytes == null) {
      synchronized (this) {
        bytes = _abstractBeanBytes.get(entityClass);
        if (bytes == null) {
          prepare(entityClass);
        }
      }
    }
    bytes = _abstractBeanBytes.get(entityClass);
    if (bytes == DUMMY_BYTES) {
      throw new RuntimeException("Class [" +
                                 entityClass +
                                 "] is not an interface or an abstract class");
    }
    return bytes;
  }

  public void close() throws SQLException
  {
    if (_connectionProvider != null)
      _connectionProvider.close();
  }

  public void init(DataSource dataSource, Properties properties)
    throws SQLException
  {
    if (properties == null && dataSource == null) {
      throw new IllegalArgumentException(
        "either dataSource or properties must be specified");
    }
    else if (dataSource != null) {
      _connectionProvider = new DataSourceWrapper(dataSource);

      return;
    }
    else if (properties.get(Persistence.JNDI_NAME) != null) {
      String jndiName = properties.getProperty(Persistence.JNDI_NAME);

      _connectionProvider = new DataSourceWrapper(jndiName);
    }
    else if (properties.get(Persistence.DRIVER) != null) {
      _connectionProvider = new ConnectionPool(properties);
    }
    else {
      throw new IllegalArgumentException(
        "neither dataSource or valid properties were specified");
    }
  }

  @Override
  public <T> MappedClass<T> register(Class<T> entityClass)
  {
    Entity entityAnn = entityClass.getAnnotation(Entity.class);

    if (entityAnn == null)
      throw new IllegalArgumentException("Class "
                                         + entityClass
                                         + " must be annotated with @Entitity annotation");

    MappedClass jdbcMap = new MappedClassImpl(entityClass, entityAnn);

    _jdbcMaps.put(entityClass, jdbcMap);

    return jdbcMap;
  }

  public Connection getConnection()
    throws SQLException
  {
    return new ConnectionImpl(_connectionProvider.conn(), this);
  }

  void close(Connection conn)
    throws SQLException
  {
    _connectionProvider.close(((ConnectionImpl) conn).getUnderlyingConnection());
  }

  public String getDbname()
  {
    return _dbname;
  }

  interface ConnectionProvider
  {
    java.sql.Connection conn()
      throws SQLException;

    void close(java.sql.Connection conn)
      throws SQLException;

    void close() throws SQLException;
  }

  private void validateProperty(String name, String value)
  {
    if (value == null)
      throw new RuntimeException("Property " + name + " is not specified.");
  }

  class ConnectionPool implements ConnectionProvider
  {
    private Properties properties;
    //
    private Driver driver;
    private String url;
    //
    private final java.sql.Connection[] _connectionPool;
    private volatile int connectionCount = 0;
    private int connectionPoolSize = 0;

    public ConnectionPool(Properties properties)
      throws SQLException
    {
      url = properties.getProperty(URL);
      final String clazz = properties.getProperty(DRIVER);
      try {
        connectionPoolSize = Integer.parseInt(properties.getProperty(
          POOL_SIZE,
          "10"));
      } catch (Throwable e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        else {
          throw new RuntimeException(e);
        }
      }
      properties.remove(URL);
      properties.remove(DRIVER);
      properties.remove(POOL_SIZE);
      this.properties = new Properties();
      this.properties.putAll(properties);
      _connectionPool = new java.sql.Connection[connectionPoolSize];
      try {
        Class.forName(clazz);
        driver = DriverManager.getDriver(url);
      } catch (SQLException e) {
        throw e;
      } catch (Throwable e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        else {
          throw new RuntimeException(e);
        }
      }
    }

    public java.sql.Connection conn()
      throws SQLException
    {
      synchronized (_connectionPool) {
        java.sql.Connection c = null;
        while (true) {
          for (int i = 0; i < _connectionPool.length; i++) {
            if ((c = _connectionPool[i]) != null) {
              _connectionPool[i] = null;
              return c;
            }
          }
          if (connectionCount < connectionPoolSize) {
            c = driver.connect(url, this.properties);
            connectionCount++;
            return c;
          }
          try {
            _connectionPool.wait();
          } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
          }
        }
      }
    }

    public void close(java.sql.Connection conn)
    {
      synchronized (_connectionPool) {
        for (int i = 0; i < _connectionPool.length; i++) {
          if (_connectionPool[i] == null) {
            _connectionPool[i] = conn;
            break;
          }
        }
        _connectionPool.notifyAll();
      }
    }

    public void close() throws SQLException
    {
      for (java.sql.Connection connection : _connectionPool) {
        try {
          connection.close();
        } catch (Throwable e) {
          log.log(Level.FINER, e.getLocalizedMessage(), e);
        }
      }
    }
  }

  class DataSourceWrapper implements ConnectionProvider
  {
    DataSource _datasource = null;

    public DataSourceWrapper(DataSource dataSource)
    {
      this._datasource = dataSource;
    }

    public DataSourceWrapper(String jndiName)
      throws SQLException
    {
      try {
        InitialContext ic = new InitialContext();
        _datasource = (DataSource) ic.lookup(jndiName);
      } catch (NamingException e) {
        e.printStackTrace();
        throw new SQLException(e.getMessage());
      }
    }

    public java.sql.Connection conn()
      throws SQLException
    {
      return _datasource.getConnection();
    }

    public void close(java.sql.Connection conn)
      throws SQLException
    {
      conn.close();
    }

    public void close() throws SQLException
    {
    }
  }
}
