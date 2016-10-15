package com.aerhard.xml.tools.util;

import java.io.*;
import java.net.Socket;
import java.util.List;

import static com.aerhard.xml.tools.Constants.COMMAND_AUTO_COMPLETE;
import static com.aerhard.xml.tools.Constants.COMMAND_VALIDATE;

public class ClientThread extends Thread {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  private final String host;
  private final int port;
  private String catalogPath;
  private final String[] schemaInfos;
  private final String command;
  private final String suggestionType;
  private final String fragment;
  private List<String> result;
  private final String xmlPath;

  public static void validateSync(String host, int port, String xmlPath, String catalogPath,
                                  String[] schemaInfos, List<String> result) {
    Thread t = new ClientThread(host, port, xmlPath, catalogPath, schemaInfos, COMMAND_VALIDATE, result);
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void suggestSync(String host, int port, String xmlPath, String catalogPath,
                                 String[] schemaInfos, String suggestionType, String fragment, List<String> result) {
    Thread t = new ClientThread(host, port, xmlPath, catalogPath, schemaInfos, COMMAND_AUTO_COMPLETE, suggestionType, fragment, result);
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public ClientThread(final String host, final int port, final String xmlPath, final String catalogPath,
                      final String[] schemaInfos, String command, List<String> result) {
    this(host, port, xmlPath, catalogPath, schemaInfos, command, null, null, result);
  }

  public ClientThread(final String host, final int port, final String xmlPath, final String catalogPath,
                      final String[] schemaInfos, String command, String suggestionType, String fragment, List<String> result) {
    this.host = host;
    this.port = port;
    this.xmlPath = xmlPath;
    this.catalogPath = catalogPath;
    this.schemaInfos = schemaInfos;
    this.command = command;
    this.suggestionType = suggestionType;
    this.fragment = fragment;
    this.result = result;
  }

  public void run() {
    try {

      socket = new Socket(host, port);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      OutputStream outputStream = socket.getOutputStream();

      String encoding = "UTF-8";

      out = new PrintWriter(new OutputStreamWriter(outputStream));

      if (suggestionType == null) {
        out.format("-%s\n-r\n-%s\n-%s\n-%s\n", command, encoding, xmlPath, catalogPath);
      } else {
        out.format("-%s\n-%s\n-%s\n-rwn\n-%s\n-%s\n-%s\n", command, suggestionType, fragment == null ? "" : fragment, encoding, xmlPath, catalogPath);
      }

      for (String schemaInfo: schemaInfos) {
        out.println("-" + schemaInfo);
      }
      out.println();

      File file = new File(xmlPath);
      FileReader fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        out.append(line);
        out.append("\n");
      }
      fileReader.close();

      out.flush();
      socket.shutdownOutput();

      String inLine;
      while (true) {
        inLine = in.readLine();
        if (inLine == null) {
          break;
        } else {
          result.add(inLine);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}