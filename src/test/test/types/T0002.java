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
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * title: java.sql.Date->Types.DATE
 */
public class T0002 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0002",
               "CREATE TABLE T0002 (ID INT, DATE_ DATE, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0002Bean.class);
    Connection conn = _persistence.getConnection();
    Calendar calendar = new GregorianCalendar(1970, 1, 1, 0, 0, 0);

    final java.sql.Date date = new java.sql.Date(calendar.getTimeInMillis());

    T0002Bean bean = _persistence.newInstance(T0002Bean.class);
    bean.setId(1);
    bean.setDate(date);

    conn.insert(bean);
    conn.commit();

    bean = (T0002Bean) conn.load(T0002Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    Assert.assertEquals(date, bean.getDate());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T0002Bean.class);
    Connection conn = _persistence.getConnection();

    Calendar calendar = new GregorianCalendar(1970, 1, 1, 0, 0, 0);

    final java.sql.Date date = new java.sql.Date(calendar.getTimeInMillis());
    calendar.add(Calendar.YEAR, 1);
    final java.sql.Date date1 = new java.sql.Date(calendar.getTimeInMillis());

    T0002Bean bean = _persistence.newInstance(T0002Bean.class);
    bean.setId(1);
    bean.setDate(date);

    conn.insert(bean);
    conn.commit();

    bean.setDate(date1);
    conn.update(bean);
    conn.commit();

    bean = (T0002Bean) conn.load(T0002Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(date1, bean.getDate());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T0002Bean.class);
    Connection conn = _persistence.getConnection();

    Calendar calendar = new GregorianCalendar(1970, 1, 1, 0, 0, 0);

    final java.sql.Date date = new java.sql.Date(calendar.getTimeInMillis());

    calendar.add(Calendar.YEAR, 1);

    T0002Bean bean = _persistence.newInstance(T0002Bean.class);
    bean.setId(1);
    bean.setDate(date);

    conn.insert(bean);
    conn.commit();

    bean.setDate(null);
    conn.update(bean);
    conn.commit();

    bean = (T0002Bean) conn.load(T0002Bean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getDate());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0002")
  public static interface T0002Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DATE_")
    public java.sql.Date getDate();

    public void setDate(java.sql.Date date);
  }
}

