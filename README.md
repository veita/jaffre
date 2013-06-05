jaffre - Java Fat Free Remoting
===============================

## Introduction

Jaffre is a lightweight RPC library for the Java platform.
It is designed to be simple, extensible, robust, and efficient.
Currently it supports transport over insecure or TLS encrypted
TCP channels. It supports sessions and can be customized so that
calls are performed as a particular session dependent subject.

## Usage

Implementing a service is as simple as defining a service interface,
implementing it in a pojo, and registering that pojo as a service
endpoint. The service is exposed to clients through one ore more
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

## Dependencies

Jaffre does not have any required dependencies.
