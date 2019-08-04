package test.pk;

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
import java.sql.Types;

/**
 * title: Single column PK + 2 Columns
 */
public class T0005 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0005",
               "CREATE TABLE T0005 (ID INT, DATA1 VARCHAR(20),DATA2 VARCHAR(20), PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0005Bean.class);
    Connection conn = _persistence.getConnection();
    T0005Bean bean = (T0005Bean) _persistence.newInstance(T0005Bean.class);
    bean.setId(1);
    bean.setData1("DATA-1");
    bean.setData2("DATA-2");
    conn.insert(bean);
    conn.commit();

    bean = (T0005Bean) conn.load(T0005Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("DATA-1", bean.getData1());
    Assert.assertEquals("DATA-2", bean.getData2());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0005Bean.class);
    Connection conn = _persistence.getConnection();
    T0005Bean bean = (T0005Bean) _persistence.newInstance(T0005Bean.class);
    bean.setId(1);
    bean.setData1("DATA-1");
    bean.setData2("DATA-2");
    conn.insert(bean);
    conn.commit();

    bean.setData1("NEW-DATA-1");
    bean.setData2("NEW-DATA-2");
    conn.update(bean);
    conn.commit();

    bean = (T0005Bean) conn.load(T0005Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals("NEW-DATA-1", bean.getData1());
    Assert.assertEquals("NEW-DATA-2", bean.getData2());

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0005Bean.class);
    Connection conn = _persistence.getConnection();
    T0005Bean bean = (T0005Bean) _persistence.newInstance(T0005Bean.class);
    bean.setId(1);
    bean.setData1("DATA-1");
    bean.setData2("DATA-2");
    conn.insert(bean);
    conn.commit();

    conn.delete(bean);
    conn.commit();

    bean = (T0005Bean) conn.load(T0005Bean.class, new Integer(1));

    Assert.assertNull(bean);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0005")
  public static interface T0005Bean
  {
    @Column(name = "ID", sqlType = Types.INTEGER)
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DATA1")
    public String getData1();

    public void setData1(String data);

    @Column(name = "DATA2")
    public String getData2();

    public void setData2(String data);

  }
}

