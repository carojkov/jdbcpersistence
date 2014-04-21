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

public class T000i extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000I",
               "CREATE TABLE T000I (ID INT, VARBINARY_ VARCHAR (1024) FOR BIT DATA, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000IBean.class);
    Connection conn = _persistence.getConnection();

    byte[] data = new byte[1024];
    for (int i = 0; i < 1024; i++) {
      data[i] = (byte) (i % 128);
    }

    T000IBean bean = _persistence.newInstance(T000IBean.class);
    bean.setId(1);
    bean.setBytes(data);

    conn.insert(bean);
    conn.commit();

    bean = (T000IBean) conn.load(T000IBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertArrayEquals(data, bean.getBytes());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000IBean.class);
    Connection conn = _persistence.getConnection();

    byte[] data1 = new byte[1024];
    for (int i = 0; i < 1024; i++) {
      data1[i] = (byte) (i % 128);
    }

    byte[] data2 = new byte[1024];
    for (int i = 0; i < 1024; i++) {
      data2[i] = (byte) (i % 127);
    }

    T000IBean bean = _persistence.newInstance(T000IBean.class);
    bean.setId(1);
    bean.setBytes(data1);

    conn.insert(bean);
    conn.commit();

    bean.setBytes(data2);
    conn.update(bean);
    conn.commit();

    bean = (T000IBean) conn.load(T000IBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertArrayEquals(data2, bean.getBytes());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000IBean.class);
    Connection conn = _persistence.getConnection();

    T000IBean bean = _persistence.newInstance(T000IBean.class);
    bean.setId(1);
    bean.setBytes(null);

    conn.insert(bean);
    conn.commit();

    bean.setBytes(null);
    conn.update(bean);
    conn.commit();

    bean = (T000IBean) conn.load(T000IBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getBytes());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000I")
  public static interface T000IBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "VARBINARY_")
    public byte[] getBytes();

    public void setBytes(byte[] data);
  }
}
