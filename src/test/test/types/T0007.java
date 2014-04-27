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
 * title: java.lang.Double->Types.DOUBLE
 */
public class T0007 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0007",
               "CREATE TABLE T0007 (ID INT, DOUBLE_ DOUBLE, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0007Bean.class);
    Connection conn = _persistence.getConnection();

    Double f = new Double(Math.PI);
    T0007Bean bean = _persistence.newInstance(T0007Bean.class);
    bean.setId(1);
    bean.setDouble(f);

    conn.insert(bean);
    conn.commit();

    bean = (T0007Bean) conn.load(T0007Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getDouble());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0007Bean.class);
    Connection conn = _persistence.getConnection();

    Double f1 = new Double(Math.PI);
    Double f2 = new Double(Math.PI * 2);

    T0007Bean bean = _persistence.newInstance(T0007Bean.class);
    bean.setId(1);
    bean.setDouble(f1);

    conn.insert(bean);
    conn.commit();

    bean.setDouble(f2);
    conn.update(bean);
    conn.commit();

    bean = (T0007Bean) conn.load(T0007Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getDouble());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T0007Bean.class);
    Connection conn = _persistence.getConnection();

    T0007Bean bean = _persistence.newInstance(T0007Bean.class);
    bean.setId(1);
    bean.setDouble(Math.PI);

    conn.insert(bean);
    conn.commit();

    bean.setDouble(null);
    conn.update(bean);
    conn.commit();

    bean = (T0007Bean) conn.load(T0007Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getDouble());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0007")
  public static interface T0007Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DOUBLE_")
    public Double getDouble();

    public void setDouble(Double value);
  }
}
