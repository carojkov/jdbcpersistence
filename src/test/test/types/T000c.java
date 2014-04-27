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
 * title: short->Types.SHORT
 */
public class T000c extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000C",
               "CREATE TABLE T000C (ID INT, SHORT_ SMALLINT , PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000CBean.class);
    Connection conn = _persistence.getConnection();

    T000CBean bean = _persistence.newInstance(T000CBean.class);
    bean.setId(1);
    bean.setShort(Short.MIN_VALUE);

    conn.insert(bean);
    conn.commit();

    bean = (T000CBean) conn.load(T000CBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(Short.MIN_VALUE, bean.getShort());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000CBean.class);
    Connection conn = _persistence.getConnection();

    T000CBean bean = _persistence.newInstance(T000CBean.class);
    bean.setId(1);
    bean.setShort(Short.MIN_VALUE);

    conn.insert(bean);
    conn.commit();

    bean.setShort(Short.MAX_VALUE);
    conn.update(bean);
    conn.commit();

    bean = (T000CBean) conn.load(T000CBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(Short.MAX_VALUE, bean.getShort());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000C")
  public static interface T000CBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "SHORT_")
    public short getShort();

    public void setShort(short value);
  }
}
