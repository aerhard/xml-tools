package com.aerhard.xml.tools;

import com.aerhard.xml.tools.util.ClientThread;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static com.aerhard.xml.tools.SchemaFactory.SCHEMA_PATH_SPLIT_REGEX;
import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class SuggesterTest {

  private final static String testJsonDirPath;
  private final static String testJsonFilePath;

  private static final String host;
  private static final int port = 9002;
  private static final Thread serverThread;

  private final String description;
  private final String xmlFilePath;
  private final String catalogFilePath;
  private final String suggestionType;
  private final String fragment;
  private final Integer splitPoint;
  private final String[] schemata;
  private final TestCase item;

  static {
    Properties properties = new Properties();

    try {
      properties.load(SuggesterTest.class.getResourceAsStream("/test.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    String testDataPath = properties.getProperty("testDataPath");
    testJsonDirPath = testDataPath + "/autocomplete/json";
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

  @JsonIgnoreProperties(ignoreUnknown=true)
  public static class FirstLevelTestGroup {
    public String catalog;
    public SecondLevelTestGroup[] items;
  }

  public static class SecondLevelTestGroup {
    public SchemaReference[] schemata;
    public TestCase[] items;
  }

  public static class TestCase {
    public String file;
    public ExpectedResult[] expectResult;
    public String suggestionType;
    public String fragment;
    public Integer splitPoint;
  }

  @JsonIgnoreProperties(ignoreUnknown=true)
  public static class ExpectedResult {
    public String displayText;
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

    FirstLevelTestGroup[] testsWithCatalogs = readFromFile(testJsonFilePath);

    for (FirstLevelTestGroup firstLevelTestGroup : testsWithCatalogs) {
      for (SecondLevelTestGroup secondLevelTestGroup : firstLevelTestGroup.items) {
        for (TestCase item : secondLevelTestGroup.items) {
          StringBuilder sb = new StringBuilder();

          sb.append("given schema \"");

          SchemaReference[] schemaReferences = secondLevelTestGroup.schemata;
          int len = schemaReferences.length;
          String[] schemaFiles = new String[len];
          for (int i = 0; i < len; i++) {
            String schemaPath = schemaReferences[i].path;
            if (schemaPath == null) {
              schemaFiles[i] = "none";
            } else {
              String[] tokens = schemaPath.split("/");
              schemaFiles[i] = tokens[tokens.length - 1];
            }
          }

          sb.append(StringUtils.join(schemaFiles, ", "));
          sb.append("\", it suggests [");

          ExpectedResult[] expectedResults = item.expectResult;
          len = expectedResults.length;
          String[] displayTexts = new String[len];
          for (int i = 0; i < len; i++) {
            displayTexts[i] = expectedResults[i].displayText;
          }

          sb.append(StringUtils.join(displayTexts, ", "));

          sb.append("] when requesting type \"");
          sb.append(item.suggestionType);
          sb.append("\" autocomplete in file \"");
          String[] tokens = item.file.split("/");
          sb.append(tokens[tokens.length - 1]);
          sb.append("\" ");
          if (item.fragment != null) {
            sb.append("at fragment ");
            sb.append(item.fragment);
          }

          String description = sb.toString();

          String xmlFilePath = testJsonDirPath + "/" + item.file;
          String catalogFilePath = testJsonDirPath + "/" + firstLevelTestGroup.catalog;

          String suggestionType = item.suggestionType;
          String fragment = item.fragment;
          Integer splitPoint = item.splitPoint;

          String[] schemata = new String[schemaReferences.length];
          for (int i = 0, j = schemaReferences.length; i < j; i++) {
            SchemaReference ref = schemaReferences[i];
            String path;
            if (ref.path == null) {
              path = "";
            } else {
              String[] subpaths = ref.path.trim().split(SCHEMA_PATH_SPLIT_REGEX);

              for (int l = 0; l < subpaths.length; l++) {
                if (subpaths[l].startsWith("../")) {
                  subpaths[l] = testJsonDirPath + "/" + subpaths[l];
                }
              }
              path = StringUtils.join(subpaths, "*");
            }

            schemata[i] = ref.lang + " " + path;
          }

          data.add(new Object[] { description, xmlFilePath, catalogFilePath, schemata, item, suggestionType, fragment, splitPoint });
        }
      }
    }

    return data;
  }

  public SuggesterTest(String description, String xmlFilePath, String catalogFilePath,
                       String[] schemata, TestCase item, String suggestionType, String fragment,
                       Integer splitPoint) {
    this.description = description;
    this.xmlFilePath = xmlFilePath;
    this.catalogFilePath = catalogFilePath;
    this.schemata = schemata;
    this.item = item;
    this.suggestionType = suggestionType;
    this.fragment = fragment;
    this.splitPoint = splitPoint;
  }

  @Test
  public void run() throws IOException, InterruptedException {
    List<String> result = new ArrayList<String>();
    ClientThread.suggestSync(host, port, xmlFilePath, catalogFilePath, schemata, suggestionType, fragment, splitPoint, result);

    String str = StringUtils.join(result.toArray(), "");
    JSONArray arr = new JSONArray(str);

    int adjustment = "E".equals(suggestionType) ? -2 : 0;

    assertEquals(new Long(item.expectResult.length  + adjustment), new Long(arr.length()));
  }
}
