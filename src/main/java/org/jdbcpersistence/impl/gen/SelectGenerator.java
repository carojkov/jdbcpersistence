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
import org.jdbcpersistence.impl.*;
import org.objectweb.asm.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by alex on 2014-04-26.
 */
public class SelectGenerator implements Generator, Constants
{
  private Class _bean;
  private MappedClass _mappedClass;
  private ClassWriter _classWriter;

  public SelectGenerator(Class bean,
                         MappedClass mappedClass,
                         ClassWriter classWriter)
  {
    _bean = bean;
    _mappedClass = mappedClass;
    _classWriter = classWriter;
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
    final int valueArrayIdx = 2;
    final CodeVisitor mw
      = _classWriter.visitMethod(ACC_PUBLIC, "load",
                                 Type.getMethodDescriptor(Type.getType(Object.class),
                                                          new Type[]{Type.getType(
                                                            Connection.class), Type
                                                                       .getType(
                                                                         Object[].class)}
                                 ),
                                 PersistorGenerator.INSERT_DELETE_UPDATE_EXCEPTIONS,
                                 null
    );
    if (_mappedClass.getIdentifyingColumns().length == 0) {
      PersistorGenerator.writeThrowNoPKException(mw, _mappedClass);
      return;
    }
    final Label methodStart = new Label();
    final Label methodEnd = new Label();
    mw.visitLabel(methodStart);
    //
    final CodeInfo codeInfo = new CodeInfo();
    codeInfo._varindx = 3;
    //get java.sql.JDBCConnection from JDBCPersistence and store it in conn local variable
    final int conn = 1;
    //declare PreparedStatement and assign it value of null
    final int psSelectIdx = codeInfo._varindx++;
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ASTORE, psSelectIdx);
    mw.visitLocalVariable("psSelect",
                          Type.getDescriptor(PreparedStatement.class),
                          methodStart,
                          methodEnd,
                          psSelectIdx);
    //declare ResultSet and assign it value of null
    final int rsSelectIdx = codeInfo._varindx++;
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ASTORE, rsSelectIdx);
    mw.visitLocalVariable("rsSelect",
                          Type.getDescriptor(ResultSet.class),
                          methodStart,
                          methodEnd,
                          psSelectIdx);
    final int resultIdx = codeInfo._varindx++;
    mw.visitInsn(ACONST_NULL);
    mw.visitVarInsn(ASTORE, resultIdx);
    mw.visitLocalVariable("result",
                          Type.getDescriptor(_bean),
                          methodStart,
                          methodEnd,
                          resultIdx);
    //
    final Label beginTryBlock = new Label();
    final Label endTryBlock = new Label();
    final Label catchSqlBlock = new Label();
    final Label catchThrBlock = new Label();
    final Label finallyExceptionHandler = new Label();
    //obtain prepared statement
    mw.visitLabel(beginTryBlock);
    mw.visitVarInsn(ALOAD, conn);
    mw.visitLdcInsn(SqlStatementFactory.makeSelect(_mappedClass));
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(java.sql.Connection.class),
                       MethodsForConnection.prepareStatement.getName(),
                       Type.getMethodDescriptor(MethodsForConnection.prepareStatement));
    mw.visitVarInsn(ASTORE, psSelectIdx);
    //set best row identifier values
    MappedClass.MappedAttribute[] identifyingColumns
      = _mappedClass.getIdentifyingColumns();

    final int length = identifyingColumns.length;
    for (int i = 0; i < length; i++) {
      MappedClass.MappedAttribute column = identifyingColumns[i];
      PersistorGenerator.writeSetBestIdentifierValueForSelect(mw,
                                                              column,
                                                              psSelectIdx,
                                                              i,
                                                              valueArrayIdx);
    }
    //
    mw.visitVarInsn(ALOAD, psSelectIdx);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(PreparedStatement.class),
                       MethodsForPreparedStatement.executeQuery.getName(),
                       Type.getMethodDescriptor(MethodsForPreparedStatement.executeQuery));
    mw.visitInsn(DUP);//duplicate local variable result set
    mw.visitVarInsn(ASTORE, rsSelectIdx);
    mw.visitLocalVariable("rsSelect",
                          Type.getDescriptor(ResultSet.class),
                          methodStart,
                          methodEnd,
                          rsSelectIdx);
    mw.visitMethodInsn(INVOKEINTERFACE,
                       Type.getInternalName(ResultSet.class),
                       MethodsForResultSet.next.getName(),
                       Type.getMethodDescriptor(MethodsForResultSet.next));
    mw.visitJumpInsn(IFEQ, endTryBlock);
    //create result and set its properties
    mw.visitTypeInsn(NEW, Type.getInternalName(_bean));
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(_bean),
                       "<init>",
                       "()V");
    mw.visitVarInsn(ASTORE, resultIdx);
    //
    for (int i = 0; i < length; i++) {
      MappedClass.MappedAttribute column = identifyingColumns[i];
      PersistorGenerator.writeSetBeanPropertyFromArray(_bean,
                                                       mw,
                                                       column,
                                                       resultIdx,
                                                       i,
                                                       valueArrayIdx);
    }
    MappedClass.MappedAttribute[] regularColumns
      = _mappedClass.getRegularColumns();

    int columnIndex = 0;

    for (int i = 0; i < regularColumns.length; i++) {
      final MappedClass.MappedAttribute column = regularColumns[i];
      if (column.getSetter() == null)
        continue;

      codeInfo._nextInstruction = null;
      PersistorGenerator.writeSetBeanPropertyFromResultSet(_bean,
                                                           mw,
                                                           column,
                                                           codeInfo,
                                                           rsSelectIdx,
                                                           resultIdx,
                                                           columnIndex,
                                                           false);
      columnIndex++;
      if (codeInfo._nextInstruction != null) {
        mw.visitLabel(codeInfo._nextInstruction);
      }
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
    mw.visitVarInsn(ALOAD, rsSelectIdx);
    mw.visitVarInsn(ALOAD, psSelectIdx);
    mw.visitMethodInsn(INVOKESTATIC,
                       Type.getInternalName(SQLUtils.class),
                       MethodsForSqlUtil.M_UTL_close.getName(),
                       Type.getMethodDescriptor(MethodsForSqlUtil.M_UTL_close));
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
    mw.visitVarInsn(ALOAD, resultIdx);
    mw.visitInsn(ARETURN);
    mw.visitMaxs(7, codeInfo._varindx);
  }

  @Override
  public void generateTail()
  {
  }
}
