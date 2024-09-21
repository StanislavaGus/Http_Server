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

  ### curl "http://localhost:8081" -D -
  
    HTTP/1.1 200 OK
    Content-Length: 74
    Content-Type: text/plain
    
    Your headers are:
    Accept: */*
    User-Agent:     curl/8.4.0
    Host:   localhost:8081

### curl "http://localhost:8081/person" -D -

    HTTP/1.1 400 Bad Request
    Content-Length: 41
    Content-Type: text/plain
    
    Invalid request: Missing 'name' parameter

### curl -X PUT  --data-raw "Filip's Data" "http://localhost:8081/person?name=Filip" -D -

    HTTP/1.1 201 Created
    Content-Length: 0
    Content-Type: text/plain


# Description

The Main class initializes custom endpoints and starts the server on port 8081 in a separate thread. 

After the server starts, it waits for requests from clients. After the client has contacted the server, it establishes a connection with it and processes its requests. The project implements one custom endpoint - person, its handler can be found in the Main class. After the server has accepted the request, it processes it in the HttpRequest class and sends the data to the HttpResponse class in which the response is sent to the client. 

You can see examples of requests and responses above.

      

  

      
