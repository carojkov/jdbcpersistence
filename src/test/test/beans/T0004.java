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
 * title: abstract class with concrete setters and private fields
 */
public class T0004 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0003",
               "CREATE TABLE T0003 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0003Bean.class);
    Connection conn = _persistence.getConnection();
    T0003Bean bean = _persistence.newInstance(T0003Bean.class);
    bean.setId(1);
    bean.setData("T0003");
    conn.insert(bean);
    conn.commit();

    bean = (T0003Bean) conn.load(T0003Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("T0003", bean.getData());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0003Bean.class);

    T0003Bean bean = _persistence.newInstance(T0003Bean.class);
    bean.setId(1);
    bean.setData("T0003");

    Connection conn = _persistence.getConnection();
    conn.insert(bean);

    bean.setData("new T0003");
    conn.update(bean);

    bean = conn.load(T0003Bean.class, 1);
    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("new T0003", bean.getData());

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0003Bean.class);
    Connection conn = _persistence.getConnection();
    T0003Bean bean = _persistence.newInstance(T0003Bean.class);
    bean.setId(1);
    bean.setData("T0003");
    conn.insert(bean);
    conn.commit();

    conn.delete(bean);
    conn.commit();

    bean = (T0003Bean) conn.load(T0003Bean.class, new Integer(1));

    Assert.assertNull(bean);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0003")
  public static abstract class T0003Bean
  {
    private int id;
    private String data;

    @Column(name = "ID")
    @Id()
    public abstract int getId();

    public void setId(int id)
    {
      this.id = id;
    }

    @Column(name = "DATA")
    public abstract String getData();

    public void setData(String data)
    {
      this.data = data;
    }
  }
}

