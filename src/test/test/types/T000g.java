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
 * title: byte->Types.TINYINT
 */
public class T000g extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000G",
               "CREATE TABLE T000G (ID INT, BYTE_ SMALLINT, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000GBean.class);
    Connection conn = _persistence.getConnection();

    T000GBean bean = _persistence.newInstance(T000GBean.class);
    bean.setId(1);
    bean.setByte(Byte.MIN_VALUE);

    conn.insert(bean);
    conn.commit();

    bean = (T000GBean) conn.load(T000GBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(Byte.MIN_VALUE, bean.getByte());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000GBean.class);
    Connection conn = _persistence.getConnection();

    T000GBean bean = _persistence.newInstance(T000GBean.class);
    bean.setId(1);
    bean.setByte(Byte.MIN_VALUE);

    conn.insert(bean);
    conn.commit();

    bean.setByte(Byte.MAX_VALUE);
    conn.update(bean);
    conn.commit();

    bean = (T000GBean) conn.load(T000GBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(Byte.MAX_VALUE, bean.getByte());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000G")
  public static interface T000GBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "BYTE_")
    public byte getByte();

    public void setByte(byte value);
  }
}
