package test.pk;

import org.jdbcpersistence.Column;
import org.jdbcpersistence.Connection;
import org.jdbcpersistence.Entity;
import org.jdbcpersistence.Id;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.BaseTest;

import java.sql.SQLException;

public class T0000 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0000",
               "CREATE TABLE T0000 (ID INT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0000Bean.class);
    Connection conn = _persistence.getConnection();
    T0000Bean bean = _persistence.newInstance(T0000Bean.class);
    bean.setId(1);
    conn.insert(bean);
    conn.commit();

    bean = (T0000Bean) conn.load(T0000Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {

    _persistence.register(T0000Bean.class);
    T0000Bean bean = _persistence.newInstance(T0000Bean.class);
    Connection conn = _persistence.getConnection();
    try {
      conn.update(bean);

    } catch (Exception e) {
      Assert.assertEquals(
        "java.lang.IllegalArgumentException: Not supported on bean test.pk.T0000$T0000Bean because the class declares no regular columns",
        e.toString());
    }

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0000Bean.class);
    Connection conn = _persistence.getConnection();
    T0000Bean bean = _persistence.newInstance(T0000Bean.class);
    bean.setId(1);
    conn.insert(bean);
    conn.commit();

    conn.delete(bean);
    conn.commit();

    bean = (T0000Bean) conn.load(T0000Bean.class, new Integer(1));

    Assert.assertNull(bean);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0000")
  public static interface T0000Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);
  }
}

