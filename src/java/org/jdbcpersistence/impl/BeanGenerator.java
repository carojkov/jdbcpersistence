/**
 * JDBCPersistence framework for java
 *   Copyright (C) 2004-2010 Alex Rojkov
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

import org.jdbcpersistence.impl.asm.ClassWriter;
import org.jdbcpersistence.impl.asm.CodeVisitor;
import org.jdbcpersistence.impl.asm.Constants;
import org.jdbcpersistence.impl.asm.Label;
import org.jdbcpersistence.impl.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author Alex Rojkov
 */
class BeanGenerator implements Constants
{
  private ClassWriter _classWriter;
  private BeanProperties _beanProperties;
  private Class _superClass;
  private String _beanClassName;

  BeanGenerator(Class beanClass)
  {
    _beanProperties = new BeanProperties(beanClass);
    initializeSuperClass();
    initializeBeanClassName();
  }

  private void initializeSuperClass()
  {
    if (_beanProperties.getBeanClass().isInterface())
      _superClass = Object.class;
    else
      _superClass = _beanProperties.getBeanClass();
  }

  private void initializeBeanClassName()
  {
    _beanClassName = "org/jdbcpersistence/generated/"
                     + _beanProperties.getBeanClass().getSimpleName()
                     + "Impl";
  }

  public byte[] generate()
    throws Exception
  {
    _classWriter = new ClassWriter(true);

    generateClassHeader();

    writeInit();

    generateFields();

    generateMethods();

    byte[] bytes = _classWriter.toByteArray();

    if ("true".equalsIgnoreCase(System.getProperty("jdbcpersistence.verbose"))) {
      CodeGenUtils.writeToFile(_beanClassName, bytes);
      CodeGenUtils.echo(_beanClassName);
    }

    return bytes;
  }

  private void generateClassHeader()
  {
    String[] interfaces;

    if (_beanProperties.isBeanClassInterface()) {
      interfaces
        = new String[]{Type.getInternalName(_beanProperties.getBeanClass())};
    }
    else {
      interfaces = new String[0];
    }

    _classWriter.visit(Constants.V1_5,
                       ACC_PUBLIC | ACC_FINAL,
                       _beanClassName,
                       Type.getInternalName(_superClass),
                       interfaces,
                       null);
  }

  private void generateFields()
  {
    Class superClass = _superClass;
    Collection<BeanProperty> properties = _beanProperties.getProperties();
    for (Iterator<BeanProperty> iterator = properties.iterator();
         iterator.hasNext(); ) {
      BeanProperty property = iterator.next();
      generateField(superClass, property);
    }
  }

  private void generateMethods()
  {
    Collection<BeanProperty> properties = _beanProperties.getProperties();

    for (Iterator<BeanProperty> iterator = properties.iterator();
         iterator.hasNext(); ) {
      BeanProperty property = iterator.next();
      generateMethods(property);
    }
  }

  private void generateMethods(BeanProperty property)
  {
    if (property.getGetter() != null && property.isGetterAbstract())
      generateMethod(property.getGetter(), _beanClassName);

    if (property.getSetter() != null && property.isSetterAbstract())
      generateMethod(property.getSetter(), _beanClassName);
  }

  String getBeanClassNameDotted()
  {
    return _beanClassName.replace('/', '.');
  }

  private void writeInit()
  {
    final CodeVisitor mw = _classWriter.visitMethod(ACC_PUBLIC,
                                                    "<init>",
                                                    Type.getMethodDescriptor(
                                                      Type.getType(
                                                        void.class),
                                                      new Type[]{}),
                                                    null,
                                                    null);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(_superClass),
                       "<init>",
                       "()V");
    mw.visitInsn(RETURN);

