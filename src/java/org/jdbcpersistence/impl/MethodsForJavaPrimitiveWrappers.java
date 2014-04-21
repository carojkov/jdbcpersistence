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
import java.util.HashMap;
import java.util.Map;

class MethodsForJavaPrimitiveWrappers
{
  public static Method booleanValue;
  public static Method byteValue;
  public static Method shortValue;
  public static Method intValue;
  public static Method longValue;
  public static Method floatValue;
  public static Method doubleValue;

  public static final Map WRAPPER_TO_PRIMITIVE = new HashMap();
  public static final Map WRAPPER_TO_PRIM_GETTER = new HashMap();

  static {
    initialize();
  }

  private static void initialize()
  {
    try {
      initializeReflectedMethods();
      initializeWrapperGettersMap();
      initializeWrapperToPrimitiveMap();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static void initializeReflectedMethods()
    throws NoSuchMethodException
  {
    booleanValue = Boolean.class.getMethod("booleanValue",
                                           new Class[0]);
    byteValue = Byte.class.getMethod("byteValue", new Class[0]);
    shortValue = Short.class.getMethod("shortValue", new Class[0]);
    intValue = Integer.class.getMethod("intValue", new Class[0]);
    longValue = Long.class.getMethod("longValue", new Class[0]);
    floatValue = Float.class.getMethod("floatValue", new Class[0]);
    doubleValue = Double.class.getMethod("doubleValue",
                                         new Class[0]);
  }

  private static void initializeWrapperGettersMap()
  {
    WRAPPER_TO_PRIM_GETTER.put(Boolean.class, booleanValue);
    WRAPPER_TO_PRIM_GETTER.put(Byte.class, byteValue);
    WRAPPER_TO_PRIM_GETTER.put(Short.class, shortValue);
    WRAPPER_TO_PRIM_GETTER.put(Integer.class, intValue);
    WRAPPER_TO_PRIM_GETTER.put(Long.class, longValue);
    WRAPPER_TO_PRIM_GETTER.put(Float.class, floatValue);
    WRAPPER_TO_PRIM_GETTER.put(Double.class, doubleValue);
  }

  private static void initializeWrapperToPrimitiveMap()
  {
    WRAPPER_TO_PRIMITIVE.put(Boolean.class, boolean.class);
    WRAPPER_TO_PRIMITIVE.put(Byte.class, byte.class);
    WRAPPER_TO_PRIMITIVE.put(Short.class, short.class);
    WRAPPER_TO_PRIMITIVE.put(Integer.class, int.class);
    WRAPPER_TO_PRIMITIVE.put(Long.class, long.class);
    WRAPPER_TO_PRIMITIVE.put(Float.class, float.class);
    WRAPPER_TO_PRIMITIVE.put(Double.class, double.class);
  }
}