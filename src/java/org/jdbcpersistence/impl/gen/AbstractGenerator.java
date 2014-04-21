package org.jdbcpersistence.impl.gen;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGenerator implements Generator
{
  private List<Generator> _generators = new ArrayList<>();

  @Override
  public void generateBody()
  {
    for (Generator generator : _generators) {
      generator.generateBody();
    }
  }
}

//title for class is its name and extends/implements clause

//title for method is its name, args and throws clause

//head for class is its variables static and instance

//head for method is its variables

//head for try block is try {

//tail for try block is catch (Exception e) {...}

