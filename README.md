jdbcpersistence
===============

Java Fast ORM

   JDBCPersistence framework for java                                                 
   Copyright (C) 2004-2014 Alex Rojkov                                                
                                                                                      
   This library is free software; you can redistribute it and/or                      
   modify it under the terms of the GNU Lesser General Public                         
   License as published by the Free Software Foundation; either                       
   version 2.1 of the License, or (at your option) any later version.                 
                                                                                      
   This library is distributed in the hope that it will be useful,                    
   but WITHOUT ANY WARRANTY; without even the implied warranty of                     
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU                  
   Lesser General Public License for more details.                                    
                                                                                      
   You should have received a copy of the GNU Lesser General Public                   
   License along with this library; if not, write to the Free Software                
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA          
                                                                                      
   You can contact me by email jdbcpersistence   a t   gmail    d o t    com

            JDBCPersistence distribution

JDBCPersistence is an Object Relational Mapping Framework. The differentiating features
of JDBCPersistence are a result of the focus placed on making programmer most effective
by building on existing knowledge of SQL, JDBC APIs, IDEs and, at the same time, avoiding
creation of dependencies on specific libraries, tools, IDEs.

The framework strikes a good balance in what it provides verses what it requires a
programmer to do in order to use it, e.g. a programmer could quickly prototype value
objects by specifying them as Java interfaces and asking the framework to provide
implementation for these interfaces at runtime, thus allowing to go from specifying
an interface to using it in no time.

On the other hand, recognizing that data relationships are governed by business
rules much more complex than could be specified in a typical ORM configuration file,
a programmer is required to maintain such relationships in code.

The focus of the framework is to provide ORM services while leaving other concerns,
in particular, data caching, up to the developer to solve in the context of an
application. Being aware that the caching requirements may be different in online
vs. batch application, the framework does not prescribe a solution. Developer needs
to decide on application specific caching policy and implement it in an architectural
layer (DAO) designed to function in both contexts.

The framework does not add any behavior to the classes that comprise the application.
Classes supplied by the developer are the classes that will be used by the application
when it runs.

JDBCPersistence uses bytecode generation technique to create bytecode for classes
that implement logic used for persisting the data. Such, for every, loosely
speaking "Java Bean", that requires persistence, a persistor class that implements
CRUD operations is created. As opposed to using reflection, the approach of generating
bytecode is apt to further optimization by JVM.

JDBCPersistence takes bytecode generation a little further by providing a feature
that allows generating complete implementation of a value object specified as an
abstract class. Similar to generating full implementation for Java interface class
that describes value object class, the framework can generate implementation for
all abstract methods of an abstract class representing value object.

As all of the bytecode generation takes place at run time, there is no impact on
development or build process. The approach of generating bytecode at runtime is also
used by RMI implementation of the Java Platform starting with version 5.0.

The API of the framework builds on the existing JDBC APIs. JDBCPersistence's public
API adds four classes and three interfaces. As the framework uses SQL for queering
the data a curve associated with learning framework specific query language is avoided.

In an attempt to "keep it simple" JDBCPersistence' only dependency is ASM,
which is used for bytecode generation.
 
The footprint of the package is less then 200K.
Please forward you feedback to jdbcpersistence   a t   gmail    d o t    com
