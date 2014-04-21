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

public class T0000 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0000",
               "CREATE TABLE T0000 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0000Bean.class);
    Connection conn = _persistence.getConnection();
    T0000Bean bean = _persistence.newInstance(T0000Bean.class);
    bean.setId(1);
    bean.setData("T0000");
    conn.insert(bean);
    conn.commit();

    bean = (T0000Bean) conn.load(T0000Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("T0000", bean.getData());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0000Bean.class);

    T0000Bean bean = _persistence.newInstance(T0000Bean.class);
    bean.setId(1);
    bean.setData("T0000");

    Connection conn = _persistence.getConnection();
    conn.insert(bean);

    bean.setData("new T0000");
    conn.update(bean);

    bean = conn.load(T0000Bean.class, 1);
    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("new T0000", bean.getData());

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0000Bean.class);
    Connection conn = _persistence.getConnection();
    T0000Bean bean = _persistence.newInstance(T0000Bean.class);
    bean.setId(1);
    bean.setData("T0000");
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

    @Column(name = "DATA")
    public String getData();

    public void setData(String data);
  }
}

