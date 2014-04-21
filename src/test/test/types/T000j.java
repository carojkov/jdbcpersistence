package test.types;

import org.jdbcpersistence.Column;
import org.jdbcpersistence.Connection;
import org.jdbcpersistence.Entity;
import org.jdbcpersistence.Id;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import test.BaseTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

public class T000j extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TYPE T000J",
               "CREATE TYPE T000J EXTERNAL NAME 'java.net.URL' LANGUAGE JAVA");
    dropCreate("DROP TABLE T000J",
               "CREATE TABLE T000J (ID INT, URL_ T000J, PRIMARY KEY(ID))");
  }

  //@Test
  public void insert() throws SQLException, MalformedURLException
  {
    setVerbose(true);
    _persistence.register(T000JBean.class);
    Connection conn = _persistence.getConnection();

    URL url = new URL("http://www.fox.com");
    T000JBean bean = _persistence.newInstance(T000JBean.class);
    bean.setId(1);
    bean.setUrl(url);

    conn.insert(bean);
    conn.commit();

    bean = (T000JBean) conn.load(T000JBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(url, bean.getUrl());

    conn.close();
  }

  //@Test
  public void update() throws SQLException, MalformedURLException
  {
    _persistence.register(T000JBean.class);
    Connection conn = _persistence.getConnection();

    URL url1 = new URL("http://www.fox.com");
    URL url2 = new URL("http://www.al-jazeera.com/en");

    T000JBean bean = _persistence.newInstance(T000JBean.class);
    bean.setId(1);
    bean.setUrl(url1);

    conn.insert(bean);
    conn.commit();

    bean.setUrl(url2);
    conn.update(bean);
    conn.commit();

    bean = (T000JBean) conn.load(T000JBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(url2, bean.getUrl());

    conn.close();
  }

  //@Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000JBean.class);
    Connection conn = _persistence.getConnection();

    T000JBean bean = _persistence.newInstance(T000JBean.class);
    bean.setId(1);
    bean.setUrl(null);

    conn.insert(bean);
    conn.commit();

    bean.setUrl(null);
    conn.update(bean);
    conn.commit();

    bean = (T000JBean) conn.load(T000JBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getUrl());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000J")
  public static interface T000JBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "URL_")
    public URL getUrl();

    public void setUrl(URL url);
  }
}
