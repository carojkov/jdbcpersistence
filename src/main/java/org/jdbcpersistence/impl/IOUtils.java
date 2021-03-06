/**
 * JDBCPersistence framework for java
 *   Copyright (C) 2004-2014 Alex Rojkov
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
 * */
package org.jdbcpersistence.impl;

import java.io.*;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IOUtils
{
  public final static int BUFFER_SIZE = 4096;
  public final static Method M_READ_FROM_READER_TO_STRING;
  public final static Method M_READ_FROM_INPUTSTREAM_TO_BYTES;
  public final static Logger log = Logger.getLogger(IOUtils.class.getName());

  static {
    try {
      M_READ_FROM_READER_TO_STRING = IOUtils.class.getMethod("read",
                                                             new Class[]{Reader.class});
      M_READ_FROM_INPUTSTREAM_TO_BYTES = IOUtils.class.getMethod("read",
                                                                 new Class[]{
                                                                   InputStream.class});
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public final static byte[] read(final InputStream inputStream)
    throws IOException
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
    copy(inputStream, baos);
    return baos.toByteArray();
  }

  public final static void copy(final InputStream from, final OutputStream to)
    throws IOException
  {
    final byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = from.read(buffer)) != -1) {
      to.write(buffer, 0, bytesRead);
      to.flush();
    }
  }

  public final static String read(final Reader reader)
    throws IOException
  {
    final StringWriter writer = new StringWriter(BUFFER_SIZE);
    copy(reader, writer);
    return writer.getBuffer().toString();
  }

  public final static void copy(final Reader from, final Writer to)
    throws IOException
  {
    final char[] buffer = new char[BUFFER_SIZE];
    int charsRead;
    while ((charsRead = from.read(buffer)) != -1) {
      to.write(buffer, 0, charsRead);
      to.flush();
    }
  }

  public final static void close(Closeable closeable)
  {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Throwable e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }

  public final static void close(Closeable... closeable)
  {
    for (Closeable c : closeable) {
      if (c != null) {
        try {
          c.close();
        } catch (Throwable e) {
          log.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
}
