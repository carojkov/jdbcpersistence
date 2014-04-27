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

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * title: java.math.BigDecimal->Types.DECIMAL
 */
public class T000h extends BaseTest
{
  @Before
  public void myInit() throws SQLException
  {
    dropCreate("DROP TABLE T000H",
               "CREATE TABLE T000H (ID INT, DECIMAL_ DECIMAL(31,3), PRIMARY KEY(ID))");
  }

  @Test
  public void insert() throws SQLException
  {
    _persistence.register(T000HBean.class);
    Connection conn = _persistence.getConnection();

    BigDecimal f = new BigDecimal("9999999999999999999999999999.999");
    T000HBean bean = _persistence.newInstance(T000HBean.class);
    bean.setId(1);
    bean.setBigDecimal(f);

    conn.insert(bean);
    conn.commit();

    bean = (T000HBean) conn.load(T000HBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f, bean.getBigDecimal());

    conn.close();
  }

  @Test
  public void update() throws SQLException
  {
    _persistence.register(T000HBean.class);
    Connection conn = _persistence.getConnection();

    BigDecimal f1 = new BigDecimal("9999999999999999999999999999.999");
    BigDecimal f2 = new BigDecimal("9999999999999999999999999999.998");

    T000HBean bean = _persistence.newInstance(T000HBean.class);
    bean.setId(1);
    bean.setBigDecimal(f1);

    conn.insert(bean);
    conn.commit();

    bean.setBigDecimal(f2);
    conn.update(bean);
    conn.commit();

    bean = (T000HBean) conn.load(T000HBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertEquals(f2, bean.getBigDecimal());

    conn.close();
  }

  @Test
  public void updateWithNull() throws SQLException
  {
    _persistence.register(T000HBean.class);
    Connection conn = _persistence.getConnection();

    T000HBean bean = _persistence.newInstance(T000HBean.class);
    bean.setId(1);
    bean.setBigDecimal(new BigDecimal("9999999999999999999999999999.999"));

    conn.insert(bean);
    conn.commit();

    bean.setBigDecimal(null);
    conn.update(bean);
    conn.commit();

    bean = (T000HBean) conn.load(T000HBean.class, 1);

    Assert.assertNotNull(bean);
    Assert.assertEquals(1, bean.getId());
    Assert.assertNull(bean.getBigDecimal());

    conn.close();
  }

  @After
  public void myDestroy()
  {

  }

  @Entity(name = "T000H")
  public static interface T000HBean
  {
    @Column(name = "ID")
    @Id()
    public int getId();

    public void setId(int id);

    @Column(name = "DECIMAL_")
    public BigDecimal getBigDecimal();

    public void setBigDecimal(BigDecimal value);
  }
}
