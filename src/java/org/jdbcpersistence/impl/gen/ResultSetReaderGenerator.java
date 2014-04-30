package org.jdbcpersistence.impl.gen;

import org.jdbcpersistence.MappedClass;
import org.jdbcpersistence.ResultSetReader;
import org.jdbcpersistence.impl.CodeGenUtils;
import org.jdbcpersistence.impl.PersistenceClassLoader;
import org.jdbcpersistence.impl.PersistorGenerator;
import org.jdbcpersistence.impl.asm.ClassWriter;
import org.jdbcpersistence.impl.asm.Constants;
import org.jdbcpersistence.impl.asm.Type;

public class ResultSetReaderGenerator implements Generator, Constants
{
  private final Class clazz;
  private final MappedClass jdbcMap;
  private String[] columnNames;
  private String query;
  private final boolean locatorsUpdateCopy;
  private final boolean oracle;
  private PersistenceClassLoader cl;

  private Class _reader;

  public ResultSetReaderGenerator(Class clazz,
                                  MappedClass jdbcMap,
                                  String[] columnNames,
                                  String query,
                                  boolean locatorsUpdateCopy,
                                  boolean oracle,
                                  PersistenceClassLoader cl)
  {
    this.clazz = clazz;
    this.jdbcMap = jdbcMap;
    this.columnNames = columnNames;
    this.query = query;
    this.locatorsUpdateCopy = locatorsUpdateCopy;
    this.oracle = oracle;
    this.cl = cl;
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
    PersistorGenerator.writeInit(cw, className, persistorSuperClass);
    if (!PersistorGenerator.isMethodPresent(persistorSuperClass,
                                            PersistorGenerator.M_JDBCP_LOAD_FROM_RS)) {
      PersistorGenerator.writeSelectFromResultSet(clazz,
                                                  cw,
                                                  jdbcMap,
                                                  columnNames);
    }
    final byte[] classBytes = cw.toByteArray();
    if ("true".equalsIgnoreCase(System.getProperty("jdbcpersistence.verbose"))) {
      CodeGenUtils.writeToFile(className, classBytes);
      CodeGenUtils.echo(className);
    }

    _reader = cl.define(className.replace('/', '.'), classBytes);
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
