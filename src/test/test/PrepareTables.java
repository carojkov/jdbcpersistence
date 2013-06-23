package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by IntelliJ IDEA. User: CA031443 Date: 22-Aug-2006 Time: 3:54:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrepareTables
{
  public static void prepareTables(Connection conn)
  {
    try {
      String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();

      prepareTables(conn, dbName);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }

  public static void prepareTables(Connection conn, String dbName)
  {
    BufferedReader reader = null;
    try {
      File file = new File("src/test/ddl/" + dbName + ".ddl");
      System.out.println("PrepareTables.prepareTables " + file.getCanonicalPath());
      reader = new BufferedReader(new FileReader(file));
      StringBuffer buffer = null;
      String ln = null;
      boolean inComment = false;
      while ((ln = reader.readLine()) != null) {
        ln = ln.trim();
        if (ln.startsWith("/*") && ln.endsWith("*/")) {
          continue;
        }
        if (ln.startsWith("/*")) {
          inComment = true;
          buffer = null;
          continue;
        }
        if (ln.endsWith("*/")) {
          inComment = false;
          continue;
        }
        if (!inComment) {
          if (ln.endsWith(";")) {
            char[] chars = ln.toCharArray();
            if (buffer == null) buffer = new StringBuffer();
            buffer.append(chars, 0, chars.length - 1);
            executeSQL(conn, buffer.toString());
            buffer = new StringBuffer();
          }
          else {
            if (buffer == null) buffer = new StringBuffer();
            buffer.append(ln).append("\r\n");
          }
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } finally {
      try {
        reader.close();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void executeSQL(Connection conn, String sql)
  {
    Statement st = null;
    try {
      conn.setAutoCommit(false);
      st = conn.createStatement();
      st.executeUpdate(sql);
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (st != null) try {
        st.close();
      } catch (Throwable e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
    }
  }
}
