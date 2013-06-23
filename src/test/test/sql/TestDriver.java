package test.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class TestDriver implements Driver
{
  public final static String URL = "jdbc:jdbcp:";

  @Override
  public Connection connect(String url, Properties info)
    throws SQLException
  {
    return new TestConnection();
  }

  @Override
  public boolean acceptsURL(String url) throws SQLException
  {
    return url.startsWith(URL);
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
    throws SQLException
  {
    return new DriverPropertyInfo[0];
  }

  @Override
  public int getMajorVersion()
  {
    return 0;
  }

  @Override
  public int getMinorVersion()
  {
    return 0;
  }

  @Override
  public boolean jdbcCompliant()
  {
    return true;
  }
}
