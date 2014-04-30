package test.query;

import org.jdbcpersistence.Column;
import org.jdbcpersistence.Connection;
import org.jdbcpersistence.Entity;
import org.jdbcpersistence.Id;
import org.jdbcpersistence.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.BaseTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * title: Basic ResultSetReader
 */
public class T0000 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0000",
               "CREATE TABLE T0000 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");

    _persistence.register(T0000Bean.class);
  }

  @Test
  public void query() throws SQLException
  {
    Connection conn = _persistence.getConnection();
    T0000Bean[] beans = new T0000Bean[5];

    for (int i = 0; i < 5; i++) {
      T0000Bean bean = _persistence.newInstance(T0000Bean.class);
      bean.setId(i);
      bean.setData("value-" + i);
      beans[i] = bean;
    }

    conn.insert(beans);
    conn.commit();

    Query query = new Query("SELECT * FROM T0000", T0000Bean.class, null);
    List list = new ArrayList();
    conn.executeQuery(query, list);

    for (int i = 0; i < list.size(); i++) {
      T0000Bean bean = (T0000Bean) list.get(i);
      Assert.assertEquals(i, bean.getId());
      Assert.assertEquals("value-" + i, bean.getData());
    }

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0000")
  public static interface T0000Bean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DATA")
    public String getData();

    public void setData(String data);
  }
}

