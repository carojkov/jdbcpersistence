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
import org.jdbcpersistence.impl.PersistenceClassLoader;
import org.jdbcpersistence.impl.PersistorGenerator;

public class JDBCPersistorGenerator implements Generator
{
  private Class _bean;
  private Class _persistor;

  private MappedClass _mappedClass;
  private boolean _locatorsUpdateCopy;
  private boolean _isOracle;
  private PersistenceClassLoader _classLoader;
  boolean _isUseExecute;

  public JDBCPersistorGenerator(final Class cl,
                                final MappedClass jdbcMap,
                                final boolean locatorsUpdateCopy,
                                final boolean oracle,
                                PersistenceClassLoader classLoader,
                                boolean useExecute)
  {
    _bean = cl;
    _mappedClass = jdbcMap;
    _locatorsUpdateCopy = locatorsUpdateCopy;
    _isOracle = oracle;
    _classLoader = classLoader;
    _isUseExecute = useExecute;
  }

  public void generate() {
    generateHead();
    generateBody();
    generateTail();
  }

  @Override
  public void generateHead()
  {
    try {
      _persistor = PersistorGenerator.generateJDBCPersistor(_bean,
                                                            _mappedClass,
                                                            _locatorsUpdateCopy,
                                                            _isOracle,
                                                            _classLoader,
                                                            false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void generateBody()
  {

  }

  @Override
  public void generateTail()
  {

  }

  public Class getPersistor()
  {
    return _persistor;
  }
}
