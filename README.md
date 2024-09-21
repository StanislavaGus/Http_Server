# Task:
Implement part of HTTP 1.1 protocol using ServerSocketChannel (java.nio)

Methods:

    GET
    POST
    PUT
    PATCH
    Delete

Headers (should be accesible as Map)

Library should support:

Create and httpserver on specified host+port

Add listener to specific path and method

Access to request parameters (headers, method, etc)

Create and send http response back


# Compiling and Building:

## for Windows:
        
      javac Main.java
      java Main

## for Linux:

      mvn package
      mvn exec:java -Dexec.mainClass="Main"

# Test curl request

  ## for Windows:
  

  

      
