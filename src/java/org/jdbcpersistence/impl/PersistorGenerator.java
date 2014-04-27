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

import org.jdbcpersistence.MappedClass;
import org.jdbcpersistence.Persistor;
import org.jdbcpersistence.ResultSetReader;
import org.jdbcpersistence.impl.asm.ClassWriter;
import org.jdbcpersistence.impl.asm.CodeVisitor;
import org.jdbcpersistence.impl.asm.Constants;
import org.jdbcpersistence.impl.asm.Label;
import org.jdbcpersistence.impl.asm.Type;
import org.jdbcpersistence.impl.gen.CodeInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public final class PersistorGenerator implements Constants
{
  public static final String[] INSERT_DELETE_UPDATE_EXCEPTIONS = new String[]{
    Type.getInternalName(SQLException.class),
    Type.getInternalName(RuntimeException.class)};
  private static final String[] INSERT_DELETE_UPDATE_BATCH_EXCEPTIONS
    = new String[]{Type.getInternalName(SQLException.class),
                   Type.getInternalName(BatchUpdateException.class),
                   Type.getInternalName(RuntimeException.class)};
  public static final String FN_DUMMY_BYTES = "dba_";
  public static final String FN_DUMMY_STRING = "ds_";
  private static final String I_C_N_ORA_SQL_CLOB = "oracle/sql/CLOB";
  private static final String I_C_N_ORA_SQL_BLOB = "oracle/sql/BLOB";
  public static final Method M_JDBCP_INSERT;
  public static final Method M_JDBCP_BATCH_INSERT;
  public static final Method M_JDBCP_UPDATE;
  public static final Method M_JDBCP_BATCH_UPDATE;
  public static final Method M_JDBCP_DELETE;
  public static final Method M_JDBCP_BATCH_DELETE;
  public static final Method M_JDBCP_LOAD;
  private static final Method M_JDBCP_LOAD_FROM_RS;

  static {
    try {
      M_JDBCP_INSERT = Persistor.class.getMethod("insert",
                                                 new Class[]{Connection.class,
                                                             Object.class}
      );
      M_JDBCP_BATCH_INSERT = Persistor.class.getMethod("insert",
                                                       new Class[]{
                                                         Connection.class,
                                                         Object[].class}
      );
      M_JDBCP_UPDATE = Persistor.class.getMethod("update",
                                                 new Class[]{Connection.class,
                                                             Object.class}
      );
      M_JDBCP_BATCH_UPDATE = Persistor.class.getMethod("update",
                                                       new Class[]{
                                                         Connection.class,
                                                         Object[].class}
      );
      M_JDBCP_DELETE = Persistor.class.getMethod("delete",
                                                 new Class[]{Connection.class,
                                                             Object.class}
      );
      M_JDBCP_BATCH_DELETE = Persistor.class.getMethod("delete",
                                                       new Class[]{
                                                         Connection.class,
                                                         Object[].class}
      );
      M_JDBCP_LOAD = Persistor.class.getMethod("load",
                                               new Class[]{Connection.class,
                                                           Object[].class}
      );
      M_JDBCP_LOAD_FROM_RS = ResultSetReader.class.getMethod("read",
                                                             new Class[]{
                                                               ResultSet.class,
                                                               List.class}
      );
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void writeSelectFromResultSet(final Class cl,
                                               final ClassWriter cw,
                                               final MappedClass jdbcMap,
                                               String[] columnNames)
  {
    final CodeVisitor mw = cw.visitMethod(ACC_PUBLIC,
                                          "read",
                                          Type.getMethodDescriptor(Type.getType(
                                                                     java.util.List.class),
                                                                   new Type[]{
                                                                     Type.getType(
                                                                       ResultSet.class),
                                                                     Type.getType(
                                                                       List.class)}
                                          ),
                                          INSERT_DELETE_UPDATE_EXCEPTIONS,
                                          null
    );
    final Label methodStart = new Label();
    final Label methodEnd = new Label();
    mw.visitLabel(methodStart);
    final int rsSelectIdx = 1;
    final int resultListIdx = 2;
    final CodeInfo codeInfo = new CodeInfo();
    codeInfo._varindx = 3;
    //
    final Label beginTryBlock = new Label();
    final Label endTryBlock = new Label();
    final Label catchSqlBlock = new Label();
    final Label catchThrBlock = new Label();
    final Label finallyExceptionHandler = new Label();
    //obtain prepared statement
    mw.visitLabel(beginTryBlock);
    Label whileStart = new Label();
    mw.visitLabel(whileStart);
    mw.visitVarInsn(ALOAD, rsSelectIdx);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(ResultSet.class),
                       MethodsForResultSet.next.getName(),
                       Type.getMethodDescriptor(MethodsForResultSet.next));
    mw.visitJumpInsn(IFEQ, endTryBlock);
    //
    final int beanIdx = codeInfo._varindx++;
    mw.visitTypeInsn(NEW, Type.getInternalName(cl));
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(cl),
                       "<init>",
                       "()V");
    mw.visitVarInsn(ASTORE, beanIdx);
    for (int i = 0; i < columnNames.length; i++) {
      String columnName = columnNames[i];

      final MappedClass.MappedAttribute column = jdbcMap.getColumn(
        columnName);

      if (column == null || column.getSetter() == null)
        continue;

      codeInfo._nextInstruction = null;
      writeSetBeanPropertyFromResultSet(cl,
                                        mw,
                                        column,
                                        codeInfo,
                                        rsSelectIdx,
                                        beanIdx,
                                        i,
                                        false);
      if (codeInfo._nextInstruction != null) {
        mw.visitLabel(codeInfo._nextInstruction);
      }
    }
    mw.visitVarInsn(ALOAD, resultListIdx);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.util.List.class),
                       "add",
                       Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                                                new Type[]{Type.getType(Object.class)})
    );
    mw.visitInsn(POP);
    mw.visitJumpInsn(GOTO, whileStart);
    //mw.visitInsn(NOP);
    mw.visitLabel(endTryBlock);
    //finallyBlockStart is where the actual java code would start
    final Label finallyBlockStart = new Label();
    mw.visitJumpInsn(JSR, finallyBlockStart);
    //endTryBlockBeforeGoToReturn
    final Label endTryBlockBeforeGoToReturn = new Label();
    mw.visitLabel(endTryBlockBeforeGoToReturn);
    mw.visitJumpInsn(GOTO, methodEnd);
    //
    //handle SQLException
    //label catchSqlBlock will also be used as a start PC to add to the Exceptions table for the finally handler
    // as well as handler PC for any SQL exceptions occuring during try/catch(SQLException)
    mw.visitLabel(catchSqlBlock);
    final int sqle = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, sqle);
    mw.visitVarInsn(ALOAD, sqle);
    mw.visitInsn(ATHROW);
    //
    //handle throwable
    //label catchThrBlock will be used as a handler PC for any Throwable exceptions that might occur during try/catch(Throwable)
    mw.visitLabel(catchThrBlock);
    final int thre = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, thre);
    mw.visitTypeInsn(NEW, Type.getInternalName(RuntimeException.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, thre);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(RuntimeException.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(
                                                  Throwable.class)}
                       )
    );
    final int persExcIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, persExcIdx);
    mw.visitVarInsn(ALOAD, persExcIdx);
    mw.visitInsn(ATHROW);
    //
    //finally
    mw.visitLabel(finallyExceptionHandler);
    final int finallyExceptionIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, finallyExceptionIdx);
    mw.visitJumpInsn(JSR, finallyBlockStart);
    final int retIdx = codeInfo._varindx++;
    final Label finallyBlockRethrow = new Label();
    mw.visitLabel(finallyBlockRethrow);
    mw.visitVarInsn(ALOAD, finallyExceptionIdx);
    mw.visitInsn(ATHROW);
    mw.visitLabel(finallyBlockStart);
    mw.visitVarInsn(ASTORE, retIdx);
    mw.visitVarInsn(RET, retIdx);
    mw.visitMaxs(6, codeInfo._varindx);
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchSqlBlock,
                          Type.getInternalName(SQLException.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchThrBlock,
                          Type.getInternalName(Throwable.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlockBeforeGoToReturn,
                          finallyExceptionHandler,
                          null);
    mw.visitTryCatchBlock(catchSqlBlock,
                          finallyBlockRethrow,
                          finallyExceptionHandler,
                          null);
    //end of method
    mw.visitLabel(methodEnd);
    mw.visitVarInsn(ALOAD, resultListIdx);
    mw.visitInsn(ARETURN);
    mw.visitMaxs(7, codeInfo._varindx);
  }

  public static void writeSetBeanPropertyFromResultSet(final Class cl,
                                                       final CodeVisitor mw,
                                                       MappedClass.MappedAttribute column,
                                                       CodeInfo codeInfo,
                                                       final int rsIdx,
                                                       final int beanIdx,
                                                       final int colPos,
                                                       boolean useColumnName)
  {
    final int colSqlType = column.getSqlType();
    final Method getter = column.getGetter();
    final Class returnType = getter.getReturnType();
    //
    if (isClob(colSqlType, getter)) {
      writeSetBeanClobProperty(cl,
                               mw,
                               column,
                               rsIdx,
                               beanIdx,
                               colPos,
                               useColumnName);
    }
    else if (isBlob(colSqlType, getter)) {
      writeSetBeanBlobProperty(cl,
                               mw,
                               column,
                               rsIdx,
                               beanIdx,
                               colPos,
                               useColumnName);
    }
    else if (isSQLNative(returnType)) {
      writeSetBeanSQLNativeProperty(cl,
                                    mw,
                                    returnType,
                                    column,
                                    rsIdx,
                                    beanIdx,
                                    colPos,
                                    useColumnName);
    }
    else if (returnType == String.class &&
             colSqlType != Types.LONGVARCHAR &&
             colSqlType != Types.CLOB) {
      writeSetBeanStringProperty(cl,
                                 mw,
                                 column,
                                 rsIdx,
                                 beanIdx,
                                 colPos,
                                 useColumnName);
    }
    else if (returnType == String.class && colSqlType == Types.LONGVARCHAR) {
      writeSetBeanStringPropertyFromLongVarChar(cl,
                                                mw,
                                                column,
                                                codeInfo,
                                                rsIdx,
                                                beanIdx,
                                                colPos,
                                                useColumnName);
    }
    else if (returnType == byte[].class &&
             colSqlType != Types.LONGVARBINARY &&
             colSqlType != Types.BLOB) {
      writeSetBeanByteArrayProperty(cl,
                                    mw,
                                    column,
                                    rsIdx,
                                    beanIdx,
                                    colPos,
                                    useColumnName);
    }
    else if (returnType == byte[].class && colSqlType == Types.LONGVARBINARY) {
      writeSetBeanByteArrayPropertyFromLongBinary(cl,
                                                  mw,
                                                  column,
                                                  codeInfo,
                                                  rsIdx,
                                                  beanIdx,
                                                  colPos,
                                                  useColumnName);
    }
    else if (returnType == java.util.Date.class &&
             (colSqlType == Types.DATE ||
              colSqlType == Types.TIMESTAMP ||
              colSqlType == Types.TIME)) {
      writeSetBeanDatePropertyFromExtendedSQLDate(cl,
                                                  mw,
                                                  column,
                                                  codeInfo,
                                                  rsIdx,
                                                  beanIdx,
                                                  colPos,
                                                  colSqlType,
                                                  useColumnName);
    }
    else if (isPrimitive(returnType) && returnType != void.class) {
      writeSetBeanPrimitiveProperty(cl,
                                    mw,
                                    returnType,
                                    column,
                                    rsIdx,
                                    beanIdx,
                                    colPos,
                                    useColumnName);
    }
    else if (isPrimitiveWrapper(returnType)) {
      writeSetBeanPrimitiveWrapperProperty(cl,
                                           mw,
                                           returnType,
                                           column,
                                           codeInfo,
                                           rsIdx,
                                           beanIdx,
                                           colPos,
                                           useColumnName);
    }
    else {
      throw new RuntimeException(
        "Can not find code generation handler for column [" +
        column.getName() +
        "] of type [" +
        SQLUtils.sqlTypeToSqlTypeName(colSqlType) +
        "] with getter [" +
        getter +
        "].\n If this columnName is a CLOB or a BLOB please refer to the manual "
        +
        "for detail of handling these columns"
      );
    }
  }

  private static void writeSetBeanBlobProperty(final Class cl,
                                               final CodeVisitor mw,
                                               MappedClass.MappedAttribute column,
                                               final int rsIdx,
                                               final int beanIdx,
                                               final int colPos,
                                               boolean useColumnName)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.ResultSet.class),
                         MethodsForResultSet.getBlobByColumnName.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getBlobByColumnName));
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.ResultSet.class),
                         MethodsForResultSet.getBlobByColumnIndex.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getBlobByColumnIndex));
    }
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.Blob.class),
                       MethodsForBlob.getBinaryStream.getName(),
                       Type.getMethodDescriptor(MethodsForBlob.getBinaryStream));
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanClobProperty(final Class cl,
                                               final CodeVisitor mw,
                                               MappedClass.MappedAttribute column,
                                               final int rsIdx,
                                               final int beanIdx,
                                               final int colPos,
                                               boolean useColumnName)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.ResultSet.class),
                         MethodsForResultSet.getClobByColumnName.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getClobByColumnName));
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.ResultSet.class),
                         MethodsForResultSet.getClobByColumnIndex.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getClobByColumnIndex));
    }
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.Clob.class),
                       MethodsForClob.getCharacterStream.getName(),
                       Type.getMethodDescriptor(MethodsForClob.getCharacterStream));
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanUrlProperty(final Class cl,
                                              final CodeVisitor mw,
                                              MappedClass.MappedAttribute column,
                                              CodeInfo codeInfo,
                                              final int rsIdx,
                                              final int beanIdx,
                                              final int colPos,
                                              final int colSqlType,
                                              boolean useColumnName)
  {
    int urlIdx = codeInfo._varindx++;
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
    }
    final Method getterFromResultSet
      = MethodsForResultSet.findGetter(
      URL.class,
      useColumnName);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.ResultSet.class),
                       getterFromResultSet.getName(),
                       Type.getMethodDescriptor(getterFromResultSet));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ASTORE, urlIdx);
    Label nullHandler = new Label();
    mw.visitJumpInsn(IFNULL, nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitTypeInsn(NEW, Type.getInternalName(java.util.Date.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, urlIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(URL.class),
                       "getURL",
                       Type.getMethodDescriptor(Type.LONG_TYPE, new Type[]{}));
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
    mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
    mw.visitLabel(nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitInsn(ACONST_NULL);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanPrimitiveProperty(final Class cl,
                                                    final CodeVisitor mw,
                                                    final Class propertyType,
                                                    MappedClass.MappedAttribute column,
                                                    final int rsIdx,
                                                    final int beanIdx,
                                                    final int colPos,
                                                    boolean useColumnName)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
    }
    final Method getterFromResultSet
      = MethodsForResultSet.findGetter(
      propertyType,
      useColumnName);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.ResultSet.class),
                       getterFromResultSet.getName(),
                       Type.getMethodDescriptor(getterFromResultSet));
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanPrimitiveWrapperProperty(final Class cl,
                                                           final CodeVisitor mw,
                                                           final Class propertyType,
                                                           MappedClass.MappedAttribute column,
                                                           CodeInfo codeInfo,
                                                           final int rsIdx,
                                                           final int beanIdx,
                                                           final int colPos,
                                                           boolean useColumnName)
  {
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
    }
    final Method getterFromResultSet
      = MethodsForResultSet.findGetter(propertyType, useColumnName);

    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.ResultSet.class),
                       getterFromResultSet.getName(),
                       Type.getMethodDescriptor(getterFromResultSet));

    int var = codeInfo._varindx++;

    if (Boolean.class.equals(propertyType)
        || Byte.class.equals(propertyType)
        || Short.class.equals(propertyType)
        || Integer.class.equals(propertyType))
      mw.visitVarInsn(ISTORE, var);
    else if (Float.class.equals(propertyType))
      mw.visitVarInsn(FSTORE, var);
    else if (Double.class.equals(propertyType))
      mw.visitVarInsn(DSTORE, var);
    else if (Long.class.equals(propertyType))
      mw.visitVarInsn(LSTORE, var);
    else
      throw new RuntimeException(
        "Type '" +
        propertyType.getName() +
        "' is not supported in writeSetBeanPrimitiveWrapperProperty"
      );

    mw.visitVarInsn(ALOAD, rsIdx);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.ResultSet.class),
                       MethodsForResultSet.wasNull.getName(),
                       Type.getMethodDescriptor(MethodsForResultSet.wasNull));
    Label setNull = new Label();
    Label cont = new Label();

    Method setter = column.getSetter();

    mw.visitJumpInsn(IFEQ, setNull);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitInsn(ACONST_NULL);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
    mw.visitJumpInsn(GOTO, cont);
    mw.visitLabel(setNull);

    mw.visitVarInsn(ALOAD, beanIdx);

    mw.visitTypeInsn(NEW, Type.getInternalName(propertyType));
    mw.visitInsn(DUP);
    if (Boolean.class.equals(propertyType)
        || Byte.class.equals(propertyType)
        || Short.class.equals(propertyType)
        || Integer.class.equals(propertyType))
      mw.visitVarInsn(ILOAD, var);
    else if (Float.class.equals(propertyType))
      mw.visitVarInsn(FLOAD, var);
    else if (Double.class.equals(propertyType))
      mw.visitVarInsn(DLOAD, var);
    else if (Long.class.equals(propertyType))
      mw.visitVarInsn(LLOAD, var);

    Class constrParam
      = (Class) MethodsForJavaPrimitiveWrappers.WRAPPER_TO_PRIMITIVE
      .get(propertyType);

    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(propertyType),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(
                                                  constrParam)}
                       )
    );

    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
    mw.visitLabel(cont);
  }

  private static void writeSetBeanDatePropertyFromExtendedSQLDate(final Class cl,
                                                                  final CodeVisitor mw,
                                                                  MappedClass.MappedAttribute column,
                                                                  CodeInfo codeInfo,
                                                                  final int rsIdx,
                                                                  final int beanIdx,
                                                                  final int colPos,
                                                                  final int colSqlType,
                                                                  boolean useColumnName)
  {
    Class dateTimeClass;
    switch (colSqlType) {
    case Types.DATE: {
      dateTimeClass = java.sql.Date.class;

      break;
    }
    case Types.TIME: {
      dateTimeClass = java.sql.Time.class;

      break;
    }
    default: {
      dateTimeClass = java.sql.Timestamp.class;
    }
    }
    int dateIndex = codeInfo._varindx++;
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
    }
    final Method getterFromResultSet
      = MethodsForResultSet.findGetter(dateTimeClass, useColumnName);

    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.ResultSet.class),
                       getterFromResultSet.getName(),
                       Type.getMethodDescriptor(getterFromResultSet));

    mw.visitInsn(DUP);
    mw.visitVarInsn(ASTORE, dateIndex);
    Label nullHandler = new Label();
    mw.visitJumpInsn(IFNULL, nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitTypeInsn(NEW, Type.getInternalName(java.util.Date.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, dateIndex);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(dateTimeClass),
                       "getTime",
                       Type.getMethodDescriptor(Type.LONG_TYPE, new Type[]{}));
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(java.util.Date.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.LONG_TYPE})
    );
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
    mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
    mw.visitLabel(nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitInsn(ACONST_NULL);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanSQLNativeProperty(final Class cl,
                                                    final CodeVisitor mw,
                                                    final Class argumentType,
                                                    MappedClass.MappedAttribute column,
                                                    final int rsIdx,
                                                    final int beanIdx,
                                                    final int colPos,
                                                    boolean useColumnName)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
      final
      Method
        getterFromResultSet
        = MethodsForResultSet.findGetter(argumentType,
                                         true);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         getterFromResultSet.getName(),
                         Type.getMethodDescriptor(getterFromResultSet));
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
      final
      Method
        getterFromResultSet
        = MethodsForResultSet.findGetter(argumentType,
                                         false);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         getterFromResultSet.getName(),
                         Type.getMethodDescriptor(getterFromResultSet));
    }
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanStringPropertyFromLongVarChar(final Class cl,
                                                                final CodeVisitor mw,
                                                                MappedClass.MappedAttribute column,
                                                                CodeInfo codeInfo,
                                                                final int rsIdx,
                                                                final int beanIdx,
                                                                final int colPos,
                                                                boolean useColumnName)
  {
    int charStreamIdx = codeInfo._varindx++;
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getCharacterStreamByColumnName
                           .getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getCharacterStreamByColumnName)
      );
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getCharacterStreamByColumnIndex
                           .getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getCharacterStreamByColumnIndex)
      );
    }
    mw.visitInsn(DUP);
    mw.visitVarInsn(ASTORE, charStreamIdx);
    Label nullHandler = new Label();
    mw.visitJumpInsn(IFNULL, nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, charStreamIdx);
    mw.visitMethodInsn(INVOKESTATIC,
                       Type.getInternalName(IOUtils.class),
                       IOUtils.M_READ_FROM_READER_TO_STRING.getName(),
                       Type.getMethodDescriptor(IOUtils.M_READ_FROM_READER_TO_STRING));
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
    mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
    mw.visitLabel(nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitInsn(ACONST_NULL);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanByteArrayPropertyFromLongBinary(final Class cl,
                                                                  final CodeVisitor mw,
                                                                  MappedClass.MappedAttribute column,
                                                                  CodeInfo codeInfo,
                                                                  final int rsIdx,
                                                                  final int beanIdx,
                                                                  final int colPos,
                                                                  boolean useColumnName)
  {
    int binStreamIdx = codeInfo._varindx++;
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getBinaryStreamByColumnName
                           .getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getBinaryStreamByColumnName)
      );
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getBinaryStreamByColumnIndex
                           .getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getBinaryStreamByColumnIndex)
      );
    }
    mw.visitInsn(DUP);
    mw.visitVarInsn(ASTORE, binStreamIdx);
    Label nullHandler = new Label();
    mw.visitJumpInsn(IFNULL, nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, binStreamIdx);
    mw.visitMethodInsn(INVOKESTATIC,
                       Type.getInternalName(IOUtils.class),
                       IOUtils.M_READ_FROM_INPUTSTREAM_TO_BYTES.getName(),
                       Type.getMethodDescriptor(IOUtils.M_READ_FROM_INPUTSTREAM_TO_BYTES));
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
    mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
    mw.visitLabel(nullHandler);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitInsn(ACONST_NULL);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanStringProperty(final Class cl,
                                                 final CodeVisitor mw,
                                                 MappedClass.MappedAttribute column,
                                                 final int rsIdx,
                                                 final int beanIdx,
                                                 final int colPos,
                                                 boolean useColumnName)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getStringByColumnName.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getStringByColumnName));
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getStringByColumnIndex.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getStringByColumnIndex));
    }
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetBeanByteArrayProperty(final Class cl,
                                                    final CodeVisitor mw,
                                                    MappedClass.MappedAttribute column,
                                                    final int rsIdx,
                                                    final int beanIdx,
                                                    final int colPos,
                                                    boolean useColumnName)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, rsIdx);
    if (useColumnName) {
      mw.visitLdcInsn(column.getName());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getBytesByColumnName.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getBytesByColumnName));
    }
    else {
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(ResultSet.class),
                         MethodsForResultSet.getBytesByColumnIndex.getName(),
                         Type.getMethodDescriptor(MethodsForResultSet.getBytesByColumnIndex));
    }
    Method setter = column.getSetter();
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  public static void writeSetBeanPropertyFromArray(final Class cl,
                                                   final CodeVisitor mw,
                                                   final MappedClass.MappedAttribute column,
                                                   final int beanIdx,
                                                   final int colPos,
                                                   final int valueArrayIdx)
  {
    final Method getter = column.getGetter();
    final Method setter = column.getSetter();
    final Class returnType = getter.getReturnType();
    //
    if (isPrimitive(returnType) && returnType != void.class) {
      writeSetPrimitiveBeanPropertyFromArray(cl,
                                             mw,
                                             setter,
                                             returnType,
                                             beanIdx,
                                             valueArrayIdx,
                                             colPos);
    }
    else {
      writeSetObjectBeanPropertyFromArray(cl,
                                          mw,
                                          setter,
                                          returnType,
                                          beanIdx,
                                          valueArrayIdx,
                                          colPos);
    }
  }

  private static void writeSetObjectBeanPropertyFromArray(final Class cl,
                                                          final CodeVisitor mw,
                                                          final Method setter,
                                                          final Class returnType,
                                                          final int beanIdx,
                                                          final int valueArrayIdx,
                                                          final int colPos)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, valueArrayIdx);
    mw.visitIntInsn(SIPUSH, colPos);
    mw.visitInsn(AALOAD);
    mw.visitTypeInsn(CHECKCAST, Type.getInternalName(returnType));
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  private static void writeSetPrimitiveBeanPropertyFromArray(final Class cl,
                                                             final CodeVisitor mw,
                                                             final Method setter,
                                                             final Class returnType,
                                                             final int beanIdx,
                                                             final int valueArrayIdx,
                                                             final int colPos)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitVarInsn(ALOAD, valueArrayIdx);
    mw.visitIntInsn(SIPUSH, colPos);
    mw.visitInsn(AALOAD);
    if (returnType == boolean.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Boolean.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Boolean.class),
                         "booleanValue",
                         Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == char.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Character.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Character.class),
                         "charValue",
                         Type.getMethodDescriptor(Type.CHAR_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == byte.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Byte.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Byte.class),
                         "byteValue",
                         Type.getMethodDescriptor(Type.BYTE_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == short.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Short.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Short.class),
                         "shortValue",
                         Type.getMethodDescriptor(Type.SHORT_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == int.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Integer.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Integer.class),
                         "intValue",
                         Type.getMethodDescriptor(Type.INT_TYPE, new Type[]{}));
    }
    else if (returnType == long.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Long.class),
                         "longValue",
                         Type.getMethodDescriptor(Type.LONG_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == float.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Float.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Float.class),
                         "floatValue",
                         Type.getMethodDescriptor(Type.FLOAT_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == double.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Double.class),
                         "doubleValue",
                         Type.getMethodDescriptor(Type.DOUBLE_TYPE,
                                                  new Type[]{})
      );
    }
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(cl),
                       setter.getName(),
                       Type.getMethodDescriptor(setter));
  }

  public static void writeSetBestIdentifierValueForSelect(final CodeVisitor mw,
                                                          final MappedClass.MappedAttribute column,
                                                          final int psIdx,
                                                          final int colPos,
                                                          final int valueArrayIdx)
  {
    final int colSqlType = column.getSqlType();
    final Method getter = column.getGetter();
    if (getter == null) {
      throw new RuntimeException("Getter for [" +
                                 column.getName() +
                                 "] is null");
    }
    final Class returnType = getter.getReturnType();
    //
    if (isSQLNative(returnType)) {
      writeSQLNativeSetBestIdentifierValueForSelect(mw,
                                                    returnType,
                                                    psIdx,
                                                    valueArrayIdx,
                                                    colPos);
    }
    else if (returnType == String.class &&
             colSqlType != Types.LONGVARCHAR &&
             colSqlType != Types.CLOB) {
      writeSQLNativeSetBestIdentifierValueForSelect(mw,
                                                    returnType,
                                                    psIdx,
                                                    valueArrayIdx,
                                                    colPos);
    }
    else if (returnType == java.util.Date.class &&
             (colSqlType == Types.DATE ||
              colSqlType == Types.TIMESTAMP ||
              colSqlType == Types.TIME)) {
      writeDateSetToSQLDateExtendedSetBestIdentifierValueForSelect(mw,
                                                                   psIdx,
                                                                   valueArrayIdx,
                                                                   colPos,
                                                                   colSqlType);
    }
    else if (isPrimitive(returnType) && returnType != void.class) {
      writePrimitiveSetBestIdentifierValueForSelect(mw,
                                                    returnType,
                                                    psIdx,
                                                    valueArrayIdx,
                                                    colPos);
    }
    else {
      throw new RuntimeException(
        "Can not find code generation handler for columnName of type [" +
        SQLUtils.sqlTypeToSqlTypeName(colSqlType) +
        "] with getter [" +
        getter +
        "].\n If this columnName is a CLOB or a BLOB please refer to the manual "
        +
        "for detail of handling these columns"
      );
    }
  }

  private static void writePrimitiveSetBestIdentifierValueForSelect(final CodeVisitor mw,
                                                                    final Class returnType,
                                                                    final int psIdx,
                                                                    final int valueArrayIdx,
                                                                    final int colPos)
  {
    mw.visitVarInsn(ALOAD, psIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitVarInsn(ALOAD, valueArrayIdx);
    mw.visitIntInsn(SIPUSH, colPos);
    mw.visitInsn(AALOAD);
    if (returnType == boolean.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Boolean.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Boolean.class),
                         "booleanValue",
                         Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == char.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Character.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Character.class),
                         "charValue",
                         Type.getMethodDescriptor(Type.CHAR_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == byte.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Byte.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Byte.class),
                         "byteValue",
                         Type.getMethodDescriptor(Type.BYTE_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == short.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Short.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Short.class),
                         "shortValue",
                         Type.getMethodDescriptor(Type.SHORT_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == int.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Integer.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Integer.class),
                         "intValue",
                         Type.getMethodDescriptor(Type.INT_TYPE, new Type[]{}));
    }
    else if (returnType == long.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Long.class),
                         "longValue",
                         Type.getMethodDescriptor(Type.LONG_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == float.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Float.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Float.class),
                         "floatValue",
                         Type.getMethodDescriptor(Type.FLOAT_TYPE,
                                                  new Type[]{})
      );
    }
    else if (returnType == double.class) {
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(Double.class),
                         "doubleValue",
                         Type.getMethodDescriptor(Type.DOUBLE_TYPE,
                                                  new Type[]{})
      );
    }
    final Method targetSetter
      = MethodsForPreparedStatement.findSetter(
      returnType);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
  }

  private static void writeSQLNativeSetBestIdentifierValueForSelect(final CodeVisitor mw,
                                                                    final Class returnType,
                                                                    final int pstntIdx,
                                                                    final int valuesArrayIdx,
                                                                    final int colPos)
  {
    mw.visitVarInsn(ALOAD, pstntIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitVarInsn(ALOAD, valuesArrayIdx);
    mw.visitIntInsn(SIPUSH, colPos);
    mw.visitInsn(AALOAD);
    mw.visitTypeInsn(CHECKCAST, Type.getInternalName(returnType));
    final Method targetSetter
      = MethodsForPreparedStatement.findSetter(
      returnType);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
  }

  private static void writeDateSetToSQLDateExtendedSetBestIdentifierValueForSelect(
    final CodeVisitor mw,
    final int pstntIdx,
    final int valuesArrayIdx,
    final int colPos,
    final int colSqlType)
  {
    final Class dateExtendedClass;
    switch (colSqlType) {
    case Types.DATE: {
      dateExtendedClass = java.sql.Date.class;
      break;
    }
    case Types.TIME: {
      dateExtendedClass = java.sql.Time.class;
      break;
    }
    default: {
      dateExtendedClass = java.sql.Timestamp.class;
    }
    }
    mw.visitVarInsn(ALOAD, pstntIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitTypeInsn(NEW, Type.getInternalName(dateExtendedClass));
    mw.visitInsn(DUP);
    //
    mw.visitVarInsn(ALOAD, valuesArrayIdx);
    mw.visitIntInsn(SIPUSH, colPos);
    mw.visitInsn(AALOAD);
    mw.visitTypeInsn(CHECKCAST, Type.getInternalName(java.util.Date.class));
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(java.util.Date.class),
                       "getTime",
                       Type.getMethodDescriptor(Type.LONG_TYPE, new Type[]{}));
    //
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(dateExtendedClass),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(long.class)})
    );
    final Method targetSetter
      = MethodsForPreparedStatement.findSetter(
      dateExtendedClass);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
  }

  public static void writeThrowNoPKException(CodeVisitor mw,
                                             MappedClass jdbcMap)
  {
    mw.visitTypeInsn(NEW, Type.getInternalName(IllegalArgumentException.class));
    mw.visitInsn(DUP);
    mw.visitLdcInsn("Not supported for bean " +
                    jdbcMap.getMappedClass().getName() +
                    " because the class declares no primary key columns");
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(IllegalArgumentException.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(String.class)})
    );
    mw.visitInsn(ATHROW);
    mw.visitMaxs(3, 3);
  }

  private static void writeThrowNoColumnsException(CodeVisitor mw,
                                                   MappedClass jdbcMap)
  {
    mw.visitTypeInsn(NEW, Type.getInternalName(IllegalArgumentException.class));
    mw.visitInsn(DUP);
    mw.visitLdcInsn("Not supported on bean " +
                    jdbcMap.getMappedClass().getName() +
                    " because the class declares no regular columns");
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(IllegalArgumentException.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(String.class)})
    );
    mw.visitInsn(ATHROW);
    mw.visitMaxs(3, 3);
  }

  public static void writeInsert(final Class cl,
                                 final ClassWriter cw,
                                 final String className,
                                 final MappedClass jdbcMap,
                                 final boolean locatorsUpdateCopy,
                                 final boolean oracle,
                                 final boolean isBatch,
                                 final boolean useExecute)
    throws NoSuchMethodException
  {
    final CodeVisitor mw;
    if (isBatch) {
      mw = cw.visitMethod(ACC_PUBLIC,
                          "insert",
                          Type.getMethodDescriptor(Type.getType(int[].class),
                                                   new Type[]{Type.getType(
                                                     Connection.class),
                                                              Type.getType(
                                                                Object[].class)}
                          ),
                          INSERT_DELETE_UPDATE_BATCH_EXCEPTIONS,
                          null
      );
    }
    else {
      mw = cw.visitMethod(ACC_PUBLIC,
                          "insert",
                          Type.getMethodDescriptor(Type.INT_TYPE,
                                                   new Type[]{Type.getType(
                                                     Connection.class),
                                                              Type.getType(
                                                                Object.class)}
                          ),
                          INSERT_DELETE_UPDATE_EXCEPTIONS,
                          null
      );
    }
    final Label methodStart = new Label();
    final Label methodEnd = new Label();
    mw.visitLabel(methodStart);
    //this can be an array or an object
    int insertSubjIdx = 2;
    final CodeInfo codeInfo = new CodeInfo();
    codeInfo._varindx = 3;
    //create a constant that denotes conn being passed in
    final int connIdx = 1;
    //declare PreparedStatement and assigned it value of null
    final int psInsertOrUpdateIdx = codeInfo._varindx++;
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ASTORE, psInsertOrUpdateIdx);
    mw.visitLocalVariable("psInsert",
                          Type.getDescriptor(PreparedStatement.class),
                          methodStart,
                          methodEnd,
                          psInsertOrUpdateIdx);
    //
    int psSelectLobsIdx = -1;
    int rsSelectLobsIdx = -1;
    int psUpdateLobsIdx = -1;
    if (hasLobColumns(jdbcMap)) {
      psSelectLobsIdx = codeInfo._varindx++;
      rsSelectLobsIdx = codeInfo._varindx++;
      mw.visitInsn(ACONST_NULL);
      mw.visitVarInsn(ASTORE, psSelectLobsIdx);
      mw.visitLocalVariable("psSelectLobs",
                            Type.getDescriptor(PreparedStatement.class),
                            methodStart,
                            methodEnd,
                            psSelectLobsIdx);
      mw.visitInsn(ACONST_NULL);
      mw.visitVarInsn(ASTORE, rsSelectLobsIdx);
      mw.visitLocalVariable("rsSelectLobs",
                            Type.getDescriptor(ResultSet.class),
                            methodStart,
                            methodEnd,
                            rsSelectLobsIdx);
      if (locatorsUpdateCopy) {
        psUpdateLobsIdx = codeInfo._varindx++;
        mw.visitInsn(ACONST_NULL);
        mw.visitVarInsn(ASTORE, psUpdateLobsIdx);
        mw.visitLocalVariable("psUpdateLobs",
                              Type.getDescriptor(PreparedStatement.class),
                              methodStart,
                              methodEnd,
                              psUpdateLobsIdx);
      }
    }
    //
    final Label beginTryBlock = new Label();
    final Label endTryBlock = new Label();
    final Label catchSqlBlock = new Label();
    final Label catchThrBlock = new Label();
    final Label finallyExceptionHandler = new Label();
    //obtain prepared statement
    mw.visitLabel(beginTryBlock);
    mw.visitVarInsn(ALOAD, connIdx);
    mw.visitLdcInsn(SqlStatementFactory.makeInsert(jdbcMap));
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.Connection.class),
                       MethodsForConnection.prepareStatement.getName(),
                       Type.getMethodDescriptor(MethodsForConnection.prepareStatement));
    mw.visitVarInsn(ASTORE, psInsertOrUpdateIdx);
    //to operate in batch mode an index on the object array is required.
    final int insObjIdx = codeInfo._varindx++;
    int batchObjArrayIdx = 0;
    Label lblBatchUpdate = new Label();
    Label lblLoopCheckCondition = new Label();
    if (isBatch) {
      batchObjArrayIdx = codeInfo._varindx++;
      Label lblLoopStart = new Label();
      Label lblLoopEnd = new Label();
      mw.visitLocalVariable("objArrayIndex",
                            Type.INT_TYPE.getDescriptor(),
                            lblLoopStart,
                            lblLoopEnd,
                            batchObjArrayIdx);
      mw.visitLabel(lblLoopStart);
      mw.visitInsn(ICONST_0);
      mw.visitVarInsn(ISTORE, batchObjArrayIdx);
      mw.visitLabel(lblLoopCheckCondition);
      mw.visitVarInsn(ILOAD, batchObjArrayIdx);
      mw.visitVarInsn(ALOAD, insertSubjIdx);
      mw.visitInsn(ARRAYLENGTH);
      mw.visitJumpInsn(IF_ICMPGE, lblBatchUpdate);
      mw.visitIntInsn(ALOAD, insertSubjIdx);
      mw.visitIntInsn(ILOAD, batchObjArrayIdx);
      mw.visitInsn(AALOAD);
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(cl));
      mw.visitVarInsn(ASTORE, insObjIdx);
    }
    else {
      //make local varible for an object that is being persisted
      mw.visitVarInsn(ALOAD, insertSubjIdx);
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(cl));
      mw.visitVarInsn(ASTORE, insObjIdx);
    }
    int columnIndex = 0;
    MappedClass.MappedAttribute[] columns = jdbcMap.getColumns();
    for (int i = 0; i < columns.length; i++) {
      final MappedClass.MappedAttribute column = columns[i];
      if (column.getGetter() == null) continue;
      codeInfo._nextInstruction = null;
      writeSetPreparedStatementValue(cl,
                                     className,
                                     mw,
                                     null,
                                     column,
                                     false,
                                     psInsertOrUpdateIdx,
                                     insObjIdx,
                                     columnIndex,
                                     codeInfo,
                                     column.isNullable());
      columnIndex++;
      if (codeInfo._nextInstruction != null) {
        mw.visitLabel(codeInfo._nextInstruction);
      }
    }
    if (isBatch) {
      mw.visitVarInsn(ALOAD, psInsertOrUpdateIdx);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.PreparedStatement.class),
                         MethodsForPreparedStatement.addBatch.getName(),
                         Type.getMethodDescriptor(MethodsForPreparedStatement.addBatch));
      mw.visitIincInsn(batchObjArrayIdx, 1);
      mw.visitJumpInsn(GOTO, lblLoopCheckCondition);
    }
    mw.visitLabel(lblBatchUpdate);
    int resultIdx = codeInfo._varindx++;
    if (isBatch) {
      mw.visitLocalVariable("result",
                            Type.getType(int[].class).getDescriptor(),
                            methodStart,
                            methodEnd,
                            resultIdx);
    }
    else {
      mw.visitLocalVariable("result",
                            Type.INT_TYPE.getDescriptor(),
                            methodStart,
                            methodEnd,
                            resultIdx);
    }
    //
    if (isBatch) {
      mw.visitVarInsn(ALOAD, psInsertOrUpdateIdx);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.Statement.class),
                         MethodsForPreparedStatement.executeBatch
                           .getName(),
                         Type.getMethodDescriptor(MethodsForPreparedStatement.executeBatch)
      );
      mw.visitVarInsn(ASTORE, resultIdx);
    }
    else {
      if (useExecute) {
        mw.visitVarInsn(ALOAD, psInsertOrUpdateIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.execute.getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.execute)
        );
        mw.visitInsn(POP);
        mw.visitVarInsn(ALOAD, psInsertOrUpdateIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.getUpdateCount
                             .getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.getUpdateCount)
        );
        mw.visitVarInsn(ISTORE, resultIdx);
      }
      else {
        mw.visitVarInsn(ALOAD, psInsertOrUpdateIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.executeUpdate
                             .getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.executeUpdate)
        );
        mw.visitVarInsn(ISTORE, resultIdx);
      }
    }
    //
    if (false && hasLobColumns(jdbcMap)) {
      writeLobHandler(cl,
                      className,
                      mw,
                      jdbcMap,
                      insObjIdx,
                      connIdx,
                      codeInfo,
                      psSelectLobsIdx,
                      rsSelectLobsIdx,
                      psUpdateLobsIdx,
                      locatorsUpdateCopy,
                      oracle);
    }
    mw.visitLabel(endTryBlock);
    //finallyBlockStart is where the actual java code would start
    final Label finallyBlockStart = new Label();
    mw.visitJumpInsn(JSR, finallyBlockStart);
    //endTryBlockBeforeGoToReturn
    final Label endTryBlockBeforeGoToReturn = new Label();
    mw.visitLabel(endTryBlockBeforeGoToReturn);
    mw.visitJumpInsn(GOTO, methodEnd);
    //
    //handle SQLException
    //label catchSqlBlock will also be used as a start PC to add to the Exceptions table for the finally handler
    // as well as handler PC for any SQL exceptions occuring during try/catch(SQLException)
    mw.visitLabel(catchSqlBlock);
    final int sqle = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, sqle);
    mw.visitVarInsn(ALOAD, sqle);
    mw.visitInsn(ATHROW);
    //
    //handle throwable
    //label catchThrBlock will be used as a handler PC for any Throwable exceptions that might occur during try/catch(Throwable)
    mw.visitLabel(catchThrBlock);
    final int thre = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, thre);
    mw.visitTypeInsn(NEW, Type.getInternalName(RuntimeException.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, thre);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(RuntimeException.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(
                                                  Throwable.class)}
                       )
    );
    final int persExcIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, persExcIdx);
    mw.visitVarInsn(ALOAD, persExcIdx);
    mw.visitInsn(ATHROW);
    //
    //finally
    mw.visitLabel(finallyExceptionHandler);
    final int finallyExceptionIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, finallyExceptionIdx);
    mw.visitJumpInsn(JSR, finallyBlockStart);
    final int retIdx = codeInfo._varindx++;
    final Label finallyBlockRethrow = new Label();
    mw.visitLabel(finallyBlockRethrow);
    mw.visitVarInsn(ALOAD, finallyExceptionIdx);
    mw.visitInsn(ATHROW);
    mw.visitLabel(finallyBlockStart);
    mw.visitVarInsn(ASTORE, retIdx);
    final
    Method
      methodClose
      = SQLUtils.class.getDeclaredMethod("close",
                                         new Class[]{ResultSet.class,
                                                     Statement.class}
    );
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ALOAD, psInsertOrUpdateIdx);
    mw.visitMethodInsn(INVOKESTATIC,
                       Type.getInternalName(SQLUtils.class),
                       methodClose.getName(),
                       Type.getMethodDescriptor(methodClose));
    if (psSelectLobsIdx != -1) {
      mw.visitVarInsn(ALOAD, rsSelectLobsIdx);
      mw.visitVarInsn(ALOAD, psSelectLobsIdx);
      mw.visitMethodInsn(INVOKESTATIC,
                         Type.getInternalName(SQLUtils.class),
                         methodClose.getName(),
                         Type.getMethodDescriptor(methodClose));
    }
    if (psUpdateLobsIdx != -1) {
      mw.visitInsn(ACONST_NULL);
      mw.visitVarInsn(ALOAD, psUpdateLobsIdx);
      mw.visitMethodInsn(INVOKESTATIC,
                         Type.getInternalName(SQLUtils.class),
                         methodClose.getName(),
                         Type.getMethodDescriptor(methodClose));
    }
    mw.visitVarInsn(RET, retIdx);
    mw.visitMaxs(6, codeInfo._varindx);
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchSqlBlock,
                          Type.getInternalName(SQLException.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchThrBlock,
                          Type.getInternalName(Throwable.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlockBeforeGoToReturn,
                          finallyExceptionHandler,
                          null);
    mw.visitTryCatchBlock(catchSqlBlock,
                          finallyBlockRethrow,
                          finallyExceptionHandler,
                          null);
    //end of method
    mw.visitLabel(methodEnd);
    if (isBatch) {
      mw.visitVarInsn(ALOAD, resultIdx);
      mw.visitInsn(ARETURN);
    }
    else {
      mw.visitVarInsn(ILOAD, resultIdx);
      mw.visitInsn(IRETURN);
    }
  }

  static class VersionControlInfo
  {
    MappedClass.MappedAttribute column;
    int versionIdx;
    Class returnClass;
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

  public static void writeUpdate(final Class cl,
                                 final ClassWriter cw,
                                 final String className,
                                 final MappedClass jdbcMap,
                                 final boolean locatorsUpdateCopy,
                                 final boolean oracle,
                                 boolean isBatch,
                                 boolean useExecute)
    throws NoSuchMethodException
  {
    final CodeVisitor mw;
    if (isBatch) {
      mw = cw.visitMethod(ACC_PUBLIC,
                          "update",
                          Type.getMethodDescriptor(Type.getType(int[].class),
                                                   new Type[]{Type.getType(
                                                     Connection.class),
                                                              Type.getType(
                                                                Object[].class)}
                          ),
                          INSERT_DELETE_UPDATE_BATCH_EXCEPTIONS,
                          null
      );
    }
    else {
      mw = cw.visitMethod(ACC_PUBLIC,
                          "update",
                          Type.getMethodDescriptor(Type.INT_TYPE,
                                                   new Type[]{Type.getType(
                                                     Connection.class),
                                                              Type.getType(
                                                                Object.class)}
                          ),
                          INSERT_DELETE_UPDATE_EXCEPTIONS,
                          null
      );
    }
    if (jdbcMap.getIdentifyingColumns().length == 0) {
      writeThrowNoPKException(mw, jdbcMap);

      return;
    }

    if (jdbcMap.getRegularColumns().length == 0) {
      writeThrowNoColumnsException(mw, jdbcMap);

      return;
    }
    //
    final Label methodStart = new Label();
    final Label methodEnd = new Label();
    mw.visitLabel(methodStart);
    //
    final int updateSubjIdx = 2;
    //this can be an array or an object
    final CodeInfo codeInfo = new CodeInfo();
    codeInfo._varindx = 3;
    //create a constant that denotes conn being passed in
    final int connIdx = 1;
    //declare PreparedStatement and assigned it value of null
    final int psUpdateIdx = codeInfo._varindx++;
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ASTORE, psUpdateIdx);
    mw.visitLocalVariable("psUpdate",
                          Type.getDescriptor(PreparedStatement.class),
                          methodStart,
                          methodEnd,
                          psUpdateIdx);
    //
    MappedClass.MappedAttribute versionControlColumn
      = jdbcMap.getVersionControlColumn();
    VersionControlInfo versionControlInfo = null;
    if (versionControlColumn != null) {
      versionControlInfo = new VersionControlInfo(versionControlColumn);
    }
    //
    int psSelectLobsIdx = -1;
    int rsSelectLobsIdx = -1;
    int psUpdateLobsIdx = -1;
    if (false && hasLobColumns(jdbcMap)) {
      psSelectLobsIdx = codeInfo._varindx++;
      rsSelectLobsIdx = codeInfo._varindx++;
      mw.visitInsn(ACONST_NULL);
      mw.visitVarInsn(ASTORE, psSelectLobsIdx);
      mw.visitLocalVariable("psSelectLobs",
                            Type.getDescriptor(PreparedStatement.class),
                            methodStart,
                            methodEnd,
                            psSelectLobsIdx);
      mw.visitInsn(ACONST_NULL);
      mw.visitVarInsn(ASTORE, rsSelectLobsIdx);
      mw.visitLocalVariable("rsSelectLobs",
                            Type.getDescriptor(ResultSet.class),
                            methodStart,
                            methodEnd,
                            rsSelectLobsIdx);
      if (locatorsUpdateCopy) {
        psUpdateLobsIdx = codeInfo._varindx++;
        mw.visitInsn(ACONST_NULL);
        mw.visitVarInsn(ASTORE, psUpdateLobsIdx);
        mw.visitLocalVariable("psUpdateLobs",
                              Type.getDescriptor(PreparedStatement.class),
                              methodStart,
                              methodEnd,
                              psUpdateLobsIdx);
      }
    }
    //
    final Label beginTryBlock = new Label();
    final Label endTryBlock = new Label();
    final Label catchSqlBlock = new Label();
    final Label catchThrBlock = new Label();
    final Label finallyExceptionHandler = new Label();
    //obtain prepared statement
    mw.visitLabel(beginTryBlock);
    mw.visitVarInsn(ALOAD, connIdx);
    mw.visitLdcInsn(SqlStatementFactory.makeUpdate(jdbcMap));
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.Connection.class),
                       MethodsForConnection.prepareStatement.getName(),
                       Type.getMethodDescriptor(MethodsForConnection.prepareStatement));
    mw.visitVarInsn(ASTORE, psUpdateIdx);
    final int updateObjIdx = codeInfo._varindx++;
    int batchObjArrayIdx = 0;
    Label lblBatchUpdate = new Label();
    Label lblLoopCheckCondition = new Label();
    if (isBatch) {
      batchObjArrayIdx = codeInfo._varindx++;
      Label lblLoopStart = new Label();
      Label lblLoopEnd = new Label();
      mw.visitLocalVariable("objArrayIndex",
                            Type.INT_TYPE.getDescriptor(),
                            lblLoopStart,
                            lblLoopEnd,
                            batchObjArrayIdx);
      mw.visitLabel(lblLoopStart);
      mw.visitInsn(ICONST_0);
      mw.visitVarInsn(ISTORE, batchObjArrayIdx);
      mw.visitLabel(lblLoopCheckCondition);
      mw.visitVarInsn(ILOAD, batchObjArrayIdx);
      mw.visitVarInsn(ALOAD, updateSubjIdx);
      mw.visitInsn(ARRAYLENGTH);
      mw.visitJumpInsn(IF_ICMPGE, lblBatchUpdate);
      mw.visitIntInsn(ALOAD, updateSubjIdx);
      mw.visitIntInsn(ILOAD, batchObjArrayIdx);
      mw.visitInsn(AALOAD);
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(cl));
      mw.visitVarInsn(ASTORE, updateObjIdx);
    }
    else {
      //make local varible for an object that is being persisted
      mw.visitVarInsn(ALOAD, updateSubjIdx);
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(cl));
      mw.visitVarInsn(ASTORE, updateObjIdx);
    }

    //
    int columnIndex = 0;
    final MappedClass.MappedAttribute[] regularColumns
      = jdbcMap.getRegularColumns();
    for (int i = 0; i < regularColumns.length; i++) {
      final MappedClass.MappedAttribute column = regularColumns[i];
      if (column.getGetter() == null) continue;
      codeInfo._nextInstruction = null;
      writeSetPreparedStatementValue(cl,
                                     className,
                                     mw,
                                     versionControlInfo,
                                     column,
                                     column == versionControlColumn,
                                     psUpdateIdx,
                                     updateObjIdx,
                                     columnIndex,
                                     codeInfo,
                                     column.isNullable());
      columnIndex++;
      if (codeInfo._nextInstruction != null) {
        mw.visitLabel(codeInfo._nextInstruction);
      }
    }
    final int length = columnIndex;
    MappedClass.MappedAttribute[] identifyingColumns
      = jdbcMap.getIdentifyingColumns();
    if (identifyingColumns.length > 0) {
      for (int i = 0; i < identifyingColumns.length; i++) {
        final MappedClass.MappedAttribute column
          = identifyingColumns[i];
        codeInfo._nextInstruction = null;
        writeSetPreparedStatementValue(cl,
                                       className,
                                       mw,
                                       versionControlInfo,
                                       column,
                                       false,
                                       psUpdateIdx,
                                       updateObjIdx,
                                       i + length,
                                       codeInfo,
                                       false);
        if (codeInfo._nextInstruction != null) {
          mw.visitLabel(codeInfo._nextInstruction);
        }
      }
    }
    if (versionControlInfo != null) {
      writeSetPreparedStatementValue(cl,
                                     className,
                                     mw,
                                     versionControlInfo,
                                     versionControlColumn,
                                     false,
                                     psUpdateIdx,
                                     updateObjIdx,
                                     identifyingColumns.length + length,
                                     codeInfo,
                                     false);
    }
    //
    if (isBatch) {
      mw.visitVarInsn(ALOAD, psUpdateIdx);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.PreparedStatement.class),
                         MethodsForPreparedStatement.addBatch.getName(),
                         Type.getMethodDescriptor(MethodsForPreparedStatement.addBatch));
      mw.visitIincInsn(batchObjArrayIdx, 1);
      mw.visitJumpInsn(GOTO, lblLoopCheckCondition);
    }
    mw.visitLabel(lblBatchUpdate);
    int resultIdx = codeInfo._varindx++;
    if (isBatch) {
      mw.visitLocalVariable("result",
                            Type.getType(int[].class).getDescriptor(),
                            methodStart,
                            methodEnd,
                            resultIdx);
    }
    else {
      mw.visitLocalVariable("result",
                            Type.INT_TYPE.getDescriptor(),
                            methodStart,
                            methodEnd,
                            resultIdx);
    }
    if (isBatch) {
      mw.visitVarInsn(ALOAD, psUpdateIdx);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.Statement.class),
                         MethodsForPreparedStatement.executeBatch
                           .getName(),
                         Type.getMethodDescriptor(MethodsForPreparedStatement.executeBatch)
      );
      mw.visitVarInsn(ASTORE, resultIdx);
    }
    else {
      if (useExecute) {
        mw.visitVarInsn(ALOAD, psUpdateIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.execute.getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.execute)
        );
        mw.visitInsn(POP);
        mw.visitVarInsn(ALOAD, psUpdateIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.getUpdateCount
                             .getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.getUpdateCount)
        );
        mw.visitVarInsn(ISTORE, resultIdx);
      }
      else {
        mw.visitVarInsn(ALOAD, psUpdateIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.executeUpdate
                             .getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.executeUpdate)
        );
        mw.visitVarInsn(ISTORE, resultIdx);
      }
    }

    //fix
    if (false && versionControlInfo != null) {
      mw.visitVarInsn(ALOAD, updateSubjIdx);
      if (versionControlInfo.returnClass.equals(int.class)) {
        mw.visitVarInsn(ILOAD, versionControlInfo.versionIdx);
      }
      else if (versionControlInfo.returnClass.equals(long.class)) {
        mw.visitVarInsn(LLOAD, versionControlInfo.versionIdx);
      }
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(cl),
                         versionControlInfo.setter.getName(),
                         Type.getMethodDescriptor(versionControlInfo.setter));
    }
    if (false && hasLobColumns(jdbcMap)) {
      writeLobHandler(cl,
                      className,
                      mw,
                      jdbcMap,
                      updateSubjIdx,
                      connIdx,
                      codeInfo,
                      psSelectLobsIdx,
                      rsSelectLobsIdx,
                      psUpdateLobsIdx,
                      locatorsUpdateCopy,
                      oracle);
    }
    mw.visitLabel(endTryBlock);
    //finallyBlockStart is where the actual java code would start
    final Label finallyBlockStart = new Label();
    mw.visitJumpInsn(JSR, finallyBlockStart);
    //endTryBlockBeforeGoToReturn
    final Label endTryBlockBeforeGoToReturn = new Label();
    mw.visitLabel(endTryBlockBeforeGoToReturn);
    mw.visitJumpInsn(GOTO, methodEnd);
    //
    //handle SQLException
    //label catchSqlBlock will also be used as a start PC to add to the Exceptions table for the finally handler
    // as well as handler PC for any SQL exceptions occuring during try/catch(SQLException)
    mw.visitLabel(catchSqlBlock);
    final int sqle = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, sqle);
    mw.visitVarInsn(ALOAD, sqle);
    mw.visitInsn(ATHROW);
    //
    //handle throwable
    //label catchThrBlock will be used as a handler PC for any Throwable exceptions that might occur during try/catch(Throwable)
    mw.visitLabel(catchThrBlock);
    final int thre = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, thre);
    mw.visitTypeInsn(NEW, Type.getInternalName(RuntimeException.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, thre);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(RuntimeException.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(
                                                  Throwable.class)}
                       )
    );
    final int persExcIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, persExcIdx);
    mw.visitVarInsn(ALOAD, persExcIdx);
    mw.visitInsn(ATHROW);
    //
    //finally
    mw.visitLabel(finallyExceptionHandler);
    final int finallyExceptionIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, finallyExceptionIdx);
    mw.visitJumpInsn(JSR, finallyBlockStart);
    final int retIdx = codeInfo._varindx++;
    final Label finallyBlockRethrow = new Label();
    mw.visitLabel(finallyBlockRethrow);
    mw.visitVarInsn(ALOAD, finallyExceptionIdx);
    mw.visitInsn(ATHROW);
    mw.visitLabel(finallyBlockStart);
    mw.visitVarInsn(ASTORE, retIdx);
    final
    Method
      methodClose
      = SQLUtils.class.getDeclaredMethod("close",
                                         new Class[]{ResultSet.class,
                                                     Statement.class}
    );
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ALOAD, psUpdateIdx);
    mw.visitMethodInsn(INVOKESTATIC,
                       Type.getInternalName(SQLUtils.class),
                       methodClose.getName(),
                       Type.getMethodDescriptor(methodClose));
    if (psSelectLobsIdx != -1) {
      mw.visitVarInsn(ALOAD, rsSelectLobsIdx);
      mw.visitVarInsn(ALOAD, psSelectLobsIdx);
      mw.visitMethodInsn(INVOKESTATIC,
                         Type.getInternalName(SQLUtils.class),
                         methodClose.getName(),
                         Type.getMethodDescriptor(methodClose));
    }
    if (psUpdateLobsIdx != -1) {
      mw.visitInsn(ACONST_NULL);
      mw.visitVarInsn(ALOAD, psUpdateLobsIdx);
      mw.visitMethodInsn(INVOKESTATIC,
                         Type.getInternalName(SQLUtils.class),
                         methodClose.getName(),
                         Type.getMethodDescriptor(methodClose));
    }
    mw.visitVarInsn(RET, retIdx);
    mw.visitMaxs(6, codeInfo._varindx);
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchSqlBlock,
                          Type.getInternalName(SQLException.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchThrBlock,
                          Type.getInternalName(Throwable.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlockBeforeGoToReturn,
                          finallyExceptionHandler,
                          null);
    mw.visitTryCatchBlock(catchSqlBlock,
                          finallyBlockRethrow,
                          finallyExceptionHandler,
                          null);
    //end of method
    mw.visitLabel(methodEnd);
    if (isBatch) {
      mw.visitVarInsn(ALOAD, resultIdx);
      mw.visitInsn(ARETURN);
    }
    else {
      mw.visitVarInsn(ILOAD, resultIdx);
      mw.visitInsn(IRETURN);
    }
  }

  private static void writeLobHandler(final Class cl,
                                      final String className,
                                      final CodeVisitor mw,
                                      final MappedClass jdbcMap,
                                      final int inso,
                                      final int conn,
                                      final CodeInfo codeInfo,
                                      final int pstntSelectLobsIdx,
                                      final int rsSelectLobsIdx,
                                      final int pstntUpdateLobsIdx,
                                      final boolean locatorsUpdateCopy,
                                      final boolean oracle)
    throws NoSuchMethodException
  {
    final MappedClass.MappedAttribute[] lobColumns = getLobColumns(
      jdbcMap);
    final int[] lobIdxs = new int[lobColumns.length];
    final boolean[] clobs = new boolean[lobColumns.length];
    mw.visitVarInsn(ALOAD, conn);
    mw.visitLdcInsn(SqlStatementFactory.makeSelect(lobColumns,
                                                   (oracle ?
                                                     " FOR UPDATE" :
                                                     null),
                                                   jdbcMap
    ));
    final Method target = Connection.class.getMethod("prepareStatement",
                                                     new Class[]{String.class});
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(Connection.class),
                       target.getName(),
                       Type.getMethodDescriptor(target));
    mw.visitVarInsn(ASTORE, pstntSelectLobsIdx);
    MappedClass.MappedAttribute[] identifyingColumns
      = jdbcMap.getIdentifyingColumns();
    for (int i = 0; i < identifyingColumns.length; i++) {
      final MappedClass.MappedAttribute column = identifyingColumns[i];
      writeSetPreparedStatementValue(cl,
                                     className,
                                     mw,
                                     null,
                                     column,
                                     false,
                                     pstntSelectLobsIdx,
                                     inso,
                                     i,
                                     codeInfo,
                                     false);
    }
    mw.visitVarInsn(ALOAD, pstntSelectLobsIdx);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(PreparedStatement.class),
                       MethodsForPreparedStatement.executeQuery.getName(),
                       Type.getMethodDescriptor(MethodsForPreparedStatement.executeQuery));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ASTORE, rsSelectLobsIdx);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(ResultSet.class),
                       MethodsForResultSet.next.getName(),
                       Type.getMethodDescriptor(MethodsForResultSet.next));
    mw.visitInsn(POP);
    for (int i = 0; i < lobColumns.length; i++) {
      final MappedClass.MappedAttribute lobColumn = lobColumns[i];
      final int colSqlType = lobColumn.getSqlType();
      final Method setter = lobColumn.getGetter();
      if (setter == null) {
        throw new RuntimeException("No Setter found for column [" +
                                   lobColumn +
                                   "]");
      }
      final int lobIdx = codeInfo._varindx++;
      lobIdxs[i] = lobIdx;
      if (isClob(colSqlType, setter)) {
        clobs[i] = true;
        mw.visitVarInsn(ALOAD, rsSelectLobsIdx);
        mw.visitIntInsn(SIPUSH, i + 1);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(ResultSet.class),
                           MethodsForResultSet.getClobByColumnIndex.getName(),
                           Type.getMethodDescriptor(
                             MethodsForResultSet.getClobByColumnIndex)
        );
        mw.visitVarInsn(ASTORE, lobIdx);
        mw.visitVarInsn(ALOAD, inso);
        mw.visitVarInsn(ALOAD, lobIdx);
        if (oracle) {
          mw.visitTypeInsn(CHECKCAST, I_C_N_ORA_SQL_CLOB);
          mw.visitMethodInsn(INVOKEVIRTUAL,
                             I_C_N_ORA_SQL_CLOB,
                             "getCharacterOutputStream",
                             Type.getMethodDescriptor(Type.getType(Writer.class),
                                                      new Type[]{})
          );
        }
        else {
          mw.visitIntInsn(BIPUSH, 1);
          mw.visitInsn(I2L);
          mw.visitMethodInsn(INVOKEINTERFACE,
                             Type.getInternalName(Clob.class),
                             MethodsForClob.setCharacterStream.getName(),
                             Type.getMethodDescriptor(
                               MethodsForClob.setCharacterStream)
          );
        }
        mw.visitInsn(DUP);
        final int writerIdx = codeInfo._varindx++;
        mw.visitVarInsn(ASTORE, writerIdx);
        mw.visitMethodInsn(INVOKEVIRTUAL,
                           Type.getInternalName(cl),
                           setter.getName(),
                           Type.getMethodDescriptor(setter));
        mw.visitVarInsn(ALOAD, writerIdx);
        mw.visitInsn(DUP);
        mw.visitMethodInsn(INVOKEVIRTUAL,
                           Type.getInternalName(Writer.class),
                           "flush",
                           Type.getMethodDescriptor(Type.VOID_TYPE,
                                                    new Type[]{})
        );
        mw.visitMethodInsn(INVOKEVIRTUAL,
                           Type.getInternalName(Writer.class),
                           "close",
                           Type.getMethodDescriptor(Type.VOID_TYPE,
                                                    new Type[]{})
        );
      }
      else {
        clobs[i] = false;
        mw.visitVarInsn(ALOAD, rsSelectLobsIdx);
        mw.visitIntInsn(SIPUSH, i + 1);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(ResultSet.class),
                           MethodsForResultSet.getBlobByColumnIndex.getName(),
                           Type.getMethodDescriptor(
                             MethodsForResultSet.getBlobByColumnIndex)
        );
        mw.visitVarInsn(ASTORE, lobIdx);
        mw.visitVarInsn(ALOAD, inso);
        mw.visitVarInsn(ALOAD, lobIdx);
        if (oracle) {
          mw.visitTypeInsn(CHECKCAST, I_C_N_ORA_SQL_BLOB);
          mw.visitMethodInsn(INVOKEVIRTUAL,
                             I_C_N_ORA_SQL_BLOB,
                             "getBinaryOutputStream",
                             Type.getMethodDescriptor(Type.getType(OutputStream.class),
                                                      new Type[]{})
          );
        }
        else {
          mw.visitIntInsn(BIPUSH, 1);
          mw.visitInsn(I2L);
          mw.visitMethodInsn(INVOKEINTERFACE,
                             Type.getInternalName(Blob.class),
                             MethodsForBlob.setBinaryStream.getName(),
                             Type.getMethodDescriptor(
                               MethodsForBlob.setBinaryStream)
          );
        }
        mw.visitInsn(DUP);
        final int outputStreamIdx = codeInfo._varindx++;
        mw.visitVarInsn(ASTORE, outputStreamIdx);
        mw.visitMethodInsn(INVOKEVIRTUAL,
                           Type.getInternalName(cl),
                           setter.getName(),
                           Type.getMethodDescriptor(setter));
        mw.visitVarInsn(ALOAD, outputStreamIdx);
        mw.visitInsn(DUP);
        mw.visitMethodInsn(INVOKEVIRTUAL,
                           Type.getInternalName(OutputStream.class),
                           "flush",
                           Type.getMethodDescriptor(Type.VOID_TYPE,
                                                    new Type[]{})
        );
        mw.visitMethodInsn(INVOKEVIRTUAL,
                           Type.getInternalName(OutputStream.class),
                           "close",
                           Type.getMethodDescriptor(Type.VOID_TYPE,
                                                    new Type[]{})
        );
      }
    }
    if (locatorsUpdateCopy) {
      mw.visitVarInsn(ALOAD, conn);
      mw.visitLdcInsn(SqlStatementFactory.makeUpdate(lobColumns,
                                                     jdbcMap));
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(Connection.class),
                         MethodsForConnection.prepareStatement.getName(),
                         Type.getMethodDescriptor(MethodsForConnection.prepareStatement));
      mw.visitVarInsn(ASTORE, pstntUpdateLobsIdx);
      for (int i = 0; i < lobIdxs.length; i++) {
        final int lobIdx = lobIdxs[i];
        final boolean clob = clobs[i];
        mw.visitVarInsn(ALOAD, pstntUpdateLobsIdx);
        mw.visitIntInsn(SIPUSH, i + 1);
        mw.visitVarInsn(ALOAD, lobIdx);
        if (clob) {
          mw.visitMethodInsn(INVOKEINTERFACE,
                             Type.getInternalName(PreparedStatement.class),
                             MethodsForPreparedStatement.setClob.getName(),
                             Type.getMethodDescriptor(
                               MethodsForPreparedStatement.setClob)
          );
        }
        else {
          mw.visitMethodInsn(INVOKEINTERFACE,
                             Type.getInternalName(PreparedStatement.class),
                             MethodsForPreparedStatement.setBlob.getName(),
                             Type.getMethodDescriptor(
                               MethodsForPreparedStatement.setBlob)
          );
        }
      }
      for (int i = 0; i < identifyingColumns.length; i++) {
        final MappedClass.MappedAttribute column
          = identifyingColumns[i];
        writeSetPreparedStatementValue(cl,
                                       className,
                                       mw,
                                       null,
                                       column,
                                       false,
                                       pstntUpdateLobsIdx,
                                       inso,
                                       i + lobColumns.length,
                                       codeInfo,
                                       false);
      }
      mw.visitVarInsn(ALOAD, pstntUpdateLobsIdx);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         MethodsForPreparedStatement.executeUpdate
                           .getName(),
                         Type.getMethodDescriptor(MethodsForPreparedStatement.executeUpdate)
      );
      mw.visitInsn(POP);
    }
  }

  private static boolean isClob(final int colSqlType, final Method getter)
  {
    //to support writing clobs via blob handler we test for blob/clob writer and type clob/longvarchar
    return (matchClobWriter(getter) || matchBlobWriter(getter)) &&
           (colSqlType == Types.CLOB || colSqlType == Types.LONGVARCHAR);
  }

  private static boolean isBlob(final int colSqlType, final Method getter)
  {
    //we will only support blob when there is a matching writer for it and the columnt type is blob;
    return (matchBlobWriter(getter)) &&
           (colSqlType == Types.BLOB || colSqlType == Types.LONGVARBINARY);
  }

  private static MappedClass.MappedAttribute[] getLobColumns(final MappedClass jdbcMap)
  {
    final ArrayList list = new ArrayList();
    MappedClass.MappedAttribute[] columns = jdbcMap.getColumns();

    for (int i = 0; i < columns.length; i++) {
      final MappedClass.MappedAttribute column = columns[i];
      final int colSqlType = column.getSqlType();
      final Method getter = column.getGetter();
      if (getter == null)
        continue;

      if (isClob(colSqlType, getter) || isBlob(colSqlType, getter)) {
        list.add(column);
      }
    }

    final MappedClass.MappedAttribute[] result
      = new MappedClass.MappedAttribute[list.size()];
    list.toArray(result);

    return result;
  }

  private static boolean hasLobColumns(final MappedClass jdbcMap)
  {
    MappedClass.MappedAttribute[] columns = jdbcMap.getColumns();

    for (int i = 0; i < columns.length; i++) {
      final MappedClass.MappedAttribute column = columns[i];
      final int colSqlType = column.getSqlType();
      final Method getter = column.getGetter();
      if (getter == null)
        continue;

      if (isClob(colSqlType, getter) || isBlob(colSqlType, getter)) {

        return true;
      }
    }

    return false;
  }

  public static void writeDelete(final Class cl,
                                 final ClassWriter cw,
                                 final String className,
                                 final MappedClass jdbcMap,
                                 final boolean isBatch,
                                 final boolean useExecute)
    throws NoSuchMethodException
  {
    final CodeVisitor mw;

    if (isBatch) {
      mw = cw.visitMethod(ACC_PUBLIC,
                          "delete",
                          Type.getMethodDescriptor(Type.getType(int[].class),
                                                   new Type[]{Type.getType(
                                                     Connection.class),
                                                              Type.getType(
                                                                Object[].class)}
                          ),
                          INSERT_DELETE_UPDATE_BATCH_EXCEPTIONS,
                          null
      );
    }
    else {
      mw = cw.visitMethod(ACC_PUBLIC,
                          "delete",
                          Type.getMethodDescriptor(Type.INT_TYPE,
                                                   new Type[]{Type.getType(
                                                     Connection.class),
                                                              Type.getType(
                                                                Object.class)}
                          ),
                          INSERT_DELETE_UPDATE_EXCEPTIONS,
                          null
      );
    }

    if (jdbcMap.getIdentifyingColumns().length == 0) {
      writeThrowNoPKException(mw, jdbcMap);

      return;
    }

    final Label methodStart = new Label();
    final Label methodEnd = new Label();
    mw.visitLabel(methodStart);
    //
    final CodeInfo codeInfo = new CodeInfo();
    int deleteSubjIdx = 2;
    codeInfo._varindx = 3;
    //make local varible for an object that is being persisted
    //make a constant for conn beeing passed in here
    final int conn = 1;
    //declare PreparedStatement and assigned it value of null
    final int psDeleteIdx = codeInfo._varindx++;
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ASTORE, psDeleteIdx);
    //
    final Label beginTryBlock = new Label();
    final Label endTryBlock = new Label();
    final Label catchSqlBlock = new Label();
    final Label catchThrBlock = new Label();
    final Label finallyExceptionHandler = new Label();
    //obtain prepared statement
    mw.visitLabel(beginTryBlock);
    mw.visitVarInsn(ALOAD, conn);
    mw.visitLdcInsn(SqlStatementFactory.makeDelete(jdbcMap));
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.Connection.class),
                       "prepareStatement",
                       Type.getMethodDescriptor(Type.getType(PreparedStatement.class),
                                                new Type[]{Type.getType(String.class)})
    );
    mw.visitVarInsn(ASTORE, psDeleteIdx);
    //
    //to operate in batch mode an index on the object array is required.
    final int insObjIdx = codeInfo._varindx++;
    int batchObjArrayIdx = 0;
    Label lblBatchUpdate = new Label();
    Label lblLoopCheckCondition = new Label();
    if (isBatch) {
      batchObjArrayIdx = codeInfo._varindx++;
      Label lblLoopStart = new Label();
      Label lblLoopEnd = new Label();
      mw.visitLocalVariable("objArrayIndex",
                            Type.INT_TYPE.getDescriptor(),
                            lblLoopStart,
                            lblLoopEnd,
                            batchObjArrayIdx);
      mw.visitLabel(lblLoopStart);
      mw.visitInsn(ICONST_0);
      mw.visitVarInsn(ISTORE, batchObjArrayIdx);
      mw.visitLabel(lblLoopCheckCondition);
      mw.visitVarInsn(ILOAD, batchObjArrayIdx);
      mw.visitVarInsn(ALOAD, deleteSubjIdx);
      mw.visitInsn(ARRAYLENGTH);
      mw.visitJumpInsn(IF_ICMPGE, lblBatchUpdate);
      mw.visitIntInsn(ALOAD, deleteSubjIdx);
      mw.visitIntInsn(ILOAD, batchObjArrayIdx);
      mw.visitInsn(AALOAD);
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(cl));
      mw.visitVarInsn(ASTORE, insObjIdx);
    }
    else {
      //make local varible for an object that is being persisted
      mw.visitVarInsn(ALOAD, deleteSubjIdx);
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(cl));
      mw.visitVarInsn(ASTORE, insObjIdx);
    }
    MappedClass.MappedAttribute[] identifyingColumns
      = jdbcMap.getIdentifyingColumns();
    for (int i = 0; i < identifyingColumns.length; i++) {
      final MappedClass.MappedAttribute column = identifyingColumns[i];
      writeSetPreparedStatementValue(cl,
                                     className,
                                     mw,
                                     null,
                                     column,
                                     false,
                                     psDeleteIdx,
                                     insObjIdx,
                                     i,
                                     codeInfo,
                                     false);
    }
    MappedClass.MappedAttribute versionControlColumn
      = jdbcMap.getVersionControlColumn();
    if (versionControlColumn != null) {
      writeSetPreparedStatementValue(cl,
                                     className,
                                     mw,
                                     null,
                                     versionControlColumn,
                                     false,
                                     psDeleteIdx,
                                     insObjIdx,
                                     identifyingColumns.length,
                                     codeInfo,
                                     false);
    }
    if (isBatch) {
      mw.visitVarInsn(ALOAD, psDeleteIdx);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.PreparedStatement.class),
                         MethodsForPreparedStatement.addBatch.getName(),
                         Type.getMethodDescriptor(MethodsForPreparedStatement.addBatch));
      mw.visitIincInsn(batchObjArrayIdx, 1);
      mw.visitJumpInsn(GOTO, lblLoopCheckCondition);
    }
    mw.visitLabel(lblBatchUpdate);
    int resultIdx = codeInfo._varindx++;
    if (isBatch) {
      mw.visitLocalVariable("result",
                            Type.getType(int[].class).getDescriptor(),
                            methodStart,
                            methodEnd,
                            resultIdx);
    }
    else {
      mw.visitLocalVariable("result",
                            Type.INT_TYPE.getDescriptor(),
                            methodStart,
                            methodEnd,
                            resultIdx);
    }
    //
    if (isBatch) {
      mw.visitVarInsn(ALOAD, psDeleteIdx);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(java.sql.Statement.class),
                         MethodsForPreparedStatement.executeBatch
                           .getName(),
                         Type.getMethodDescriptor(MethodsForPreparedStatement.executeBatch)
      );
      mw.visitVarInsn(ASTORE, resultIdx);
    }
    else {
      if (useExecute) {
        mw.visitVarInsn(ALOAD, psDeleteIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.execute.getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.execute)
        );
        mw.visitInsn(POP);
        mw.visitVarInsn(ALOAD, psDeleteIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.getUpdateCount
                             .getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.getUpdateCount)
        );
        mw.visitVarInsn(ISTORE, resultIdx);
      }
      else {
        mw.visitVarInsn(ALOAD, psDeleteIdx);
        mw.visitMethodInsn(INVOKEINTERFACE,
                           Type.getInternalName(java.sql.PreparedStatement.class),
                           MethodsForPreparedStatement.executeUpdate
                             .getName(),
                           Type.getMethodDescriptor(
                             MethodsForPreparedStatement.executeUpdate)
        );
        mw.visitVarInsn(ISTORE, resultIdx);
      }
    }
    //
    mw.visitLabel(endTryBlock);
    //finallyBlockStart is where the actual java code would start
    final Label finallyBlockStart = new Label();
    mw.visitJumpInsn(JSR, finallyBlockStart);
    //endTryBlockBeforeGoToReturn
    final Label endTryBlockBeforeGoToReturn = new Label();
    mw.visitLabel(endTryBlockBeforeGoToReturn);
    mw.visitJumpInsn(GOTO, methodEnd);
    //
    //handle SQLException
    //label catchSqlBlock will also be used as a start PC to add to the Exceptions table for the finally handler
    // as well as handler PC for any SQL exceptions occuring during try/catch(SQLException)
    mw.visitLabel(catchSqlBlock);
    final int sqle = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, sqle);
    mw.visitVarInsn(ALOAD, sqle);
    mw.visitInsn(ATHROW);
    //
    //handle throwable
    //label catchThrBlock will be used as a handler PC for any Throwable exceptions that might occur during try/catch(Throwable)
    mw.visitLabel(catchThrBlock);
    final int thre = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, thre);
    mw.visitTypeInsn(NEW, Type.getInternalName(RuntimeException.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, thre);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(RuntimeException.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(
                                                  Throwable.class)}
                       )
    );
    final int persExcIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, persExcIdx);
    mw.visitVarInsn(ALOAD, persExcIdx);
    mw.visitInsn(ATHROW);
    //
    //finally
    mw.visitLabel(finallyExceptionHandler);
    final int finallyExceptionIdx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, finallyExceptionIdx);
    mw.visitJumpInsn(JSR, finallyBlockStart);
    final int retIdx = codeInfo._varindx++;
    final Label finallyBlockRethrow = new Label();
    mw.visitLabel(finallyBlockRethrow);
    mw.visitVarInsn(ALOAD, finallyExceptionIdx);
    mw.visitInsn(ATHROW);
    mw.visitLabel(finallyBlockStart);
    mw.visitVarInsn(ASTORE, retIdx);
    final
    Method
      methodClose
      = SQLUtils.class.getDeclaredMethod("close",
                                         new Class[]{ResultSet.class,
                                                     Statement.class}
    );
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ALOAD, psDeleteIdx);
    mw.visitMethodInsn(INVOKESTATIC,
                       Type.getInternalName(SQLUtils.class),
                       methodClose.getName(),
                       Type.getMethodDescriptor(methodClose));
    mw.visitVarInsn(RET, retIdx);
    mw.visitMaxs(6, codeInfo._varindx);
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchSqlBlock,
                          Type.getInternalName(SQLException.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlock,
                          catchThrBlock,
                          Type.getInternalName(Throwable.class));
    mw.visitTryCatchBlock(beginTryBlock,
                          endTryBlockBeforeGoToReturn,
                          finallyExceptionHandler,
                          null);
    mw.visitTryCatchBlock(catchSqlBlock,
                          finallyBlockRethrow,
                          finallyExceptionHandler,
                          null);
    //
    mw.visitLabel(methodEnd);
    if (isBatch) {
      mw.visitVarInsn(ALOAD, resultIdx);
      mw.visitInsn(ARETURN);
    }
    else {
      mw.visitVarInsn(ILOAD, resultIdx);
      mw.visitInsn(IRETURN);
    }
  }

  private static void writeSetPreparedStatementValue(final Class cl,
                                                     final String className,
                                                     final CodeVisitor mw,
                                                     VersionControlInfo versionControlInfo,
                                                     final MappedClass.MappedAttribute column,
                                                     boolean incValue,
                                                     final int psIdx,
                                                     final int beanIdx,
                                                     final int colPos,
                                                     final CodeInfo codeInfo,
                                                     final boolean nullSensetive)
    throws NoSuchMethodException
  {
    final int colSqlType = column.getSqlType();
    final Method getter = column.getGetter();
    final Class returnType = getter.getReturnType();
    //
    if (isClob(colSqlType, getter)) {
      writeDummyClobValue(className, mw, psIdx, colPos);
    }
    else if (isBlob(colSqlType, getter)) {
      writeDummyBlobValue(className, mw, psIdx, colPos);
    }
    else if (isSQLNative(returnType)) {
      writeSQLNativeSetValue(cl,
                             mw,
                             getter,
                             psIdx,
                             beanIdx,
                             colPos,
                             codeInfo,
                             colSqlType,
                             nullSensetive);
    }
    else if (returnType == String.class &&
             colSqlType != Types.LONGVARCHAR &&
             colSqlType != Types.CLOB) {
      writeSQLNativeSetValue(cl,
                             mw,
                             getter,
                             psIdx,
                             beanIdx,
                             colPos,
                             codeInfo,
                             colSqlType,
                             nullSensetive);
    }
    else if (returnType == String.class && colSqlType == Types.LONGVARCHAR) {
      writeStringSetToLongVarChar(cl,
                                  mw,
                                  getter,
                                  psIdx,
                                  beanIdx,
                                  colPos,
                                  codeInfo,
                                  nullSensetive);
    }
    else if (returnType == byte[].class &&
             colSqlType != Types.LONGVARBINARY &&
             colSqlType != Types.BLOB) {
      writeSQLNativeSetValue(cl,
                             mw,
                             getter,
                             psIdx,
                             beanIdx,
                             colPos,
                             codeInfo,
                             colSqlType,
                             nullSensetive);
    }
    else if (returnType == byte[].class && colSqlType == Types.LONGVARBINARY) {
      writeByteArraySetToLongVarBinary(cl,
                                       mw,
                                       getter,
                                       psIdx,
                                       beanIdx,
                                       colPos,
                                       codeInfo,
                                       nullSensetive);
    }
    else if (returnType == java.util.Date.class &&
             (colSqlType == Types.DATE ||
              colSqlType == Types.TIMESTAMP ||
              colSqlType == Types.TIME)) {
      writeDateSetToSQLDateExteded(cl,
                                   mw,
                                   getter,
                                   psIdx,
                                   beanIdx,
                                   colPos,
                                   codeInfo,
                                   colSqlType,
                                   nullSensetive);
    }
    else if (isPrimitive(returnType) && returnType != void.class) {
      writePrimitiveSetValue(cl,
                             mw,
                             versionControlInfo,
                             getter,
                             psIdx,
                             beanIdx,
                             colPos,
                             incValue);
    }
    else if (isPrimitiveWrapper(returnType)) {
      writePrimitiveWrapperSetValue(cl,
                                    mw,
                                    column,
                                    getter,
                                    codeInfo,
                                    psIdx,
                                    beanIdx,
                                    colPos);
    }
    else {
      throw new RuntimeException(
        "Can not find code generation handler for columnName of type [" +
        colSqlType +
        "] with getter [" +
        getter +
        "].\n If this columnName is a CLOB or a BLOB please refer to the manual "
        +
        "for detail of handling these columns"
      );
    }
  }

  private static void writeDateSetToSQLDateExteded(final Class clazz,
                                                   final CodeVisitor mw,
                                                   final Method getter,
                                                   final int psIdx,
                                                   final int beanIdx,
                                                   final int colPos,
                                                   final CodeInfo codeInfo,
                                                   final int colSqlType,
                                                   final boolean nullSensetive)
    throws NoSuchMethodException
  {
    Class dateExtendedClass;
    switch (colSqlType) {
    case Types.DATE: {
      dateExtendedClass = Date.class;
      break;
    }
    case Types.TIME: {
      dateExtendedClass = Time.class;
      break;
    }
    default: {
      dateExtendedClass = Timestamp.class;
    }
    }
    if (nullSensetive) {
      Label nullHandler = null;
      nullHandler = new Label();
      mw.visitVarInsn(ALOAD, beanIdx);
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(clazz),
                         getter.getName(),
                         Type.getMethodDescriptor(getter));
      mw.visitInsn(DUP);
      final int dateidx = codeInfo._varindx++;
      mw.visitVarInsn(ASTORE, dateidx);
      mw.visitJumpInsn(IFNULL, nullHandler);
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitTypeInsn(NEW, Type.getInternalName(dateExtendedClass));
      mw.visitInsn(DUP);
      mw.visitVarInsn(ALOAD, dateidx);
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(java.util.Date.class),
                         "getTime",
                         Type.getMethodDescriptor(Type.LONG_TYPE,
                                                  new Type[]{})
      );
      mw.visitMethodInsn(INVOKESPECIAL,
                         Type.getInternalName(dateExtendedClass),
                         "<init>",
                         Type.getMethodDescriptor(Type.VOID_TYPE,
                                                  new Type[]{Type.getType(long.class)})
      );
      Method
        targetSetter
        = MethodsForPreparedStatement.findSetter(
        dateExtendedClass);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
      mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
      mw.visitLabel(nullHandler); //null handler begin
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitIntInsn(SIPUSH, colSqlType);
      targetSetter = PreparedStatement.class.getMethod("setNull",
                                                       new Class[]{int.class,
                                                                   int.class}
      );
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
    }
    else {
      //final int dateidx = codeInfo.varindx++;
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitTypeInsn(NEW, Type.getInternalName(dateExtendedClass));
      mw.visitInsn(DUP);
      mw.visitVarInsn(ALOAD, beanIdx);
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(clazz),
                         getter.getName(),
                         Type.getMethodDescriptor(getter));
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(java.util.Date.class),
                         "getTime",
                         Type.getMethodDescriptor(Type.LONG_TYPE,
                                                  new Type[]{})
      );
      mw.visitMethodInsn(INVOKESPECIAL,
                         Type.getInternalName(dateExtendedClass),
                         "<init>",
                         Type.getMethodDescriptor(Type.VOID_TYPE,
                                                  new Type[]{Type.getType(long.class)})
      );
      Method targetSetter = MethodsForPreparedStatement.findSetter(
        dateExtendedClass);
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
    }
  }

  private static void writeStringSetToLongVarChar(final Class clazz,
                                                  final CodeVisitor mw,
                                                  final Method getter,
                                                  final int psIdx,
                                                  final int beanIdx,
                                                  final int colPos,
                                                  final CodeInfo codeInfo,
                                                  final boolean nullSensetive)
    throws NoSuchMethodException
  {
    Label nullHandler = null;
    if (nullSensetive) {
      nullHandler = new Label();
    }
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(clazz),
                       getter.getName(),
                       Type.getMethodDescriptor(getter));
    if (nullSensetive) {
      mw.visitInsn(DUP);
    }
    final int stringidx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, stringidx);
    if (nullSensetive) {
      mw.visitJumpInsn(IFNULL, nullHandler);
    }
    mw.visitVarInsn(ALOAD, psIdx);//not null handler begin
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitTypeInsn(NEW, Type.getInternalName(StringReader.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, stringidx);
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(String.class),
                       "length",
                       Type.getMethodDescriptor(Type.INT_TYPE, new Type[]{}));
    final int slenidx = codeInfo._varindx++;
    mw.visitVarInsn(ISTORE, slenidx);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(StringReader.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(String.class)})
    );
    mw.visitVarInsn(ILOAD, slenidx);
    Method
      targetSetter
      = PreparedStatement.class.getMethod("setCharacterStream",
                                          new Class[]{int.class,
                                                      Reader.class,
                                                      int.class}
    );
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
    if (nullSensetive) {
      mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
      mw.visitLabel(nullHandler); //null handler begin
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitIntInsn(SIPUSH, Types.LONGVARCHAR);
      targetSetter = PreparedStatement.class.getMethod("setNull",
                                                       new Class[]{int.class,
                                                                   int.class}
      );
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
    }
  }

  private static void writeByteArraySetToLongVarBinary(final Class clazz,
                                                       final CodeVisitor mw,
                                                       final Method getter,
                                                       final int psIdx,
                                                       final int beanIdx,
                                                       final int colPos,
                                                       final CodeInfo codeInfo,
                                                       final boolean nullSensetive)
    throws NoSuchMethodException
  {
    Label nullHandler = null;
    if (nullSensetive) {
      nullHandler = new Label();
    }
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(clazz),
                       getter.getName(),
                       Type.getMethodDescriptor(getter));
    if (nullSensetive) {
      mw.visitInsn(DUP);
    }
    final int baidx = codeInfo._varindx++;
    mw.visitVarInsn(ASTORE, baidx);
    if (nullSensetive) {
      mw.visitJumpInsn(IFNULL, nullHandler);
    }
    mw.visitVarInsn(ALOAD, psIdx);//not null handler begin
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitTypeInsn(NEW, Type.getInternalName(ByteArrayInputStream.class));
    mw.visitInsn(DUP);
    mw.visitVarInsn(ALOAD, baidx);
    mw.visitInsn(DUP);
    mw.visitInsn(ARRAYLENGTH);
    final int balenidx = codeInfo._varindx++;
    mw.visitVarInsn(ISTORE, balenidx);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(ByteArrayInputStream.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(byte[].class)})
    );
    mw.visitVarInsn(ILOAD, balenidx);
    Method
      targetSetter
      = PreparedStatement.class.getMethod("setBinaryStream",
                                          new Class[]{int.class,
                                                      InputStream.class,
                                                      int.class}
    );
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
    if (nullSensetive) {
      mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
      mw.visitLabel(nullHandler); //null handler begin
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitIntInsn(SIPUSH, Types.LONGVARBINARY);
      targetSetter = PreparedStatement.class.getMethod("setNull",
                                                       new Class[]{int.class,
                                                                   int.class}
      );
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
    }
  }

  private static void writePrimitiveSetValue(final Class clazz,
                                             final CodeVisitor mw,
                                             VersionControlInfo versionControlInfo,
                                             final Method getter,
                                             final int psIdx,
                                             final int beanIdx,
                                             final int colPos,
                                             final boolean incValue)
  {
    mw.visitVarInsn(ALOAD, psIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(clazz),
                       getter.getName(),
                       Type.getMethodDescriptor(getter));
    if (incValue) {
      if (versionControlInfo.returnClass.equals(int.class)) {
        mw.visitIntInsn(SIPUSH, 1);
        mw.visitInsn(IADD);
      }
      else if (incValue && getter.getReturnType().equals(long.class)) {
        mw.visitIntInsn(SIPUSH, 1);
        mw.visitInsn(LADD);
      }
    }
    final
    Method targetSetter
      = MethodsForPreparedStatement.findSetter(getter.getReturnType());
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
  }

  private static void writePrimitiveWrapperSetValue(final Class clazz,
                                                    final CodeVisitor mw,
                                                    final MappedClass.MappedAttribute column,
                                                    final Method getter,
                                                    final CodeInfo codeInfo,
                                                    final int psIdx,
                                                    final int beanIdx,
                                                    final int colPos)
  {
    mw.visitVarInsn(ALOAD, beanIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(clazz),
                       getter.getName(),
                       Type.getMethodDescriptor(getter));

    int var = codeInfo._varindx++;

    mw.visitVarInsn(ASTORE, var);

    mw.visitVarInsn(ALOAD, var);
    Label nonNullHandler = new Label();
    mw.visitJumpInsn(IFNONNULL, nonNullHandler);
    mw.visitVarInsn(ALOAD, psIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitIntInsn(SIPUSH, column.getSqlType());
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(PreparedStatement.class),
                       MethodsForPreparedStatement.setNull.getName(),
                       Type.getMethodDescriptor(MethodsForPreparedStatement.setNull));
    mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
    mw.visitLabel(nonNullHandler);
    mw.visitVarInsn(ALOAD, psIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitVarInsn(ALOAD, var);

    Class type = getter.getReturnType();

    Method primitiveGetter
      = (Method) MethodsForJavaPrimitiveWrappers.WRAPPER_TO_PRIM_GETTER
      .get(type);
    Method psSetter = MethodsForPreparedStatement.findSetter(
      type);

    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(type),
                       primitiveGetter.getName(),
                       Type.getMethodDescriptor(primitiveGetter));
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(PreparedStatement.class),
                       psSetter.getName(),
                       Type.getMethodDescriptor(psSetter));
  }

  private static void writeDummyClobValue(final String className,
                                          final CodeVisitor mw,
                                          final int psIdx,
                                          final int colPos)
    throws NoSuchMethodException
  {
    mw.visitVarInsn(ALOAD, psIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitFieldInsn(GETSTATIC,
                      className,
                      FN_DUMMY_STRING,
                      Type.getDescriptor(String.class));
    final Method targetSetter
      = PreparedStatement.class.getMethod("setString",
                                          new Class[]{int.class,
                                                      String.class}
    );
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
  }

  private static void writeDummyBlobValue(final String className,
                                          final CodeVisitor mw,
                                          final int psIdx,
                                          final int colPos)
    throws NoSuchMethodException
  {
    mw.visitVarInsn(ALOAD, psIdx);
    mw.visitIntInsn(SIPUSH, colPos + 1);
    mw.visitFieldInsn(GETSTATIC,
                      className,
                      FN_DUMMY_BYTES,
                      Type.getDescriptor(byte[].class));
    final Method targetSetter
      = PreparedStatement.class.getMethod("setBytes",
                                          new Class[]{int.class,
                                                      byte[].class}
    );
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.PreparedStatement.class),
                       targetSetter.getName(),
                       Type.getMethodDescriptor(targetSetter));
  }

  private static void writeSQLNativeSetValue(final Class clazz,
                                             final CodeVisitor mw,
                                             final Method getter,
                                             final int psIdx,
                                             final int beanIdx,
                                             final int colPos,
                                             final CodeInfo codeInfo,
                                             final int colSqlType,
                                             final boolean nullSensetive)
    throws NoSuchMethodException
  {
    if (nullSensetive) {
      Label nullHandler = null;
      nullHandler = new Label();
      mw.visitVarInsn(ALOAD, beanIdx);
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(clazz),
                         getter.getName(),
                         Type.getMethodDescriptor(getter));
      final int objidx = codeInfo._varindx++;
      mw.visitInsn(DUP);
      mw.visitVarInsn(ASTORE, objidx);
      mw.visitJumpInsn(IFNULL, nullHandler);
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitVarInsn(ALOAD, objidx);
      Method
        targetSetter
        = MethodsForPreparedStatement.findSetter(getter.getReturnType());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
      mw.visitJumpInsn(GOTO, codeInfo._nextInstruction = new Label());
      mw.visitLabel(nullHandler); //null handler begin
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitIntInsn(SIPUSH, colSqlType);
      targetSetter = PreparedStatement.class.getMethod("setNull",
                                                       new Class[]{int.class,
                                                                   int.class}
      );
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
    }
    else {
      mw.visitVarInsn(ALOAD, psIdx);
      mw.visitIntInsn(SIPUSH, colPos + 1);
      mw.visitVarInsn(ALOAD, beanIdx);
      mw.visitMethodInsn(INVOKEVIRTUAL,
                         Type.getInternalName(clazz),
                         getter.getName(),
                         Type.getMethodDescriptor(getter));
      Method
        targetSetter
        = MethodsForPreparedStatement.findSetter(getter.getReturnType());
      mw.visitMethodInsn(INVOKEINTERFACE,
                         Type.getInternalName(PreparedStatement.class),
                         targetSetter.getName(),
                         Type.getMethodDescriptor(targetSetter));
    }
  }

  private static boolean isPrimitive(final Class cl)
  {
    return cl.isPrimitive();
  }

  private static boolean isPrimitiveWrapper(final Class cl)
  {
    return cl.equals(Boolean.class) ||
           cl.equals(Byte.class) ||
           cl.equals(Short.class) ||
           cl.equals(Integer.class) ||
           cl.equals(Float.class) ||
           cl.equals(Double.class) ||
           cl.equals(Long.class);
  }

  /**
   * java.sql.Date ,java.sql.Timestamp, java.sql.Time, java.math.BigDecimal
   */
  private static boolean isSQLNative(final Class cl)
  {
    return cl == java.sql.Date.class ||
           cl == java.sql.Timestamp.class ||
           cl == java.sql.Time.class ||
           cl == java.math.BigDecimal.class ||
           cl == java.net.URL.class;
  }

  public static void writeInit(final ClassWriter cw,
                               final String className,
                               Class persistorSuperClass)
  {
    final CodeVisitor mw = cw.visitMethod(ACC_PUBLIC,
                                          "<init>",
                                          Type.getMethodDescriptor(Type.getType(
                                            void.class), new Type[]{}),
                                          null,
                                          null
    );
    mw.visitVarInsn(ALOAD, 0);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(persistorSuperClass),
                       "<init>",
                       "()V");
    mw.visitInsn(RETURN);
    // this code uses a maximum of one stack element and one local variables
    mw.visitMaxs(1, 1);
  }

  public static void writeStaticInit(final ClassWriter cw,
                                     final String className)
  {
    final CodeVisitor mw = cw.visitMethod(ACC_STATIC,
                                          "<clinit>",
                                          Type.getMethodDescriptor(Type.getType(
                                            void.class), new Type[]{}),
                                          null,
                                          null
    );
    mw.visitIntInsn(BIPUSH, 1);
    mw.visitIntInsn(NEWARRAY, 8);
    mw.visitFieldInsn(PUTSTATIC,
                      className,
                      FN_DUMMY_BYTES,
                      Type.getDescriptor(byte[].class));
    mw.visitInsn(RETURN);
    // this code uses a maximum of one stack element and 0 local variables
    mw.visitMaxs(1, 0);
  }

  public static Class generateResultSetReader(final Class clazz,
                                              final MappedClass jdbcMap,
                                              String[] columnNames,
                                              String query,
                                              final boolean locatorsUpdateCopy,
                                              final boolean oracle,
                                              PersistenceClassLoader cl)
    throws Exception
  {
    Class persistorSuperClass = Object.class;
    final ClassWriter cw = new ClassWriter(false);
    final String className = "org/jdbcpersistence/generated/" +
                             CodeGenUtils.getShortName(clazz) +
                             "JDBCResultSetReader_" +
                             makeClassName(query);
    cw.visit(Constants.V1_3,
             ACC_PUBLIC | ACC_FINAL,
             className,
             Type.getInternalName(persistorSuperClass),
             new String[]{Type.getInternalName(ResultSetReader.class)},
             null);
    writeInit(cw, className, persistorSuperClass);
    if (!isMethodPresent(persistorSuperClass, M_JDBCP_LOAD_FROM_RS)) {
      writeSelectFromResultSet(clazz, cw, jdbcMap, columnNames);
    }
    final byte[] classBytes = cw.toByteArray();
    if ("true".equalsIgnoreCase(System.getProperty("jdbcpersistence.verbose"))) {
      CodeGenUtils.writeToFile(className, classBytes);
      CodeGenUtils.echo(className);
    }
    Class result = cl.define(className.replace('/', '.'), classBytes);
    //
    return result;
  }

  public static boolean isMethodPresent(Class persistorSuperClass,
                                        Method method)
  {
    try {
      Method m = persistorSuperClass.getMethod(method.getName(),
                                               method.getParameterTypes());
      return (!Modifier.isAbstract(m.getModifiers()));
    } catch (NoSuchMethodException e) {
      return false;
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean matchBlobWriter(final Method method)
  {
    final Class[] paramTypes = method.getParameterTypes();
    return method.getReturnType() == void.class &&
           paramTypes.length > 0 &&
           paramTypes[0] == OutputStream.class;
  }

  private static boolean matchClobWriter(final Method method)
  {
    final Class[] paramTypes = method.getParameterTypes();
    return method.getReturnType() == void.class &&
           paramTypes.length > 0 &&
           paramTypes[0] == Writer.class;
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
}

