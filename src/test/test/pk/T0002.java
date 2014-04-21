package test.pk;

import org.jdbcpersistence.Column;
import org.jdbcpersistence.Entity;
import org.jdbcpersistence.Id;
import org.junit.Assert;
import org.junit.Test;
import test.BaseTest;

import java.sql.SQLException;

public class T0002 extends BaseTest
{
  @Test
  public void register() throws SQLException
  {
    try {
      _persistence.register(T0002Bean.class);
    } catch (Exception e) {
      Assert.assertEquals(e.toString(),
                          "java.lang.IllegalArgumentException: @Id.value (100) for column 'ID' must be less than number of columns in primary key");
    }
  }

  @Entity(name = "T0002")
  public static interface T0002Bean
  {
    @Column(name = "ID")
    @Id(100)
    public int getId();

    public void setId(int id);
  }
}

