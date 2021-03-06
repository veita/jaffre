jaffre - Java Fat Free Remoting
===============================

[![Build Status](https://travis-ci.org/veita/jaffre.svg?branch=master)](https://travis-ci.org/veita/jaffre)

## Introduction

Jaffre is a lightweight RPC library for the Java platform.
It is designed to be simple, extensible, robust, and efficient.
Currently it supports transport over insecure or TLS encrypted
TCP channels. It supports sessions and can be customized so that
calls are performed as a particular session dependent subject.

## Usage

Implementing a service is as simple as defining a service interface,
implementing it in a pojo, and registering that pojo as a service
endpoint. The service is exposed to clients through one or more
connectors. Clients call the service through proxy classes.

```Java
public interface Remote
{
  public String logAndEcho(String msg);
}

// Server
void runTheServer()
{
  // register a service endpoint
  final JaffreServer server = new DefaultJaffreServer();

  server.registerInterface(new RemoteImpl());

  // start the socket connector
  final SocketJaffreConnector connector = new SocketJaffreConnector();

  connector.setServer(server);
  connector.setBindingAddress("localhost");
  connector.setPort(4711);
  connector.setCoreThreadPoolSize(3);

  connector.start();

  synchronized (this)
  {
    wait(); // until we're done
  }

  connector.stop();
}

// Client
void runTheClient() throws Exception
{
  // create a socket client
  final SocketJaffreClient client = new SocketJaffreClient();

  client.setServiceAddress("localhost");
  client.setServicePort(4711);

  // create a proxy for the remote interface
  final Remote proxy = client.getProxy(Remote.class);

  proxy.logAndEcho("This is a remote call.");

  // done
  client.dispose();
}

```

For code examples see the [samples directory](https://github.com/veita/jaffre/tree/master/src/samples). The JavaDocs can be found [here](http://veita.github.io/jaffre/docs/api/).

## Security

Jaffre makes use of Java object serialization. For this reason it is susceptible to Java deserialization attacks. It should not be used in cases where a malicious user would be able to call the server endpoints over the network.

## Dependencies

Jaffre does not have any required dependencies.
