package test.beans;

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
 * title: abstract class
 */
public class T0001 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0001",
               "CREATE TABLE T0001 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0001Bean.class);
    Connection conn = _persistence.getConnection();
    T0001Bean bean = _persistence.newInstance(T0001Bean.class);
    bean.setId(1);
    bean.setData("T0001");
    conn.insert(bean);
    conn.commit();

    bean = (T0001Bean) conn.load(T0001Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("T0001", bean.getData());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0001Bean.class);

    T0001Bean bean = _persistence.newInstance(T0001Bean.class);
    bean.setId(1);
    bean.setData("T0001");

    Connection conn = _persistence.getConnection();
    conn.insert(bean);

    bean.setData("new T0001");
    conn.update(bean);

    bean = conn.load(T0001Bean.class, 1);
    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("new T0001", bean.getData());

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0001Bean.class);
    Connection conn = _persistence.getConnection();
    T0001Bean bean = _persistence.newInstance(T0001Bean.class);
    bean.setId(1);
    bean.setData("T0001");
    conn.insert(bean);
    conn.commit();

    conn.delete(bean);
    conn.commit();

    bean = (T0001Bean) conn.load(T0001Bean.class, new Integer(1));

    Assert.assertNull(bean);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0001")
  public static abstract class T0001Bean
  {
    @Column(name = "ID")
    @Id()
    public abstract int getId();

    public abstract void setId(int id);

    @Column(name = "DATA")
    public abstract String getData();

    public abstract void setData(String data);
  }
}

