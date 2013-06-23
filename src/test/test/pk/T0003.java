package test.pk;

import org.jdbcpersistence.Column;
import org.jdbcpersistence.Entity;
import org.jdbcpersistence.Id;
import org.junit.Assert;
import org.junit.Test;
import test.BaseTest;

import java.sql.SQLException;

/**
 * title: Bad ID1@Id.value == ID2@Id.value
 */
public class T0003 extends BaseTest
{
  @Test
  public void register() throws SQLException
  {
    try {
      _persistence.register(T0003Bean.class);
    } catch (Exception e) {
      Assert.assertEquals(e.toString(), "java.lang.IllegalArgumentException: columns ID2 and ID1 must have different @Id.value");
    }
  }

  @Entity(name = "T0003")
  public static interface T0003Bean
  {
    @Column(name = "ID1")
    @Id()
    public int getId1();

    public void setId1(int id);

    @Column(name = "ID2")
    @Id()
    public int getId2();

    public void setId2(int id);
  }
}

