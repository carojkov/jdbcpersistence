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
import java.sql.Types;

public class T0004 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0004",
               "CREATE TABLE T0004 (ID INT, DATA VARCHAR(20), PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0004Bean.class);
    Connection conn = _persistence.getConnection();
    T0004Bean bean = (T0004Bean) _persistence.newInstance(T0004Bean.class);
    bean.setId(1);
    bean.setData("DATA");
    conn.insert(bean);
    conn.commit();

    bean = (T0004Bean) conn.load(T0004Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("DATA", bean.getData());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0004Bean.class);
    Connection conn = _persistence.getConnection();
    T0004Bean bean = (T0004Bean) _persistence.newInstance(T0004Bean.class);
    bean.setId(1);
    bean.setData("DATA");
    conn.insert(bean);
    conn.commit();

    bean.setData("NEW-DATA");
    conn.update(bean);
    conn.commit();

    bean = (T0004Bean) conn.load(T0004Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("NEW-DATA", bean.getData());

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0004Bean.class);
    Connection conn = _persistence.getConnection();
    T0004Bean bean = _persistence.newInstance(T0004Bean.class);
    bean.setId(1);
    bean.setData("DATA");
    conn.insert(bean);
    conn.commit();

    conn.delete(bean);
    conn.commit();

    bean = (T0004Bean) conn.load(T0004Bean.class, new Integer(1));

    Assert.assertNull(bean);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0004")
  public static interface T0004Bean
  {
    @Column(name = "ID", sqlType = Types.INTEGER)
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DATA")
    public String getData();

    public void setData(String data);
  }
}

