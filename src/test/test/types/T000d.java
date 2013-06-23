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
 * title: java.lang.Long->Types.BIGINT
 */
public class T000d extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000D",
               "CREATE TABLE T000D (ID INT, BIGINT_ BIGINT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000DBean.class);
    Connection conn = _persistence.getConnection();

    Long f = new Long(Long.MIN_VALUE);
    T000DBean bean = _persistence.newInstance(T000DBean.class);
    bean.setId(1);
    bean.setLong(f);

    conn.insert(bean);
    conn.commit();

    bean = (T000DBean) conn.load(T000DBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getLong());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000DBean.class);
    Connection conn = _persistence.getConnection();

    Long f1 = new Long(Long.MIN_VALUE);
    Long f2 = new Long(Long.MAX_VALUE);

    T000DBean bean = _persistence.newInstance(T000DBean.class);
    bean.setId(1);
    bean.setLong(f1);

    conn.insert(bean);
    conn.commit();

    bean.setLong(f2);
    conn.update(bean);
    conn.commit();

    bean = (T000DBean) conn.load(T000DBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getLong());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000DBean.class);
    Connection conn = _persistence.getConnection();

    T000DBean bean = _persistence.newInstance(T000DBean.class);
    bean.setId(1);
    bean.setLong(Long.MAX_VALUE);

    conn.insert(bean);
    conn.commit();

    bean.setLong(null);
    conn.update(bean);
    conn.commit();

    bean = (T000DBean) conn.load(T000DBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getLong());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000D")
  public static interface T000DBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "BIGINT_")
    public Long getLong();

    public void setLong(Long value);
  }
}
