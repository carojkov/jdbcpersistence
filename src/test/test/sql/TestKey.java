package test.sql;

public class TestKey
{
  public Object[] keys;

  @Override
  public int hashCode()
  {
    int result = 17;

    for (Object key : keys) {
      result = 31 * result + key.hashCode();
    }

    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    TestKey testKey = (TestKey) obj;

    for (int i = 0; i < keys.length; i++) {
      Object key = keys[i];

      if (!key.equals(testKey.keys[i]))
        return false;
    }

    return true;
  }
}
