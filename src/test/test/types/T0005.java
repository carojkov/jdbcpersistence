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

public class T0005 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0005",
               "CREATE TABLE T0005 (ID INT, FLOAT_ FLOAT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0005Bean.class);
    Connection conn = _persistence.getConnection();

    Float f = new Float(Math.PI);
    T0005Bean bean = _persistence.newInstance(T0005Bean.class);
    bean.setId(1);
    bean.setFloat(f);

    conn.insert(bean);
    conn.commit();

    bean = (T0005Bean) conn.load(T0005Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getFloat());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0005Bean.class);
    Connection conn = _persistence.getConnection();

    Float f1 = new Float(Math.PI);
    Float f2 = new Float(Math.PI * 2);

    T0005Bean bean = _persistence.newInstance(T0005Bean.class);
    bean.setId(1);
    bean.setFloat(f1);

    conn.insert(bean);
    conn.commit();

    bean.setFloat(f2);
    conn.update(bean);
    conn.commit();

    bean = (T0005Bean) conn.load(T0005Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getFloat());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T0005Bean.class);
    Connection conn = _persistence.getConnection();

    T0005Bean bean = _persistence.newInstance(T0005Bean.class);
    bean.setId(1);
    bean.setFloat((float) Math.PI);

    conn.insert(bean);
    conn.commit();

    bean.setFloat(null);
    conn.update(bean);
    conn.commit();

    bean = (T0005Bean) conn.load(T0005Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getFloat());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0005")
  public static interface T0005Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "FLOAT_")
    public Float getFloat();

    public void setFloat(Float value);
  }
}
