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
 * title: java.io.Reader & java.io.Writer->Types.CLOB
 */
public class T000k extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000K",
               "CREATE TABLE T000K (ID INT, CLOB_ CLOB, PRIMARY KEY(ID))");
  }

  //@Test
  public void insert() throws SQLException, MalformedURLException
  {
    setVerbose(true);
    _persistence.register(T000KBean.class);
    Connection conn = _persistence.getConnection();

    T000KBean bean = _persistence.newInstance(T000KBean.class);
    bean.setId(1);
    //bean.setUrl(url);

    conn.insert(bean);
    conn.commit();

    bean = (T000KBean) conn.load(T000KBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    //Assert.assertEquals(url, bean.getUrl());

    conn.close();
  }

  //@Test
  public void update() throws SQLException, MalformedURLException
  {
    _persistence.register(T000KBean.class);
    Connection conn = _persistence.getConnection();

    T000KBean bean = _persistence.newInstance(T000KBean.class);
    bean.setId(1);

    conn.insert(bean);
    conn.commit();

    conn.update(bean);
    conn.commit();

    bean = (T000KBean) conn.load(T000KBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    conn.close();
  }

  //@Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000KBean.class);
    Connection conn = _persistence.getConnection();

    T000KBean bean = _persistence.newInstance(T000KBean.class);
    bean.setId(1);

    conn.insert(bean);
    conn.commit();

    conn.update(bean);
    conn.commit();

    bean = (T000KBean) conn.load(T000KBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000K")
  public static interface T000KBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "CLOB_", sqlType = Types.CLOB)
    public String getClob();

    public void setClob(String str);
  }
}
