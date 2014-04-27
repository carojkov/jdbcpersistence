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
 * title: java.lang.Boolean ->Types.BOOLEAN
 */
public class T0009 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0009",
               "CREATE TABLE T0009 (ID INT, BOOLEAN_ BOOLEAN, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0009Bean.class);
    Connection conn = _persistence.getConnection();

    Boolean b = Boolean.TRUE;
    T0009Bean bean = _persistence.newInstance(T0009Bean.class);
    bean.setId(1);
    bean.setBoolean(b);

    conn.insert(bean);
    conn.commit();

    bean = (T0009Bean) conn.load(T0009Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(b, bean.getBoolean());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0009Bean.class);
    Connection conn = _persistence.getConnection();

    Boolean f1 = Boolean.TRUE;
    Boolean f2 = Boolean.FALSE;

    T0009Bean bean = _persistence.newInstance(T0009Bean.class);
    bean.setId(1);
    bean.setBoolean(f1);

    conn.insert(bean);
    conn.commit();

    bean.setBoolean(f2);
    conn.update(bean);
    conn.commit();

    bean = (T0009Bean) conn.load(T0009Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getBoolean());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T0009Bean.class);
    Connection conn = _persistence.getConnection();

    T0009Bean bean = _persistence.newInstance(T0009Bean.class);
    bean.setId(1);
    bean.setBoolean(Boolean.TRUE);

    conn.insert(bean);
    conn.commit();

    bean.setBoolean(null);
    conn.update(bean);
    conn.commit();

    bean = (T0009Bean) conn.load(T0009Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getBoolean());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0009")
  public static interface T0009Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "BOOLEAN_")
    public Boolean getBoolean();

    public void setBoolean(Boolean value);
  }
}
