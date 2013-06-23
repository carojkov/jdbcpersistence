REM "%JAVA_HOME%\bin\javadoc.exe" -public -splitindex -author -classpath .\lib\asm.jar;.\lib\mysql.jar;.\lib\ojdbc14.jar -sourcepath .\src\java;.\src\test -d .\javadoc -noindex org.jdbcpersistence
"%JAVA_HOME%\bin\javadoc.exe" -public -noindex -author -classpath .\lib\asm.jar;.\lib\mysql.jar;.\lib\ojdbc14.jar -sourcepath .\src\java;.\src\test -d .\javadoc org.jdbcpersistence

pause 0
exit