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
 * title: Read Primitives into a List
 */
public class T0002 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0002",
               "CREATE TABLE T0002 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");

    _persistence.register(T0002Bean.class);
  }

  @Test
  public void query() throws SQLException
  {
    Connection conn = _persistence.getConnection();
    T0002Bean[] beans = new T0002Bean[5];

    for (int i = 0; i < 5; i++) {
      T0002Bean bean = _persistence.newInstance(T0002Bean.class);
      bean.setId(i);
      bean.setData("value-" + i);
      beans[i] = bean;
    }

    conn.insert(beans);
    conn.commit();

    Query<Integer> query = new Query<>("SELECT ID FROM T0002",
                                       Integer.class,
                                       null);
    List<Integer> list = new ArrayList<>();
    conn.executeQuery(query, list);

    for (int i = 0; i < list.size(); i++) {
      Integer x = list.get(i);
      Assert.assertEquals(i, x.intValue());
    }

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

    @Column(name = "DATA")
    public String getData();

    public void setData(String data);
  }
}

