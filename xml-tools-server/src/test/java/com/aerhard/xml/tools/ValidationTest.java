package com.aerhard.xml.tools;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

import com.aerhard.xml.tools.util.ClientThread;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class ValidationTest {

  private final static String testJsonDirPath;
  private final static String testJsonFilePath;

  private static final String host;
  private static final int port = 9001;
  private static final Thread serverThread;

  private final String description;
  private String xmlFilePath;
  private String catalogFilePath;
  private String[] schemata;
  private TestCase item;

  static {
    Properties properties = new Properties();

    try {
      properties.load(ValidationTest.class.getResourceAsStream("/test.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    String testDataPath = properties.getProperty("testDataPath");
    testJsonDirPath = testDataPath + "/validation/json";
    testJsonFilePath = testJsonDirPath + "/main.json";

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

  public static class SecondLevelTestGroup {
    public String description;
    public SchemaReference[] schemata;
    public TestCase[] items;
  }

  public static class FirstLevelTestGroup {
    public String description;
    public String catalog;
    public SecondLevelTestGroup[] items;
  }

  @JsonIgnoreProperties(ignoreUnknown=true)
  public static class TestCase {
    public String condition;
    public String expectation;
    public String file;
    public Integer expectMessageLength;
  }

  public static class SchemaReference {
    public String lang;
    public String path;
  }

  public static FirstLevelTestGroup[] readFromFile(String filePath) {
    ObjectMapper mapper = new ObjectMapper();
    FirstLevelTestGroup[] FirstLevelTestGroups = null;
    try {
      FirstLevelTestGroups = mapper.readValue(new File(filePath), FirstLevelTestGroup[].class);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return FirstLevelTestGroups;
  }

  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() throws InterruptedException {
    Collection<Object[]> data = new ArrayList<Object[]>();

    FirstLevelTestGroup[] FirstLevelTestGroups = readFromFile(testJsonFilePath);

    for (FirstLevelTestGroup firstLevelTestGroup : FirstLevelTestGroups) {
      for (SecondLevelTestGroup secondLevelTestGroup : firstLevelTestGroup.items) {
        for (TestCase item : secondLevelTestGroup.items) {
          // unknown schema warning is not emitted from server and must get handled on the client
          if (!"given a well-formed xml document with an unknown schema type".equals(secondLevelTestGroup.description)) {

            String description = firstLevelTestGroup.description + " " + secondLevelTestGroup.description;
            if (item.condition != null) description += " " + item.condition;
            description += " " + item.expectation;

            String xmlFilePath = testJsonDirPath + "/" + item.file;
            String catalogFilePath = testJsonDirPath + "/" + firstLevelTestGroup.catalog;

            SchemaReference[] schemaReferences = secondLevelTestGroup.schemata;

            String[] schemata = new String[schemaReferences.length];
            for (int i = 0, j = schemaReferences.length; i < j; i++) {
              SchemaReference ref = schemaReferences[i];

              String path;
              if (ref.path == null) {
                path = "";
              } else {
                String[] subpaths = ref.path.trim().split("\\s+");

                for (int l = 0; l < subpaths.length; l++) {
                  if (subpaths[l].startsWith("../")) {
                    subpaths[l] = testJsonDirPath + "/" + subpaths[l];
                  }
                }
                path = StringUtils.join(subpaths, " ");
              }

              schemata[i] = ref.lang + " " + path;
            }

            data.add(new Object[]{description, xmlFilePath, catalogFilePath, schemata, item});
          }
        }
      }
    }

    return data;
  }

  public ValidationTest(String description, String xmlFilePath, String catalogFilePath,
                        String[] schemata, TestCase item) {
    this.description = description;
    this.xmlFilePath = xmlFilePath;
    this.catalogFilePath = catalogFilePath;
    this.schemata = schemata;
    this.item = item;
  }

  @Test
  public void run() throws IOException, InterruptedException {
    List<String> result = new ArrayList<String>();
    ClientThread.validateSync(host, port, xmlFilePath, catalogFilePath, schemata, result);

    assertEquals(new Long(item.expectMessageLength), new Long(result.size()));
  }
}
