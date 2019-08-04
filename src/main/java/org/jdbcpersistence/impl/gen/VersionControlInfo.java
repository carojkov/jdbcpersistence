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

package org.jdbcpersistence.impl.gen;

import org.jdbcpersistence.MappedClass;

import java.lang.reflect.Method;

public class VersionControlInfo
{
  MappedClass.MappedAttribute column;
  int versionIdx;
  public Class returnClass;
  Method getter;
  Method setter;

  public VersionControlInfo(MappedClass.MappedAttribute column)
  {
    if (column == null)
      throw new IllegalArgumentException(
        "Version Column can not have a value of null in a call to VersionControlInfo constructor");
    if (column.getGetter() == null)
      throw new IllegalArgumentException("Version Column must have a getter");
    this.column = column;
    getter = column.getGetter();
    setter = column.getSetter();
    returnClass = getter.getReturnType();
    if (!(returnClass.equals(int.class) || returnClass.equals(long.class)))
      throw new RuntimeException(
        "Only types long and int are supported for Version Columns");
  }
}
