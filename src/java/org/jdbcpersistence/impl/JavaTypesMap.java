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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class JavaTypesMap
{
  public static final Map<Class,Class> toWrapperMap;
  public static final Map<Class,Class> toPrimitiveMap;

  public static Class toWrapper(Class primitive)
  {
    return toWrapperMap.get(primitive);
  }

  public static boolean isPrimitiveWrapper(Class c)
  {
    return toPrimitiveMap.containsKey(c);
  }

  static {
    Map<Class,Class> map = new HashMap<>();
    map.put(boolean.class, Boolean.class);
    map.put(byte.class, Byte.class);
    map.put(short.class, Short.class);
    map.put(char.class, Character.class);
    map.put(int.class, Integer.class);
    map.put(long.class, Long.class);
    map.put(float.class, Float.class);
    map.put(double.class, Double.class);

    toWrapperMap = Collections.unmodifiableMap(map);

    map = new HashMap<Class,Class>();

    for (Map.Entry<Class,Class> entry : toWrapperMap.entrySet()) {
      map.put(entry.getValue(), entry.getKey());
    }

    toPrimitiveMap = Collections.unmodifiableMap(map);
  }
}
