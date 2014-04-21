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

public class T000a extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000A",
               "CREATE TABLE T000A (ID INT, BOOLEAN_ BOOLEAN, PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000ABean.class);
    Connection conn = _persistence.getConnection();

    boolean f = true;
    T000ABean bean = _persistence.newInstance(T000ABean.class);
    bean.setId(1);
    bean.setBoolean(f);

    conn.insert(bean);
    conn.commit();

    bean = (T000ABean) conn.load(T000ABean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getBoolean());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000ABean.class);
    Connection conn = _persistence.getConnection();

    boolean f1 = true;
    boolean f2 = false;

    T000ABean bean = _persistence.newInstance(T000ABean.class);
    bean.setId(1);
    bean.setBoolean(f1);

    conn.insert(bean);
    conn.commit();

    bean.setBoolean(f2);
    conn.update(bean);
    conn.commit();

    bean = (T000ABean) conn.load(T000ABean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getBoolean());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000A")
  public static interface T000ABean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "BOOLEAN_")
    public boolean getBoolean();

    public void setBoolean(boolean value);
  }
}
