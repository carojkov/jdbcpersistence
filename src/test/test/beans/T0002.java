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

public class T0002 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0002",
               "CREATE TABLE T0002 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0002Bean.class);
    Connection conn = _persistence.getConnection();
    T0002Bean bean = _persistence.newInstance(T0002Bean.class);
    bean.setId(1);
    bean.setData("T0002");
    conn.insert(bean);
    conn.commit();

    bean = (T0002Bean) conn.load(T0002Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("T0002", bean.getData());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0002Bean.class);

    T0002Bean bean = _persistence.newInstance(T0002Bean.class);
    bean.setId(1);
    bean.setData("T0002");

    Connection conn = _persistence.getConnection();
    conn.insert(bean);

    bean.setData("new T0002");
    conn.update(bean);

    bean = conn.load(T0002Bean.class, 1);
    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("new T0002", bean.getData());

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0002Bean.class);
    Connection conn = _persistence.getConnection();
    T0002Bean bean = _persistence.newInstance(T0002Bean.class);
    bean.setId(1);
    bean.setData("T0002");
    conn.insert(bean);
    conn.commit();

    conn.delete(bean);
    conn.commit();

    bean = (T0002Bean) conn.load(T0002Bean.class, new Integer(1));

    Assert.assertNull(bean);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0002")
  public static abstract class T0002Bean
  {
    protected int id;
    protected String data;

    @Column(name = "ID")
    @Id()
    public int getId()
    {
      return id;
    }

    public abstract void setId(int id);

    @Column(name = "DATA")
    public String getData()
    {
      return data;
    }

    public abstract void setData(String data);
  }
}

