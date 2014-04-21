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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

class MethodsForResultSet
{
  public static Method getObjectByColumnIndex;
  public static Method getBooleanByColumnIndex;
  public static Method getByteByColumnIndex;
  public static Method getShortByColumnIndex;
  public static Method getIntByColumnIndex;
  public static Method getLongByColumnIndex;
  public static Method getFloatByColumnIndex;
  public static Method getDoubleByColumnIndex;
  public static Method getBytesByColumnIndex;
  public static Method getArrayByColumnIndex;
  public static Method getURLByColumnIndex;
  public static Method getRefByColumnIndex;
  public static Method getDateByColumnIndex;
  public static Method getTimeByColumnIndex;
  public static Method getBigDecimalByColumnIndex;
  public static Method getBlobByColumnIndex;
  public static Method getClobByColumnIndex;
  public static Method getStringByColumnIndex;
  public static Method getTimestampByColumnIndex;
  public static Method getBinaryStreamByColumnIndex;
  public static Method getAsciiStreamByColumnIndex;
  public static Method getCharacterStreamByColumnIndex;
  public static Method getUnicodeStreamByColumnIndex;

  public static Method getObjectByColumnName;
  public static Method getBooleanByColumnName;
  public static Method getByteByColumnName;
  public static Method getShortByColumnName;
  public static Method getIntByColumnName;
  public static Method getLongByColumnName;
  public static Method getFloatByColumnName;
  public static Method getDoubleByColumnName;
  public static Method getBytesByColumnName;
  public static Method getArrayByColumnName;
  public static Method getURLByColumnName;
  public static Method getRefByColumnName;
  public static Method getDateByColumnName;
  public static Method getTimeByColumnName;
  public static Method getBigDecimalByColumnName;
  public static Method getBlobByColumnName;
  public static Method getClobByColumnName;
  public static Method getStringByColumnName;
  public static Method getTimestampByColumnName;
  public static Method getBinaryStreamByColumnName;
  public static Method getAsciiStreamByColumnName;
  public static Method getCharacterStreamByColumnName;
  public static Method getUnicodeStreamByColumnName;

  public static Method next;
  public static Method wasNull;

  private static Map<Class,Method> typeToGetterByColumnIndex
    = new HashMap<Class,Method>();

  private static Map<Class,Method> typeToGetterByColumnName
    = new HashMap<Class,Method>();

  static {
    initialize();
  }

