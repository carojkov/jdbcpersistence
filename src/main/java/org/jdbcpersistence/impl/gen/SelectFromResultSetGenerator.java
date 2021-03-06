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
import org.jdbcpersistence.impl.MethodsForResultSet;
import org.jdbcpersistence.impl.PersistorGenerator;
import org.objectweb.asm.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SelectFromResultSetGenerator implements Generator, Constants
{
  private final Class _bean;
  private final ClassWriter _classWriter;
  private final MappedClass _mappedClass;
  private final String[] _columnNames;

  public SelectFromResultSetGenerator(Class clazz,
                                      ClassWriter classWriter,
                                      MappedClass mappedClass,
                                      String[] columnNames)
  {

    _bean = clazz;
    _classWriter = classWriter;
    _mappedClass = mappedClass;
    _columnNames = columnNames;
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
    final CodeVisitor mw = _classWriter.visitMethod(ACC_PUBLIC,
                                                    "read",
                                                    Type.getMethodDescriptor(
                                                      Type.getType(
                                                        java.util.List.class),
                                                      new Type[]{
                                                        Type.getType(
                                                          ResultSet.class),
                                                        Type.getType(
                                                          List.class)}
                                                    ),
                                                    PersistorGenerator.INSERT_DELETE_UPDATE_EXCEPTIONS,
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
    mw.visitTypeInsn(NEW, Type.getInternalName(_bean));
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(_bean),
                       "<init>",
                       "()V");
    mw.visitVarInsn(ASTORE, beanIdx);
    for (int i = 0; i < _columnNames.length; i++) {
      String columnName = _columnNames[i];

      final MappedClass.MappedAttribute column = _mappedClass.getColumn(
        columnName);

      if (column == null || column.getSetter() == null)
        continue;

      codeInfo._nextInstruction = null;
      PersistorGenerator.writeSetBeanPropertyFromResultSet(_bean,
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

  @Override
  public void generateTail()
  {

  }
}
