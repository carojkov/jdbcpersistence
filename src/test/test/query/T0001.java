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
import java.util.Map;

/**
 * title: Read into a Map
 */
public class T0001 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0001",
               "CREATE TABLE T0001 (ID INT, DATA VARCHAR(16), PRIMARY KEY(ID))");

    _persistence.register(T0001Bean.class);
  }

  @Test
  public void query() throws SQLException
  {
    Connection conn = _persistence.getConnection();
    T0001Bean[] beans = new T0001Bean[5];

    for (int i = 0; i < 5; i++) {
      T0001Bean bean = _persistence.newInstance(T0001Bean.class);
      bean.setId(i);
      bean.setData("value-" + i);
      beans[i] = bean;
    }

    conn.insert(beans);
    conn.commit();

    Query<Map> query = new Query("SELECT * FROM T0001", Map.class, null);
    List<Map> list = new ArrayList<>();
    conn.executeQuery(query, list);

    for (int i = 0; i < list.size(); i++) {
      Map map = list.get(i);
      Assert.assertEquals(i, map.get("ID"));
      Assert.assertEquals("value-" + i, map.get("DATA"));
    }

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0001")
  public static interface T0001Bean
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

