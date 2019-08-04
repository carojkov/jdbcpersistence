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
 * title: long->Types.BIGINT
 */
public class T000e extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000E",
               "CREATE TABLE T000E (ID INT, BIGINT_ BIGINT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000EBean.class);
    Connection conn = _persistence.getConnection();

    long f = Long.MIN_VALUE;
    T000EBean bean = _persistence.newInstance(T000EBean.class);
    bean.setId(1);
    bean.setLong(f);

    conn.insert(bean);
    conn.commit();

    bean = (T000EBean) conn.load(T000EBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getLong(), 0);

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000EBean.class);
    Connection conn = _persistence.getConnection();

    long f1 = Long.MIN_VALUE;
    long f2 = Long.MAX_VALUE;

    T000EBean bean = _persistence.newInstance(T000EBean.class);
    bean.setId(1);
    bean.setLong(f1);

    conn.insert(bean);
    conn.commit();

    bean.setLong(f2);
    conn.update(bean);
    conn.commit();

    bean = (T000EBean) conn.load(T000EBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getLong(), 0);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000E")
  public static interface T000EBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "BIGINT_")
    public long getLong();

    public void setLong(long value);
  }
}
