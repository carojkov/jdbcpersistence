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
 * title: java.lang.Byte->Types.SMALLINT
 */
public class T000f extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000F",
               "CREATE TABLE T000F (ID INT, BYTE_ SMALLINT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000FBean.class);
    Connection conn = _persistence.getConnection();

    Byte f = new Byte((byte) 125);
    T000FBean bean = _persistence.newInstance(T000FBean.class);
    bean.setId(1);
    bean.setByte(f);

    conn.insert(bean);
    conn.commit();

    bean = (T000FBean) conn.load(T000FBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getByte());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000FBean.class);
    Connection conn = _persistence.getConnection();

    Byte f1 = new Byte((byte) 12);
    Byte f2 = new Byte((byte) 321);

    T000FBean bean = _persistence.newInstance(T000FBean.class);
    bean.setId(1);
    bean.setByte(f1);

    conn.insert(bean);
    conn.commit();

    bean.setByte(f2);
    conn.update(bean);
    conn.commit();

    bean = (T000FBean) conn.load(T000FBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getByte());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000FBean.class);
    Connection conn = _persistence.getConnection();

    T000FBean bean = _persistence.newInstance(T000FBean.class);
    bean.setId(1);
    bean.setByte(new Byte((byte) 20));

    conn.insert(bean);
    conn.commit();

    bean.setByte(null);
    conn.update(bean);
    conn.commit();

    bean = (T000FBean) conn.load(T000FBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getByte());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000F")
  public static interface T000FBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "BYTE_")
    public Byte getByte();

    public void setByte(Byte value);
  }
}
