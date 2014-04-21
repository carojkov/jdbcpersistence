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

public class T000b extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000B",
               "CREATE TABLE T000B (ID INT, SHORT_ SMALLINT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000BBean.class);
    Connection conn = _persistence.getConnection();

    Short f = new Short(Short.MIN_VALUE);
    T000BBean bean = _persistence.newInstance(T000BBean.class);
    bean.setId(1);
    bean.setShort(f);

    conn.insert(bean);
    conn.commit();

    bean = (T000BBean) conn.load(T000BBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getShort());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000BBean.class);
    Connection conn = _persistence.getConnection();

    Short f1 = new Short(Short.MIN_VALUE);
    Short f2 = new Short(Short.MAX_VALUE);

    T000BBean bean = _persistence.newInstance(T000BBean.class);
    bean.setId(1);
    bean.setShort(f1);

    conn.insert(bean);
    conn.commit();

    bean.setShort(f2);
    conn.update(bean);
    conn.commit();

    bean = (T000BBean) conn.load(T000BBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getShort());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000BBean.class);
    Connection conn = _persistence.getConnection();

    T000BBean bean = _persistence.newInstance(T000BBean.class);
    bean.setId(1);
    bean.setShort(new Short((short) 0));

    conn.insert(bean);
    conn.commit();

    bean.setShort(null);
    conn.update(bean);
    conn.commit();

    bean = (T000BBean) conn.load(T000BBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getShort());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000B")
  public static interface T000BBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "SHORT_")
    public Short getShort();

    public void setShort(Short value);
  }
}
