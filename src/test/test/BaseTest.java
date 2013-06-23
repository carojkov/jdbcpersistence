package test;

import org.jdbcpersistence.Connection;
import org.jdbcpersistence.Persistence;
import org.jdbcpersistence.PersistenceFactory;
import org.junit.After;
import org.junit.Before;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class BaseTest
{
  protected Persistence _persistence;

  @Before
  public final void init()
  {
    _persistence = PersistenceFactory.getInstance().newJdbcPersistence();
    //setVerbose(true);

    Properties properties = getProperties();

    try {
      _persistence.init(null, properties);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void dropCreate(String drop, String create) throws SQLException
  {
    Connection conn = _persistence.getConnection();

    Statement st = conn.createStatement();

    try {
      if (drop != null)
        st.execute(drop);
    } catch (SQLException e) {
    }

    try {
      if (create != null)
        st.executeUpdate(create);
    } finally {
      close(st);
      conn.close();
    }
  }

  protected final void setVerbose(boolean isVerbose)
  {
    System.setProperty("jdbcpersistence.verbose", String.valueOf(isVerbose));
  }

  protected Properties getProperties()
  {
    String dbName = this.getClass().getSimpleName();

    Properties properties = new Properties();

    properties.setProperty(Persistence.DRIVER,
                           "org.apache.derby.jdbc.EmbeddedDriver");
    properties.setProperty(Persistence.URL,
                           "jdbc:derby:memory:" + dbName + ";create=true");
    properties.setProperty(Persistence.USER, "");
    properties.setProperty(Persistence.PASSWORD, "");

    return properties;
  }

  @After
  public final void destroy()
  {
    try {
      if (_persistence != null)
        _persistence.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void close(Statement st)
  {
    if (st == null)
      return;

    try {
      st.close();
    } catch (SQLException e) {
    }
  }
}
