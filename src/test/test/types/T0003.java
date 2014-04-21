package test.types;

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

public class T0003 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0003",
               "CREATE TABLE T0003 (ID INT, DATE_ TIMESTAMP, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0003Bean.class);
    Connection conn = _persistence.getConnection();

    final java.sql.Timestamp time
      = new java.sql.Timestamp(System.currentTimeMillis());
    T0003Bean bean = _persistence.newInstance(T0003Bean.class);
    bean.setId(1);
    bean.setTimestamp(time);

    conn.insert(bean);
    conn.commit();

    bean = (T0003Bean) conn.load(T0003Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    Assert.assertEquals(time, bean.getTimestamp());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0003Bean.class);
    Connection conn = _persistence.getConnection();

    final java.sql.Timestamp time
      = new java.sql.Timestamp(System.currentTimeMillis());

    final java.sql.Timestamp time1
      = new java.sql.Timestamp(System.currentTimeMillis()
                               - 1000 * 60 * 60 * 60);

    T0003Bean bean = _persistence.newInstance(T0003Bean.class);
    bean.setId(1);
    bean.setTimestamp(time);

    conn.insert(bean);
    conn.commit();

    bean.setTimestamp(time1);
    conn.update(bean);
    conn.commit();

    bean = (T0003Bean) conn.load(T0003Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(time1, bean.getTimestamp());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T0003Bean.class);
    Connection conn = _persistence.getConnection();

    final java.sql.Timestamp time
      = new java.sql.Timestamp(System.currentTimeMillis());

    T0003Bean bean = _persistence.newInstance(T0003Bean.class);
    bean.setId(1);
    bean.setTimestamp(time);

    conn.insert(bean);
    conn.commit();

    bean.setTimestamp(null);
    conn.update(bean);
    conn.commit();

    bean = (T0003Bean) conn.load(T0003Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getTimestamp());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0003")
  public static interface T0003Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DATE_")
    public java.sql.Timestamp getTimestamp();

    public void setTimestamp(java.sql.Timestamp time);
  }
}

