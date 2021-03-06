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
import org.jdbcpersistence.impl.MethodsForPreparedStatement;
import org.jdbcpersistence.impl.PersistorGenerator;
import org.jdbcpersistence.impl.SQLUtils;
import org.jdbcpersistence.impl.SqlStatementFactory;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.sql.*;

public class DeleteGenerator implements Generator, Constants
{
  private MappedClass _mappedClass;
  private boolean _isUseExecute;

  private Class _bean;
  private ClassWriter _classWriter;
  private String _persistorClassName;
  private boolean _isBatch;

  public DeleteGenerator(MappedClass mappedClass,
                         boolean isUseExecute,
                         boolean isBatch,
                         Class bean,
                         ClassWriter classWriter,
                         String persistorClassName)
  {
    _mappedClass = mappedClass;
    _isUseExecute = isUseExecute;
    _bean = bean;
    _classWriter = classWriter;
    _persistorClassName = persistorClassName;
    _isBatch = isBatch;
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
    final CodeVisitor mw;

    if (_isBatch) {
      mw = _classWriter.visitMethod(ACC_PUBLIC,
                                    "delete",
                                    Type.getMethodDescriptor(Type.getType(int[].class),
                                                             new Type[]{Type.getType(
                                                               Connection.class),
                                                                        Type.getType(
                                                                          Object[].class)}
                                    ),
                                    PersistorGenerator.INSERT_DELETE_UPDATE_BATCH_EXCEPTIONS,
                                    null
      );
    }
    else {
      mw = _classWriter.visitMethod(ACC_PUBLIC,
                                    "delete",
                                    Type.getMethodDescriptor(Type.INT_TYPE,
                                                             new Type[]{Type.getType(
                                                               Connection.class),
                                                                        Type.getType(
                                                                          Object.class)}
                                    ),
                                    PersistorGenerator.INSERT_DELETE_UPDATE_EXCEPTIONS,
                                    null
      );
    }

    if (_mappedClass.getIdentifyingColumns().length == 0) {
      PersistorGenerator.writeThrowNoPKException(mw, _mappedClass);

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
    mw.visitLdcInsn(SqlStatementFactory.makeDelete(_mappedClass));
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
    if (_isBatch) {
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
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(_bean));
      mw.visitVarInsn(ASTORE, insObjIdx);
    }
    else {
      //make local varible for an object that is being persisted
      mw.visitVarInsn(ALOAD, deleteSubjIdx);
      mw.visitTypeInsn(CHECKCAST, Type.getInternalName(_bean));
      mw.visitVarInsn(ASTORE, insObjIdx);
    }
    MappedClass.MappedAttribute[] identifyingColumns
      = _mappedClass.getIdentifyingColumns();
    for (int i = 0; i < identifyingColumns.length; i++) {
      final MappedClass.MappedAttribute column = identifyingColumns[i];
      PersistorGenerator.writeSetPreparedStatementValue(_bean,
                                                        _persistorClassName,
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
      = _mappedClass.getVersionControlColumn();
    if (versionControlColumn != null) {
      PersistorGenerator.writeSetPreparedStatementValue(_bean,
                                                        _persistorClassName,
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
    if (_isBatch) {
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
    if (_isBatch) {
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
    if (_isBatch) {
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
      if (_isUseExecute) {
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
    final Method methodClose;

    try {
      methodClose = SQLUtils.class.getDeclaredMethod("close",
                                                     new Class[]{ResultSet.class,
                                                                 Statement.class}
      );
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
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
    if (_isBatch) {
      mw.visitVarInsn(ALOAD, resultIdx);
      mw.visitInsn(ARETURN);
    }
    else {
      mw.visitVarInsn(ILOAD, resultIdx);
      mw.visitInsn(IRETURN);
    }
  }

  @Override
  public void generateTail()
  {

  }
}
