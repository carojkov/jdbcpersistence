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
