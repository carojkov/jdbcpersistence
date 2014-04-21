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

public class T0008 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0008",
               "CREATE TABLE T0008 (ID INT, DOUBLE_ DOUBLE, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0008Bean.class);
    Connection conn = _persistence.getConnection();

    double f = (double) Math.PI;
    T0008Bean bean = _persistence.newInstance(T0008Bean.class);
    bean.setId(1);
    bean.setDouble(f);

    conn.insert(bean);
    conn.commit();

    bean = (T0008Bean) conn.load(T0008Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getDouble(), 0);

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0008Bean.class);
    Connection conn = _persistence.getConnection();

    double f1 = (double) Math.PI;
    double f2 = (double) (Math.PI * 2);

    T0008Bean bean = _persistence.newInstance(T0008Bean.class);
    bean.setId(1);
    bean.setDouble(f1);

    conn.insert(bean);
    conn.commit();

    bean.setDouble(f2);
    conn.update(bean);
    conn.commit();

    bean = (T0008Bean) conn.load(T0008Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getDouble(), 0);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0008")
  public static interface T0008Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DOUBLE_")
    public double getDouble();

    public void setDouble(double value);
  }
}
