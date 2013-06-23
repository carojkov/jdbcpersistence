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

/**
 * title: java.sql.Timestamp->Types.TIMESTAMP
 */
public class T0004 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0004",
               "CREATE TABLE T0004 (ID INT, TIME_ TIME, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0004Bean.class);
    Connection conn = _persistence.getConnection();

    final java.sql.Time time
      = new java.sql.Time(System.currentTimeMillis());
    T0004Bean bean = _persistence.newInstance(T0004Bean.class);
    bean.setId(1);
    bean.setTime(time);

    conn.insert(bean);
    conn.commit();

    bean = (T0004Bean) conn.load(T0004Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    Assert.assertEquals(time.getHours(), bean.getTime().getHours());
    Assert.assertEquals(time.getMinutes(), bean.getTime().getMinutes());
    Assert.assertEquals(time.getSeconds(), bean.getTime().getSeconds());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0004Bean.class);
    Connection conn = _persistence.getConnection();

    final java.sql.Time time
      = new java.sql.Time(System.currentTimeMillis());

    final java.sql.Time time1
      = new java.sql.Time(System.currentTimeMillis()
                          - 1000 * 60 * 60 * 60);

    T0004Bean bean = _persistence.newInstance(T0004Bean.class);
    bean.setId(1);
    bean.setTime(time);

    conn.insert(bean);
    conn.commit();

    bean.setTime(time1);
    conn.update(bean);
    conn.commit();

    bean = (T0004Bean) conn.load(T0004Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    Assert.assertEquals(time1.getHours(), bean.getTime().getHours());
    Assert.assertEquals(time1.getMinutes(), bean.getTime().getMinutes());
    Assert.assertEquals(time1.getSeconds(), bean.getTime().getSeconds());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T0004Bean.class);
    Connection conn = _persistence.getConnection();

    final java.sql.Time time
      = new java.sql.Time(System.currentTimeMillis());

    T0004Bean bean = _persistence.newInstance(T0004Bean.class);
    bean.setId(1);
    bean.setTime(time);

    conn.insert(bean);
    conn.commit();

    bean.setTime(null);
    conn.update(bean);
    conn.commit();

    bean = (T0004Bean) conn.load(T0004Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getTime());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0004")
  public static interface T0004Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "TIME_")
    public java.sql.Time getTime();

    public void setTime(java.sql.Time time);
  }
}

