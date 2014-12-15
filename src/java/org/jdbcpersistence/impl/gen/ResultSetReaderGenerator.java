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
import org.jdbcpersistence.ResultSetReader;
import org.jdbcpersistence.impl.CodeGenUtils;
import org.jdbcpersistence.impl.PersistenceClassLoader;
import org.jdbcpersistence.impl.PersistorGenerator;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;

public class ResultSetReaderGenerator implements Generator, Constants
{
  private final Class _bean;
  private final MappedClass _mappedClass;
  private String[] _columnNames;
  private String _query;
  private final boolean _isLocatorsUpdateCopy;
  private final boolean _isOracle;
  private PersistenceClassLoader _classLoader;

  private final Class _readerSuperClass;
  private final ClassWriter _classWriter;
  private final String _readerClassName;

  private Class _reader;

  public ResultSetReaderGenerator(Class bean,
                                  MappedClass _mappedClass,
                                  String[] columnNames,
                                  String query,
                                  boolean _isLocatorsUpdateCopy,
                                  boolean _isOracle,
                                  PersistenceClassLoader _classLoader)
  {
    this._bean = bean;
    this._mappedClass = _mappedClass;
    this._columnNames = columnNames;
    this._query = query;
    this._isLocatorsUpdateCopy = _isLocatorsUpdateCopy;
    this._isOracle = _isOracle;
    this._classLoader = _classLoader;

    _readerSuperClass = Object.class;
    _classWriter = new ClassWriter(false);
    _readerClassName = "org/jdbcpersistence/generated/" +
                       CodeGenUtils.getShortName(bean) +
                       "JDBCResultSetReader_" +
                       makeClassName(query);
  }

  public void generate()
  {
    generateHead();
    generateBody();
    generateTail();
  }

  @Override
  public void generateHead()
  {
    _classWriter.visit(Constants.V1_3,
                       ACC_PUBLIC | ACC_FINAL,
                       _readerClassName,
                       Type.getInternalName(_readerSuperClass),
                       new String[]{Type.getInternalName(ResultSetReader.class)},
                       null);

  }

  @Override
  public void generateBody()
  {

    PersistorGenerator.writeInit(_classWriter,
                                 _readerClassName,
                                 _readerSuperClass);

    if (!PersistorGenerator.isMethodPresent(_readerSuperClass,
                                            PersistorGenerator.M_JDBCP_LOAD_FROM_RS)) {
      SelectFromResultSetGenerator generator
        = new SelectFromResultSetGenerator(_bean,
                                           _classWriter,
                                           _mappedClass,
                                           _columnNames);

      generator.generate();
    }

    final byte[] classBytes = _classWriter.toByteArray();
    if ("true".equalsIgnoreCase(System.getProperty("jdbcpersistence.verbose"))) {
      CodeGenUtils.writeToFile(_readerClassName, classBytes);
      CodeGenUtils.echo(_readerClassName);
    }

    _reader = _classLoader.define(_readerClassName.replace('/', '.'),
                                  classBytes);
  }

  @Override
  public void generateTail()
  {

  }

  private static final String makeClassName(String sql)
  {
    char[] temp = sql.toCharArray();
    for (int i = 0; i < temp.length; i++) {
      char c = temp[i];
      if (Character.isLetterOrDigit(c) || c == '_') continue;
      temp[i] = '_';
    }
    return new String(temp);
  }

  public Class getReader()
  {
    return _reader;
  }
}