  private static void initialize()
  {
    try {
      initializeReflectedMethods();
      initializeTypToGetterMaps();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static void initializeReflectedMethods() throws NoSuchMethodException
  {
    getObjectByColumnIndex = ResultSet.class.getMethod("getObject",
                                                       new Class[]{int.class});
    getBooleanByColumnIndex = ResultSet.class.getMethod("getBoolean",
                                                        new Class[]{int.class});
    getByteByColumnIndex = ResultSet.class.getMethod("getByte",
                                                     new Class[]{int.class});
    getShortByColumnIndex = ResultSet.class.getMethod("getShort",
                                                      new Class[]{int.class});
    getIntByColumnIndex = ResultSet.class.getMethod("getInt",
                                                    new Class[]{int.class});
    getLongByColumnIndex = ResultSet.class.getMethod("getLong",
                                                     new Class[]{int.class});
    getFloatByColumnIndex = ResultSet.class.getMethod("getFloat",
                                                      new Class[]{int.class});
    getDoubleByColumnIndex = ResultSet.class.getMethod("getDouble",
                                                       new Class[]{int.class});
    getBytesByColumnIndex = ResultSet.class.getMethod("getBytes",
                                                      new Class[]{int.class});
    getArrayByColumnIndex = ResultSet.class.getMethod("getArray",
                                                      new Class[]{int.class});
    getURLByColumnIndex = ResultSet.class.getMethod("getURL",
                                                    new Class[]{int.class});
    getRefByColumnIndex = ResultSet.class.getMethod("getRef",
                                                    new Class[]{int.class});
    getDateByColumnIndex = ResultSet.class.getMethod("getDate",
                                                     new Class[]{int.class});
    getTimeByColumnIndex = ResultSet.class.getMethod("getTime",
                                                     new Class[]{int.class});
    getBigDecimalByColumnIndex = ResultSet.class.getMethod("getBigDecimal",
                                                           new Class[]{int.class});
    getBlobByColumnIndex = ResultSet.class.getMethod("getBlob",
                                                     new Class[]{int.class});
    getClobByColumnIndex = ResultSet.class.getMethod("getClob",
                                                     new Class[]{int.class});
    getStringByColumnIndex = ResultSet.class.getMethod("getString",
                                                       new Class[]{int.class});
    getTimestampByColumnIndex = ResultSet.class.getMethod("getTimestamp",
                                                          new Class[]{int.class});
    getBinaryStreamByColumnIndex = ResultSet.class.getMethod("getBinaryStream",
                                                             new Class[]{int.class});
    getAsciiStreamByColumnIndex = ResultSet.class.getMethod("getAsciiStream",
                                                            new Class[]{int.class});
    getCharacterStreamByColumnIndex = ResultSet.class.getMethod(
      "getCharacterStream",
      new Class[]{int.class});
    getUnicodeStreamByColumnIndex
      = ResultSet.class.getMethod("getUnicodeStream",
                                  new Class[]{int.class});
    next = ResultSet.class.getMethod("next", new Class[0]);

    wasNull = ResultSet.class.getMethod("wasNull", new Class[0]);

    //java.sql.ResultSet methods for string
    getObjectByColumnName = ResultSet.class.getMethod("getObject",
                                                      new Class[]{String.class});
    getBooleanByColumnName = ResultSet.class.getMethod("getBoolean",
                                                       new Class[]{String.class});
    getByteByColumnName = ResultSet.class.getMethod("getByte",
                                                    new Class[]{String.class});
    getShortByColumnName = ResultSet.class.getMethod("getShort",
                                                     new Class[]{String.class});
    getIntByColumnName = ResultSet.class.getMethod("getInt",
                                                   new Class[]{String.class});
    getLongByColumnName = ResultSet.class.getMethod("getLong",
                                                    new Class[]{String.class});
    getFloatByColumnName = ResultSet.class.getMethod("getFloat",
                                                     new Class[]{String.class});
    getDoubleByColumnName = ResultSet.class.getMethod("getDouble",
                                                      new Class[]{String.class});
    getBytesByColumnName = ResultSet.class.getMethod("getBytes",
                                                     new Class[]{String.class});
    getArrayByColumnName = ResultSet.class.getMethod("getArray",
                                                     new Class[]{String.class});
    getURLByColumnName = ResultSet.class.getMethod("getURL",
                                                   new Class[]{String.class});
    getRefByColumnName = ResultSet.class.getMethod("getRef",
                                                   new Class[]{String.class});
    getDateByColumnName = ResultSet.class.getMethod("getDate",
                                                    new Class[]{String.class});
    getTimeByColumnName = ResultSet.class.getMethod("getTime",
                                                    new Class[]{String.class});
    getBigDecimalByColumnName = ResultSet.class.getMethod("getBigDecimal",
                                                          new Class[]{String.class});
    getBlobByColumnName = ResultSet.class.getMethod("getBlob",
                                                    new Class[]{String.class});
    getClobByColumnName = ResultSet.class.getMethod("getClob",
                                                    new Class[]{String.class});
    getStringByColumnName = ResultSet.class.getMethod("getString",
                                                      new Class[]{String.class});
    getTimestampByColumnName = ResultSet.class.getMethod("getTimestamp",
                                                         new Class[]{String.class});
    getBinaryStreamByColumnName = ResultSet.class.getMethod("getBinaryStream",
                                                            new Class[]{String.class});
    getAsciiStreamByColumnName = ResultSet.class.getMethod("getAsciiStream",
                                                           new Class[]{String.class});
    getCharacterStreamByColumnName = ResultSet.class.getMethod(
      "getCharacterStream",
      new Class[]{String.class});
    getUnicodeStreamByColumnName = ResultSet.class.getMethod("getUnicodeStream",
                                                             new Class[]{String.class});
  }

  private static void initializeTypToGetterMaps()
  {
    typeToGetterByColumnIndex.put(Time.class, getTimeByColumnIndex);
    typeToGetterByColumnName.put(Time.class, getTimeByColumnName);

    typeToGetterByColumnIndex.put(String.class, getStringByColumnIndex);
    typeToGetterByColumnName.put(String.class, getStringByColumnName);

    typeToGetterByColumnIndex.put(Boolean.class, getBooleanByColumnIndex);
    typeToGetterByColumnName.put(Boolean.class, getBooleanByColumnName);

    typeToGetterByColumnIndex.put(Byte.class, getByteByColumnIndex);
    typeToGetterByColumnName.put(Byte.class, getByteByColumnName);

    typeToGetterByColumnIndex.put(Short.class, getShortByColumnIndex);
    typeToGetterByColumnName.put(Short.class, getShortByColumnName);

    typeToGetterByColumnIndex.put(Integer.class, getIntByColumnIndex);
    typeToGetterByColumnName.put(Integer.class, getIntByColumnName);

    typeToGetterByColumnIndex.put(Long.class, getLongByColumnIndex);
    typeToGetterByColumnName.put(Long.class, getLongByColumnName);

    typeToGetterByColumnIndex.put(Float.class, getFloatByColumnIndex);
    typeToGetterByColumnName.put(Float.class, getFloatByColumnName);

    typeToGetterByColumnIndex.put(Double.class, getDoubleByColumnIndex);
    typeToGetterByColumnName.put(Double.class, getDoubleByColumnName);

    typeToGetterByColumnIndex.put(Timestamp.class, getTimestampByColumnIndex);
    typeToGetterByColumnName.put(Timestamp.class, getTimestampByColumnName);

    typeToGetterByColumnIndex.put(Date.class, getDateByColumnIndex);
    typeToGetterByColumnName.put(Date.class, getDateByColumnName);

    typeToGetterByColumnIndex.put(BigDecimal.class, getBigDecimalByColumnIndex);
    typeToGetterByColumnName.put(BigDecimal.class, getBigDecimalByColumnName);

    typeToGetterByColumnIndex.put(URL.class, getURLByColumnIndex);
    typeToGetterByColumnName.put(URL.class, getURLByColumnName);

    typeToGetterByColumnIndex.put(byte[].class, getBytesByColumnIndex);
    typeToGetterByColumnName.put(byte[].class, getBytesByColumnName);
  }

  static final Method findGetter(Class type,
                                 boolean useColumnNames)
  {
    if (type.isPrimitive())
      type = JavaTypesMap.toWrapper(type);

    Method method;

    if (useColumnNames)
      method = typeToGetterByColumnName.get(type);
    else
      method = typeToGetterByColumnIndex.get(type);

    if (method == null)
      throw new IllegalArgumentException("Type [" +
                                         type +
                                         "] is not supported at this time");
    return method;
  }

}