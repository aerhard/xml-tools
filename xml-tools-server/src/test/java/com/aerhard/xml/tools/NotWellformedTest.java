package com.aerhard.xml.tools;

import com.aerhard.xml.tools.util.ClientThread;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class NotWellformedTest {

  @Test
  public void run() throws IOException, InterruptedException {
    Properties properties = new Properties();

    try {
      properties.load(ValidationTest.class.getResourceAsStream("/test.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    String testDataPath = properties.getProperty("testDataPath");

    String host = InetAddress.getByName(null).getHostAddress();
    final int port = 9004;

    Thread serverThread = new Thread() {
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

    String emptyXmlFilePath = testDataPath + "/validation/xml/empty.xml";
    String notWellformedXmlFilePath = testDataPath + "/validation/xml/notwellformed.xml";

    String catalogFilePath = testDataPath + "/validation/catalog/catalog.xml";
    String[] schemata = new String[] { Constants.SCHEMA_TYPE_NONE + " " };

    List<String> results = new  ArrayList<String>();
    List<String> result;

    for (int i = 0; i < 4; i++) {
      String xmlFilePath = i % 2 == 0 ? emptyXmlFilePath : notWellformedXmlFilePath;
      result = new ArrayList<String>();
      ClientThread.validateSync(host, port, xmlFilePath, catalogFilePath, schemata, result);
      results.addAll(result);
    }

    serverThread.interrupt();

    assertEquals(4, results.size());
  }
}
