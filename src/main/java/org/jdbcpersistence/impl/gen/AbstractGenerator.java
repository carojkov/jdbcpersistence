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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGenerator implements Generator
{
  private List<Generator> _generators = new ArrayList<>();

  @Override
  public void generateBody()
  {
    for (Generator generator : _generators) {
      generator.generateBody();
    }
  }
}

//title for class is its name and extends/implements clause

//title for method is its name, args and throws clause

//head for class is its variables static and instance

//head for method is its variables

//head for try block is try {

//tail for try block is catch (Exception e) {...}