    mw.visitMaxs(1, 1);
  }

  private void generateField(Class superClass,
                             BeanProperty beanProperty)
  {
    final String fieldName = beanProperty.getName();

    if (isDeclaredFieldPresent(superClass, beanProperty.getName()))
      return;

    final Class fieldType = beanProperty.getFieldType();

    _classWriter.visitField(Constants.ACC_PRIVATE,
                            fieldName,
                            Type.getDescriptor(fieldType),
                            null,
                            null);
  }

  private boolean isDeclaredFieldPresent(final Class subject,
                                         final String fieldName)
  {
    return getDeclaredField(subject, fieldName) != null;
  }

  private Field getDeclaredField(final Class subject,
                                 final String fieldName)
  {
    try {
      return subject.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      return null;
    }
  }

  private void generateMethod(Method method,
                              String className)
  {
    CodeVisitor mw = _classWriter.visitMethod(method.getModifiers()
                                              ^ ACC_ABSTRACT,
                                              method.getName(),
                                              Type.getMethodDescriptor(method),
                                              toInternalNames(method.getExceptionTypes()),
                                              null);
    Class type = getClassOfMethodSubject(method);
    if (isPrimitive(method)) {
      if (isReader(method)) {
        generatePrimitiveReader(mw, method, className);
      }
      else {
        generatePrimitiveWriter(mw, method, className);
      }
    }
    else if (type == Reader.class || type == Writer.class) {
      if (isReader(method)) {
        generateObjectReaderFromReader(mw, method, className);
      }
      else {
        generateObjectWriterToWriter(mw, method, className);
      }
    }
    else if (type == InputStream.class || type == OutputStream.class) {
      if (isReader(method)) {
        generateObjectReaderFromInputStream(mw, method, className);
      }
      else {
        generateObjectWriterToOutputStream(mw, method, className);
      }
    }
    else {
      if (isReader(method)) {
        generateObjectReader(mw, method, className);
      }
      else {
        generateObjectWriter(mw, method, className);
      }
    }
  }

  private void generateObjectWriterToOutputStream(CodeVisitor mw,
                                                  Method method,
                                                  String className)
  {
    String fieldName = makeFieldName(method);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitFieldInsn(GETFIELD,
                      className,
                      fieldName,
                      Type.getDescriptor(byte[].class));
    Label l = new Label();
    mw.visitJumpInsn(IFNULL, l);
    mw.visitVarInsn(ALOAD, 1);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitFieldInsn(GETFIELD,
                      className,
                      fieldName,
                      Type.getDescriptor(byte[].class));
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(OutputStream.class),
                       "write",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(byte[].class)}));
    mw.visitLabel(l);
    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
  }

  private void generateObjectReaderFromInputStream(CodeVisitor mw,
                                                   Method method,
                                                   String className)
  {
    String fieldName = makeFieldName(method);
    int idx = 3, baosIdx = 2;
    mw.visitTypeInsn(NEW, Type.getInternalName(ByteArrayOutputStream.class));
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(ByteArrayOutputStream.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{}));
    mw.visitVarInsn(ASTORE, baosIdx);
    mw.visitIntInsn(SIPUSH, 4098);
    mw.visitIntInsn(NEWARRAY, 8);
    int bufferIdx = idx++;
    mw.visitVarInsn(ASTORE, bufferIdx);
    Label startWhile = new Label();
    mw.visitLabel(startWhile);
    mw.visitVarInsn(ALOAD, 1);
    mw.visitVarInsn(ALOAD, bufferIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(InputStream.class),
                       "read",
                       Type.getMethodDescriptor(Type.INT_TYPE,
                                                new Type[]{Type.getType(byte[].class)}));
    int readBytesLIdx = idx++;
    mw.visitInsn(DUP);
    mw.visitVarInsn(ISTORE, readBytesLIdx);
    Label afterAWhile = new Label();
    mw.visitJumpInsn(IFLT, afterAWhile);
    mw.visitVarInsn(ALOAD, baosIdx);
    mw.visitVarInsn(ALOAD, bufferIdx);
    mw.visitIntInsn(BIPUSH, 0);
    mw.visitVarInsn(ILOAD, readBytesLIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(ByteArrayOutputStream.class),
                       "write",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(byte[].class),
                                                           Type.getType(int.class),
                                                           Type.getType(int.class)}));
    mw.visitJumpInsn(GOTO, startWhile);
    mw.visitLabel(afterAWhile);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitVarInsn(ALOAD, baosIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(ByteArrayOutputStream.class),
                       "toByteArray",
                       Type.getMethodDescriptor(Type.getType(byte[].class),
                                                new Type[]{}));
    mw.visitFieldInsn(PUTFIELD,
                      className,
                      fieldName,
                      Type.getDescriptor(byte[].class));
    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
  }

  private void generateObjectWriterToWriter(CodeVisitor mw,
                                            Method method,
                                            String className)
  {
    String fieldName = makeFieldName(method);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitFieldInsn(GETFIELD,
                      className,
                      fieldName,
                      Type.getDescriptor(String.class));
    Label l = new Label();
    mw.visitJumpInsn(IFNULL, l);
    mw.visitVarInsn(ALOAD, 1);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitFieldInsn(GETFIELD,
                      className,
                      fieldName,
                      Type.getDescriptor(String.class));
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(Writer.class),
                       "write",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(String.class)}));
    mw.visitLabel(l);
    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
  }

  private void generateObjectReaderFromReader(CodeVisitor mw,
                                              Method method,
                                              String className)
  {
    String fieldName = makeFieldName(method);
    int idx = 3, swIdx = 2;
    mw.visitTypeInsn(NEW, Type.getInternalName(StringWriter.class));
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL,
                       Type.getInternalName(StringWriter.class),
                       "<init>",
                       Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{}));
    mw.visitVarInsn(ASTORE, swIdx);
    mw.visitIntInsn(SIPUSH, 4098);
    mw.visitIntInsn(NEWARRAY, 5);
    int bufferIdx = idx++;
    mw.visitVarInsn(ASTORE, bufferIdx);
    Label startWhile = new Label();
    mw.visitLabel(startWhile);
    mw.visitVarInsn(ALOAD, 1);
    mw.visitVarInsn(ALOAD, bufferIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(Reader.class),
                       "read",
                       Type.getMethodDescriptor(Type.INT_TYPE,
                                                new Type[]{Type.getType(char[].class)}));
    int readCharsLIdx = idx++;
    mw.visitInsn(DUP);
    mw.visitVarInsn(ISTORE, readCharsLIdx);
    Label afterAWhile = new Label();
    mw.visitJumpInsn(IFLT, afterAWhile);
    mw.visitVarInsn(ALOAD, swIdx);
    mw.visitVarInsn(ALOAD, bufferIdx);
    mw.visitIntInsn(BIPUSH, 0);
    mw.visitVarInsn(ILOAD, readCharsLIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(StringWriter.class),
                       "write",
                       Type.getMethodDescriptor(Type.VOID_TYPE,
                                                new Type[]{Type.getType(char[].class),
                                                           Type.getType(int.class),
                                                           Type.getType(int.class)}));
    mw.visitJumpInsn(GOTO, startWhile);
    mw.visitLabel(afterAWhile);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitVarInsn(ALOAD, swIdx);
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(StringWriter.class),
                       "getBuffer",
                       Type.getMethodDescriptor(Type.getType(StringBuffer.class),
                                                new Type[]{}));
    mw.visitMethodInsn(INVOKEVIRTUAL,
                       Type.getInternalName(StringBuffer.class),
                       "toString",
                       Type.getMethodDescriptor(Type.getType(String.class),
                                                new Type[]{}));
    mw.visitFieldInsn(PUTFIELD,
                      className,
                      fieldName,
                      Type.getDescriptor(String.class));
    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
  }

  private void generatePrimitiveWriter(CodeVisitor cw,
                                       Method method,
                                       String className)
  {
    String fieldName = makeFieldName(method);
    Class type = getClassOfMethodSubject(method);
    cw.visitVarInsn(ALOAD, 0);
    if (type == int.class ||
        type == boolean.class ||
        type == char.class ||
        type == short.class ||
        type == byte.class) {
      cw.visitVarInsn(ILOAD, 1);
    }
    else if (type == long.class) {
      cw.visitVarInsn(LLOAD, 1);
    }
    else if (type == float.class) {
      cw.visitVarInsn(FLOAD, 1);
    }
    else if (type == double.class) {
      cw.visitVarInsn(DLOAD, 1);
    }
    cw.visitFieldInsn(PUTFIELD, className, fieldName, Type.getDescriptor(type));
    cw.visitInsn(RETURN);
    cw.visitMaxs(0, 0);
  }

  private void generateObjectWriter(CodeVisitor cw,
                                    Method method,
                                    String className)
  {
    String fieldName = makeFieldName(method);
    Class type = getClassOfMethodSubject(method);
    cw.visitVarInsn(ALOAD, 0);
    cw.visitVarInsn(ALOAD, 1);
    cw.visitFieldInsn(PUTFIELD, className, fieldName, Type.getDescriptor(type));
    cw.visitInsn(RETURN);
    cw.visitMaxs(0, 0);
  }

  private void generatePrimitiveReader(CodeVisitor cw,
                                       Method method,
                                       String className)
  {
    String fieldName = makeFieldName(method);
    Class type = getClassOfMethodSubject(method);
    cw.visitVarInsn(ALOAD, 0);
    cw.visitFieldInsn(GETFIELD, className, fieldName, Type.getDescriptor(type));
    if (type == int.class ||
        type == boolean.class ||
        type == char.class ||
        type == short.class ||
        type == byte.class) {
      cw.visitInsn(IRETURN);
    }
    else if (type == long.class) {
      cw.visitInsn(LRETURN);
    }
    else if (type == float.class) {
      cw.visitInsn(FRETURN);
    }
    else if (type == double.class) {
      cw.visitInsn(DRETURN);
    }
    cw.visitFieldInsn(PUTFIELD, className, fieldName, Type.getDescriptor(type));
    cw.visitMaxs(0, 0);
  }

  private void generateObjectReader(CodeVisitor cw,
                                    Method method,
                                    String className)
  {
    String fieldName = makeFieldName(method);
    Class type = getClassOfMethodSubject(method);
    cw.visitVarInsn(ALOAD, 0);
    cw.visitFieldInsn(GETFIELD, className, fieldName, Type.getDescriptor(type));
    cw.visitInsn(ARETURN);
    cw.visitFieldInsn(PUTFIELD, className, fieldName, Type.getDescriptor(type));
    cw.visitMaxs(0, 0);
  }

  private Class getClassOfMethodSubject(Method method)
  {
    Class t = method.getReturnType();
    if (t == null || t == void.class) {
      t = method.getParameterTypes()[0];
    }
    return t;
  }

  private boolean isPrimitive(Method method)
  {
    Class t = getClassOfMethodSubject(method);
    return t.isPrimitive();
  }

  private String makeFieldName(Method method)
  {
    String mn = method.getName();
    String temp = null;
    if (mn.startsWith("get") || mn.startsWith("set")) {
      temp = mn.substring(3);
    }
    else if (mn.startsWith("is")) {
      temp = mn.substring(2);
    }
    else if (mn.startsWith("write")) {
      temp = mn.substring(5);
    }
    else if (mn.startsWith("read")) {
      temp = mn.substring(4);
    }
    else {
      throw new RuntimeException("Can not make a field for method [" +
                                 method +
                                 "]");
    }
    char[] name = temp.toCharArray();
    name[0] = Character.toLowerCase(name[0]);
    return new String(name);
  }

  private String[] toInternalNames(Class[] classes)
  {
    String[] result = new String[classes.length];
    for (int i = 0; i < classes.length; i++) {
      result[i] = Type.getInternalName(classes[i]);
    }
    return result;
  }

  private boolean isReader(Method method)
  {
    String name = method.getName();
    return (name.startsWith("get") ||
            name.startsWith("is") ||
            name.startsWith("read"));
  }

  private static class BeanProperties
  {
    private final Map<String,BeanProperty> _properties
      = new HashMap<String,BeanProperty>();
    private final Class _beanClass;

    private BeanProperties(Class beanClass)
    {
      _beanClass = beanClass;
      initialize();
    }

    Class getBeanClass()
    {
      return _beanClass;
    }

    boolean isBeanClassInterface()
    {
      return _beanClass.isInterface();
    }

    Collection<BeanProperty> getProperties()
    {
      return _properties.values();
    }

    private void initialize()
    {
      introspect();
    }

    private void introspect()
    {
      final Method[] methods = getEligibleMethods();

      for (Method method : methods) {
        add(method);
      }
    }

    private Method[] getEligibleMethods()
    {
      final Method[] methods = _beanClass.getMethods();

      final List<Method> eligibleMethodList = new ArrayList<Method>(16);
      for (Method method : methods) {
        if (isEligible(method))
          eligibleMethodList.add(method);
      }

      final Method[] eligibleMethods = new Method[eligibleMethodList.size()];
      eligibleMethodList.toArray(eligibleMethods);

      return eligibleMethods;
    }

    private boolean isEligible(Method method)
    {
      if (method.getParameterTypes().length < 2
          && !Object.class.equals(method.getDeclaringClass()))
        return true;

      return false;
    }

    private MethodType getMethodType(Method method)
    {
      Class[] parameterTypes = method.getParameterTypes();
      MethodType methodType = null;

      if (parameterTypes.length == 0)
        methodType = MethodType.GETTER;
      else if (parameterTypes.length == 1)
        methodType = MethodType.SETTER;
      else
        methodType = MethodType.UNDEFINED;

      return methodType;
    }

    private void add(Method method)
    {
      final String propertyName = getPropertyName(method);

      MethodType methodType = getMethodType(method);

      BeanProperty beanProperty = _properties.get(propertyName);

      if (beanProperty == null) {
        beanProperty = createBeanProperty(propertyName, method, methodType);

        _properties.put(propertyName, beanProperty);
      }
      else {
        beanProperty.registerMethod(method, methodType);
      }
    }

    private String getPropertyName(Method method)
    {
      final String methodName = method.getName();

      final char[] nameBuffer = methodName.toCharArray();

      int i;
      for (i = 0;
           i < nameBuffer.length && Character.isLowerCase(nameBuffer[i]);
           i++)
        ;

      if (i == nameBuffer.length)
        throw new IllegalArgumentException(String.format(
          "method %1$s is neither setter nor getter",
          method));

      nameBuffer[i] = Character.toLowerCase(nameBuffer[i]);

      return new String(nameBuffer, i, nameBuffer.length - i);
    }

    private final BeanProperty createBeanProperty(String propertyName,
                                                  Method method,
                                                  MethodType methodType)
    {
      final BeanProperty beanProperty = new BeanProperty(propertyName);

      if (methodType == MethodType.GETTER)
        beanProperty.setGetter(method);
      else if (methodType == MethodType.SETTER)
        beanProperty.setSetter(method);
      else
        throw new IllegalArgumentException(String.format(
          "method %1$s is neither setter nor getter"));

      return beanProperty;
    }
  }

  private static class BeanProperty
  {
    private final String _name;
    private Method _getter;
    private Method _setter;

    private BeanProperty(String name)
    {
      _name = name;
    }

    public String getName()
    {
      return _name;
    }

    public Method getGetter()
    {
      return _getter;
    }

    public boolean isGetterAbstract()
    {
      return Modifier.isAbstract(_getter.getModifiers());
    }

    public void setGetter(Method method)
    {
      if (_getter != null)
        throw new IllegalStateException(String.format(
          "getter is already set to %1$s",
          method));

      _getter = method;
    }

    public Method getSetter()
    {
      return _setter;
    }

    public boolean isSetterAbstract()
    {
      return Modifier.isAbstract(_setter.getModifiers());
    }

    public void setSetter(Method method)
    {
      if (_setter != null)
        throw new IllegalStateException(String.format(
          "setter is already set to %1$s",
          _setter));

      _setter = method;
    }

    private void registerMethod(Method method,
                                MethodType methodType)
    {
      if (methodType == MethodType.GETTER)
        setGetter(method);
      else if (methodType == MethodType.SETTER)
        setSetter(method);
      else
        throw new IllegalArgumentException(String.format(
          "method %1$s is neither setter nor getter"));
    }

    private Class getFieldType()
    {
      Class type = null;

      if (_setter != null)
        type = _setter.getParameterTypes()[0];

      if (type == null && _getter != null)
        type = _getter.getReturnType();

      return type;
    }

  }

  private enum MethodType
  {
    GETTER, SETTER, UNDEFINED;
  }
}
