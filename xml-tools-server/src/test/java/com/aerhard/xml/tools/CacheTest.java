package com.aerhard.xml.tools;

import com.aerhard.xml.tools.util.ClientThread;
import org.junit.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class CacheTest {

  private static final String host;
  private static final int port = 9003;
  private static final Thread serverThread;

  private static final String testDataPath;

  static {
    Properties properties = new Properties();

    try {
      properties.load(CacheTest.class.getResourceAsStream("/test.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    testDataPath = properties.getProperty("testDataPath");

    String localhost;
    try {
      localhost = InetAddress.getByName(null).getHostAddress();
    } catch (UnknownHostException e) {
      localhost = null;
      e.printStackTrace();
    }
    host = localhost;

    serverThread = new Thread() {
      @Override
      public void run() {
        SocketServer server = new SocketServer();
        try {
          server.start(port, 15);
        } catch (IOException e) {
          e.printStackTrace();
        }
        System.out.println("CLIENT: Server ends");
      }
    };
    serverThread.start();
  }

  @Test
  public void run() {

    String xmlFilePath = testDataPath + "/validation/xml/rng-invalid.xml";
    String catalogFilePath = testDataPath + "/validation/catalog/catalog.xml";
    String[] schemata = new String[] {
        Constants.SCHEMA_TYPE_RNG +  " " + testDataPath + "/validation/schema/schema.rng",
    };

    List<String> results = new ArrayList<String>();
    List<String> result;

    result = new ArrayList<String>();
    ClientThread.validateSync(host, port, xmlFilePath, catalogFilePath, schemata, result);
    results.addAll(result);

    result = new ArrayList<String>();
    ClientThread.validateSync(host, port, xmlFilePath, catalogFilePath, schemata, result);
    results.addAll(result);

    result = new ArrayList<String>();
    ClientThread.validateSync(host, port, xmlFilePath, catalogFilePath, schemata, result);
    results.addAll(result);

    serverThread.interrupt();

    assertEquals(15, results.size());
  }
}
