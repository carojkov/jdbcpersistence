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

public class T0001 extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T0001",
               "CREATE TABLE T0001 (ID1 INT, ID2 INT, PRIMARY KEY(ID1, ID2))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T0001Bean.class);
    Connection conn = _persistence.getConnection();
    T0001Bean bean = _persistence.newInstance(T0001Bean.class);
    bean.setId1(1);
    bean.setId2(2);
    conn.insert(bean);
    conn.commit();

    bean = (T0001Bean) conn.load(T0001Bean.class, 1, 2);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId1());
    Assert.assertEquals(2, bean.getId2());

    conn.close();
  }

  @Test
  public void delete() throws SQLException
  {
    _persistence.register(T0001Bean.class);
    Connection conn = _persistence.getConnection();
    T0001Bean bean = _persistence.newInstance(T0001Bean.class);
    bean.setId1(1);
    bean.setId2(2);
    conn.insert(bean);
    conn.commit();

    conn.delete(bean);
    conn.commit();

    bean = (T0001Bean) conn.load(T0001Bean.class, 1, 2);

    Assert.assertNull(bean);

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T0001")
  public static interface T0001Bean
  {
    @Column(name = "ID1")
    @Id(0)
    public int getId1();

    public void setId1(int id);

    @Column(name = "ID2")
    @Id(1)
    public int getId2();

    public void setId2(int id);

  }
}

