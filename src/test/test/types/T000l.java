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

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * title: byte[]->Types.LONGVARBINARY
 * todo: insert with null, update
 */
public class T000l extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000L",
               "CREATE TABLE T000L (ID INT, BINARY_ BLOB, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException, MalformedURLException
  {
    setVerbose(true);
    _persistence.register(T000LBean.class);
    Connection conn = _persistence.getConnection();

    T000LBean bean = _persistence.newInstance(T000LBean.class);
    bean.setId(1);
    bean.setBytes(new byte[]{1, 2, 3, 4, 5});

    conn.insert(bean);
    conn.commit();

    bean = conn.load(T000LBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertArrayEquals(bean.getBytes(), new byte[]{1, 2, 3, 4, 5});

    conn.close();
  }

  @Test
  public void update() throws SQLException, MalformedURLException
  {
    _persistence.register(T000LBean.class);
    Connection conn = _persistence.getConnection();

    T000LBean bean = _persistence.newInstance(T000LBean.class);
    bean.setId(1);
    bean.setBytes(new byte[]{0, 1});

    conn.insert(bean);
    conn.commit();

    bean.setBytes(new byte[]{0, 1, 2, 3, 4, 5});
    conn.update(bean);
    conn.commit();

    bean = conn.load(T000LBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertArrayEquals(bean.getBytes(), new byte[]{0, 1, 2, 3, 4, 5});

    conn.close();
  }

  //@Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000LBean.class);
    Connection conn = _persistence.getConnection();

    T000LBean bean = _persistence.newInstance(T000LBean.class);
    bean.setId(1);

    conn.insert(bean);
    conn.commit();

    conn.update(bean);
    conn.commit();

    bean = (T000LBean) conn.load(T000LBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000L")
  public static interface T000LBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "BINARY_", sqlType = Types.LONGVARBINARY)
    public byte[] getBytes();

    public void setBytes(byte[] bytes);
  }
}
