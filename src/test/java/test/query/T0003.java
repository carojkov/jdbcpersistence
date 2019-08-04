package test.query;

import org.jdbcpersistence.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.BaseTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * title: Read String Primitives into a List
 */
public class T0003 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0003",
               "CREATE TABLE T0003 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");

    _persistence.register(T0003Bean.class);
  }

  @Test
  public void query() throws SQLException
  {
    Connection conn = _persistence.getConnection();
    T0003Bean[] beans = new T0003Bean[5];

    for (int i = 0; i < 5; i++) {
      T0003Bean bean = _persistence.newInstance(T0003Bean.class);
      bean.setId(i);
      bean.setData("value-" + i);
      beans[i] = bean;
    }

    conn.insert(beans);
    conn.commit();

    Query<String> query = new Query<>("SELECT DATA FROM T0003",
                                       String.class,
                                       null);
    List<String> list = new ArrayList<>();
    conn.executeQuery(query, list);

    for (int i = 0; i < list.size(); i++) {
      String s = list.get(i);
      Assert.assertEquals("value-" + i, s);
    }

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0003")
  public static interface T0003Bean
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

