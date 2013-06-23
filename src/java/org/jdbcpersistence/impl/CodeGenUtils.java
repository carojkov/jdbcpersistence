/**
 * JDBCPersistence framework for java
 *   Copyright (C) 2004-2010 Alex Rojkov
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    You can contact me by email jdbcpersistence   a t   gmail    d o t    com
 **/

package org.jdbcpersistence.impl;

import java.io.*;

/**
 * Author Alex Rojkov Date: 1-Oct-2005 Time: 11:18:22 AM
 */
class CodeGenUtils {
  public final static String getShortName(final Class clazz)
  {
    final String name = clazz.getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  public final static String getShortName(String className)
  {
    return className.substring(className.lastIndexOf('/') + 1);
  }

  public static void echo(String className)
  {
    try {
      final Process p = Runtime.getRuntime()
        .exec("/Users/alex/bin/jad -o " + className.replace('/', '_') + ".class");
      attachToSystemOut(p.getInputStream());
      attachToSystemOut(p.getErrorStream());
      p.waitFor();
      echo(new FileReader(CodeGenUtils.getShortName(className) + ".jad"),
           new PrintWriter(System.out));
    }
    catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private final static void attachToSystemOut(final InputStream is)
  {
    new Thread() {
      public void run()
      {
        try {
          echo(new InputStreamReader(is), new PrintWriter(System.out));
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }.start();
  }

  public final static void echo(final Reader r, final Writer w)
    throws IOException
  {
    final char[] buffer = new char[4096];
    int charsRead;
    while ((charsRead = r.read(buffer)) != -1) {
      if (w != null) {
        w.write(buffer, 0, charsRead);
        w.flush();
      }
    }
  }

  public static void writeToFile(String className, byte[] bytes)
    throws IOException
  {
    FileOutputStream fos = null;

    try {
      fos = new FileOutputStream(className.replace('/', '_') + ".class");
      fos.write(bytes);
      fos.flush();
    }
    finally {
      if (fos != null) {
        fos.close();
      }
    }
  }
}
