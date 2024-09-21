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

  # curl "http://localhost:8081" -D -
  
    HTTP/1.1 200 OK
    Content-Length: 74
    Content-Type: text/plain
    
    Your headers are:
    Accept: */*
    User-Agent:     curl/8.4.0
    Host:   localhost:8081

# curl "http://localhost:8081/person" -D -

    HTTP/1.1 400 Bad Request
    Content-Length: 41
    Content-Type: text/plain
    
    Invalid request: Missing 'name' parameter

# curl -X PUT  --data-raw "Filip's Data" "http://localhost:8081/person?name=Filip" -D -

    HTTP/1.1 201 Created
    Content-Length: 0
    Content-Type: text/plain
      

  

      
