package com.aerhard.xml.tools;

import java.io.*;
import java.net.*;

public class SocketServer {

  public void start(int port, int maxDriverCacheSize) throws IOException {
    DriverCache.setMaxSize(maxDriverCacheSize);
    ServerSocket socketServer = null;
    try {
      socketServer = new ServerSocket(port, 0, InetAddress.getByName(null));
      port = socketServer.getLocalPort();
      System.out.println("XML Tools Server listening on port " + port);
    } catch (IOException e) {
      System.err.println("Could not listen on port " + port);
      System.exit(1);
    }

    try {
      while (!Thread.currentThread().isInterrupted()) {
        Socket socket = socketServer.accept();
        new RequestHandlerThread(socket).start();
      }
    } catch (InterruptedIOException e) {
      System.out.println("Exiting...");
    } finally {
      try {
        socketServer.close();
      } catch (IOException e) {}
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Expected 2 arguments: port and maxDriverCacheSize");
      System.exit(1);
    }

    int port = new Integer(args[0]);
    int maxDriverCacheSize = new Integer(args[1]);

    new SocketServer().start(port, maxDriverCacheSize);
  }
}
