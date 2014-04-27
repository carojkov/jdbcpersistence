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
 * title: float->Types.FLOAT
 */
public class T0006 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0006",
               "CREATE TABLE T0006 (ID INT, FLOAT_ FLOAT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0006Bean.class);
    Connection conn = _persistence.getConnection();

    float f = (float) Math.PI;
    T0006Bean bean = _persistence.newInstance(T0006Bean.class);
    bean.setId(1);
    bean.setFloat(f);

    conn.insert(bean);
    conn.commit();

    bean = (T0006Bean) conn.load(T0006Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getFloat(), 0);

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0006Bean.class);
    Connection conn = _persistence.getConnection();

    float f1 = (float) Math.PI;
    float f2 = (float) (Math.PI * 2);

    T0006Bean bean = _persistence.newInstance(T0006Bean.class);
    bean.setId(1);
    bean.setFloat(f1);

    conn.insert(bean);
    conn.commit();

    bean.setFloat(f2);
    conn.update(bean);
    conn.commit();

    bean = (T0006Bean) conn.load(T0006Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getFloat(), 0);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0006")
  public static interface T0006Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "FLOAT_")
    public float getFloat();

    public void setFloat(float value);
  }
}
