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
  private Class _bean;
  private Class _persistor;

  private MappedClass _mappedClass;
  private boolean _locatorsUpdateCopy;
  private boolean _isOracle;
  private PersistenceClassLoader _classLoader;
  boolean _isUseExecute;

  public JDBCPersistorGenerator(final Class cl,
                                final MappedClass jdbcMap,
                                final boolean _isLocatorsUpdateCopy,
                                final boolean isOracle,
                                PersistenceClassLoader classLoader,
                                boolean isUseExecute)
  {
    _bean = cl;
    _mappedClass = jdbcMap;
    _locatorsUpdateCopy = _isLocatorsUpdateCopy;
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
  }

  @Override
  public void generateBody()
  {
    try {
      Class persistorSuperClass = Object.class;

      if (!Persistor.class.equals(_mappedClass.getPersistorClass())) {
        persistorSuperClass = _mappedClass.getPersistorClass();
      }

      final ClassWriter cw = new ClassWriter(false);
      final String className = "org/jdbcpersistence/generated/" +
                               CodeGenUtils.getShortName(_bean) +
                               "JDBCPersistor";
      cw.visit(Constants.V1_5,
               ACC_PUBLIC | ACC_FINAL,
               className,
               Type.getInternalName(persistorSuperClass),
               new String[]{Type.getInternalName(Persistor.class)},
               null);
      cw.visitField(ACC_PRIVATE | ACC_STATIC,
                    PersistorGenerator.FN_DUMMY_BYTES,
                    Type.getDescriptor(byte[].class),
                    null,
                    null);
      cw.visitField(ACC_PRIVATE | ACC_STATIC,
                    PersistorGenerator.FN_DUMMY_STRING,
                    Type.getDescriptor(String.class),
                    " ",
                    null);

      PersistorGenerator.writeStaticInit(cw, className);//

      PersistorGenerator.writeInit(cw, className, persistorSuperClass);//

      if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_LOAD)) {
        PersistorGenerator.writeSelect(_bean, cw, _mappedClass);
      }
      if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_INSERT)) {
        writeInsert(_bean,
                    cw,
                    className,
                    _mappedClass,
                    _locatorsUpdateCopy,
                    _isOracle,
                    false,
                    _isUseExecute);
      }
      if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_BATCH_INSERT)) {
        writeInsert(_bean,
                    cw,
                    className,
                    _mappedClass,
                    _locatorsUpdateCopy,
                    _isOracle,
                    true,
                    false);
      }
      if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_UPDATE)) {
        writeUpdate(_bean,
                    cw,
                    className,
                    _mappedClass,
                    _locatorsUpdateCopy,
                    _isOracle,
                    false,
                    _isUseExecute);
      }
      if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_BATCH_UPDATE)) {
        writeUpdate(_bean,
                    cw,
                    className,
                    _mappedClass,
                    _locatorsUpdateCopy,
                    _isOracle,
                    true,
                    false);
      }
      if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_DELETE)) {
        writeDelete(_bean,
                    cw,
                    className,
                    _mappedClass,
                    false,
                    _isUseExecute);
      }
      if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                              PersistorGenerator.M_JDBCP_BATCH_DELETE)) {
        writeDelete(_bean,
                    cw,
                    className,
                    _mappedClass,
                    true,
                    false);
      }
      final byte[] classBytes = cw.toByteArray();
      if ("true".equalsIgnoreCase(System.getProperty("jdbcpersistence.verbose"))) {
        CodeGenUtils.writeToFile(className, classBytes);
        CodeGenUtils.echo(className);
      }

      Class result = _classLoader.define(className.replace('/', '.'),
                                         classBytes);
      //
      _persistor = result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
