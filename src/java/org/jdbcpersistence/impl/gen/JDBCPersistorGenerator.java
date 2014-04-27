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
import org.jdbcpersistence.Persistor;
import org.jdbcpersistence.impl.CodeGenUtils;
import org.jdbcpersistence.impl.PersistenceClassLoader;
import org.jdbcpersistence.impl.PersistorGenerator;
import org.jdbcpersistence.impl.asm.ClassWriter;
import org.jdbcpersistence.impl.asm.Constants;
import org.jdbcpersistence.impl.asm.Type;

import static org.jdbcpersistence.impl.PersistorGenerator.*;

public class JDBCPersistorGenerator implements Generator, Constants
{
  private MappedClass _mappedClass;
  private boolean _isLocatorsUpdateCopy;
  private boolean _isOracle;
  private PersistenceClassLoader _classLoader;
  private boolean _isUseExecute;

  private Class _bean;
  private Class _persistor;
  private Class _persistorSuperClass;
  private ClassWriter _classWriter;
  private String _persistorClassName;

  public JDBCPersistorGenerator(final Class cl,
                                final MappedClass jdbcMap,
                                final boolean _isLocatorsUpdateCopy,
                                final boolean isOracle,
                                PersistenceClassLoader classLoader,
                                boolean isUseExecute)
  {
    _bean = cl;
    _mappedClass = jdbcMap;
    this._isLocatorsUpdateCopy = _isLocatorsUpdateCopy;
    _isOracle = isOracle;
    _classLoader = classLoader;
    this._isUseExecute = isUseExecute;
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
    _persistorSuperClass = Object.class;

    if (!Persistor.class.equals(_mappedClass.getPersistorClass())) {
      _persistorSuperClass = _mappedClass.getPersistorClass();
    }

    _classWriter = new ClassWriter(false);
    _persistorClassName = "org/jdbcpersistence/generated/" +
                          CodeGenUtils.getShortName(_bean) +
                          "JDBCPersistor";
    _classWriter.visit(Constants.V1_5,
                       ACC_PUBLIC | ACC_FINAL,
                       _persistorClassName,
                       Type.getInternalName(_persistorSuperClass),
                       new String[]{Type.getInternalName(Persistor.class)},
                       null);
    _classWriter.visitField(ACC_PRIVATE | ACC_STATIC,
                            PersistorGenerator.FN_DUMMY_BYTES,
                            Type.getDescriptor(byte[].class),
                            null,
                            null);
    _classWriter.visitField(ACC_PRIVATE | ACC_STATIC,
                            PersistorGenerator.FN_DUMMY_STRING,
                            Type.getDescriptor(String.class),
                            " ",
                            null);

    PersistorGenerator.writeStaticInit(_classWriter, _persistorClassName);

    PersistorGenerator.writeInit(_classWriter,
                                 _persistorClassName,
                                 _persistorSuperClass);
  }

  @Override
  public void generateBody()
  {
    try {
      if (!PersistorGenerator.isMethodPresent(_persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_LOAD)) {
        SelectGenerator generator = new SelectGenerator(_bean,
                                                        _mappedClass,
                                                        _classWriter);
        generator.generate();
      }
      if (!PersistorGenerator.isMethodPresent(_persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_INSERT)) {
        InsertGenerator generator = new InsertGenerator(_mappedClass,
                                                        _isLocatorsUpdateCopy,
                                                        _isOracle,
                                                        _isUseExecute,
                                                        false,
                                                        _bean,
                                                        _classWriter,
                                                        _persistorClassName);

        generator.generate();
      }
      if (!PersistorGenerator.isMethodPresent(_persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_BATCH_INSERT)) {
        InsertGenerator generator = new InsertGenerator(_mappedClass,
                                                        _isLocatorsUpdateCopy,
                                                        _isOracle,
                                                        _isUseExecute,
                                                        true,
                                                        _bean,
                                                        _classWriter,
                                                        _persistorClassName);

        generator.generate();
      }
      if (!PersistorGenerator.isMethodPresent(_persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_UPDATE)) {
        writeUpdate(_bean,
                    _classWriter,
                    _persistorClassName,
                    _mappedClass,
                    _isLocatorsUpdateCopy,
                    _isOracle,
                    false,
                    _isUseExecute);
      }
      if (!PersistorGenerator.isMethodPresent(_persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_BATCH_UPDATE)) {
        writeUpdate(_bean,
                    _classWriter,
                    _persistorClassName,
                    _mappedClass,
                    _isLocatorsUpdateCopy,
                    _isOracle,
                    true,
                    false);
      }
      if (!PersistorGenerator.isMethodPresent(_persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_DELETE)) {
        writeDelete(_bean,
                    _classWriter,
                    _persistorClassName,
                    _mappedClass,
                    false,
                    _isUseExecute);
      }
      if (!PersistorGenerator.isMethodPresent(_persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_BATCH_DELETE)) {
        writeDelete(_bean,
                    _classWriter,
                    _persistorClassName,
                    _mappedClass,
                    true,
                    false);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void generateTail()
  {
    final byte[] classBytes = _classWriter.toByteArray();
    if ("true".equalsIgnoreCase(System.getProperty("jdbcpersistence.verbose"))) {
      CodeGenUtils.writeToFile(_persistorClassName, classBytes);
      CodeGenUtils.echo(_persistorClassName);
    }

    Class result = _classLoader.define(_persistorClassName.replace('/', '.'),
                                       classBytes);
    //
    _persistor = result;
  }

  public Class getPersistor()
  {
    return _persistor;
  }
}
