package com.aerhard.xml.tools;

import com.aerhard.xml.tools.error.SilentErrorPrintHandler;
import com.aerhard.xml.tools.error.ErrorPrintHandler;
import com.aerhard.xml.tools.error.AccumulatingErrorPrintHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXParseException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import static com.aerhard.xml.tools.Constants.*;

class RequestHandlerThread extends Thread {

  private final Socket socket;

  public RequestHandlerThread(Socket socket) {
    this.socket = socket;
  }

  public void run() {
    InputStream is = null;

    try {
      is = socket.getInputStream();
      List<String> headerLines = readHeaders(is);

      final String command = headerLines.get(0);
      headerLines.remove(0);

      if (COMMAND_CLEAR_CACHE.equals(command)) {
        handleClearCacheCommand();
      } else if (COMMAND_SET_MAX_CACHE_SIZE.equals(command)) {
        handleMaxCacheSizeCommand(headerLines);
      } else if (COMMAND_VALIDATE.equals(command)) {
        handleValidationCommand(is, headerLines);
      } else if (COMMAND_AUTO_COMPLETE.equals(command)) {
        handleSuggestionCommand(is, headerLines);
      }
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      try {
        if (is != null) {
          waitForEndOfInput(is);
        }
      } catch (IOException e) { }
      try {
        socket.close();
      } catch (IOException e) {}
    }
  }

  private void handleClearCacheCommand() {
    DriverCache.clear();
  }

  private void handleMaxCacheSizeCommand(List<String> headerLines) {
    final String maxDriverCacheSizeString = headerLines.get(0);
    int maxDriverCacheSizeInt = Integer.parseInt(maxDriverCacheSizeString);
    DriverCache.setMaxSize(maxDriverCacheSizeInt);
  }


  private synchronized void handleSuggestionCommand(InputStream is, List<String> headerLines)
      throws IOException, InterruptedException {
    JSONArray suggestions = getSuggestions(is, headerLines);
    if (suggestions != null) {
      writeSuggestionsToStream(suggestions, socket.getOutputStream());
    }

    DriverCache.adjustSize();
  }

  private JSONArray getSuggestions(InputStream is, List<String> headerLines) throws IOException, InterruptedException {
    int headerIndex = 0;
    final String suggestionType = headerLines.get(headerIndex++);
    final String fragment = headerLines.get(headerIndex++);
    final String splitPointString = headerLines.get(headerIndex++);
    final String options = headerLines.get(headerIndex++);
    final String encoding = headerLines.get(headerIndex++);
    final String xmlPath = headerLines.get(headerIndex++);
    final String catalog = headerLines.get(headerIndex++);
    final RequestProperties requestProperties = new RequestProperties(catalog, options, encoding, suggestionType, fragment);

    byte[] bytes = toByteArray(is);
    byte[] head;

    if (splitPointString.isEmpty()) {
      head = bytes;
    } else {
      int splitPoint = Integer.parseInt(splitPointString);
      head = Arrays.copyOfRange(bytes, 0, splitPoint);
    }

    String schemaLine = headerLines.get(headerIndex);
    SchemaProperties schemaProperties = new SchemaProperties(schemaLine, requestProperties);

    ErrorPrintHandler eh = new SilentErrorPrintHandler();

    SuggesterThread t = new SuggesterThread(schemaProperties, eh, head, bytes, xmlPath);
    t.start();
    t.join();

    JSONArray suggestions = t.getSuggestions();

//    for (Object obj : suggestions) {
//      JSONObject suggestion = (JSONObject) obj;
//      System.out.println(suggestion.get("value"));
//    }

    bytes = null;
    head = null;

    return suggestions;
  }

  private synchronized void handleValidationCommand(InputStream is, List<String> headerLines)
      throws IOException, InterruptedException {
    Set<String> messages = validate(is, headerLines);
    writeMessagesToStream(messages, socket.getOutputStream());

    DriverCache.adjustSize();
  }


  private Set<String> validate(InputStream is, List<String> headerLines) throws IOException, InterruptedException {
    int headerIndex = 0;
    final String options = headerLines.get(headerIndex++);
    final String encoding = headerLines.get(headerIndex++);
    final String xmlPath = headerLines.get(headerIndex++);
    final String catalog = headerLines.get(headerIndex++);
    final RequestProperties requestProperties = new RequestProperties(catalog, options, encoding);

    byte[] bytes = toByteArray(is);

    if (bytes.length == 0) {
      AccumulatingErrorPrintHandler eh = new AccumulatingErrorPrintHandler(SCHEMA_TYPE_NONE);
      eh.printException(new SAXParseException("Premature end of file", null, xmlPath, 0, 0));
      return eh.getMessages();
    }

    Set<AccumulatingErrorPrintHandler> ehs = new HashSet<AccumulatingErrorPrintHandler>();
    Set<Thread> threads = new HashSet<Thread>();
    for (; headerIndex < headerLines.size(); headerIndex++) {
      String schemaLine = headerLines.get(headerIndex);
      SchemaProperties schemaProperties = new SchemaProperties(schemaLine, requestProperties);

      AccumulatingErrorPrintHandler veh = new AccumulatingErrorPrintHandler(schemaProperties.getType());
      ehs.add(veh);

      AccumulatingErrorPrintHandler reh = new AccumulatingErrorPrintHandler(SCHEMA_TYPE_NONE);
      ehs.add(reh);

      Thread t = new ValidationThread(schemaProperties, veh, reh, bytes, xmlPath);
      threads.add(t);
      t.start();
    }

    for (Thread t : threads) {
      t.join();
    }

    Set<String> messages = new HashSet<String>();
    for (AccumulatingErrorPrintHandler eh : ehs) {
      messages.addAll(eh.getMessages());
    }

//    for (String message : messages) {
//      System.out.println(message);
//    }

    bytes = null;

    return messages;
  }

  private void writeSuggestionsToStream(JSONArray suggestions, OutputStream sos) {
    PrintWriter pw = new PrintWriter(sos, true);
    pw.print(suggestions.toString());
    pw.close();
  }

  private void writeMessagesToStream(Collection<String> messages, OutputStream sos) {
    PrintWriter pw = new PrintWriter(sos, true);
    for (String message : messages) {
      pw.println(message);
    }
    pw.close();
  }

  private void waitForEndOfInput(InputStream is) throws IOException {
    byte[] data = new byte[1024];
    while(is.read(data) != -1) {}
  }

  private byte[] toByteArray(InputStream is) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[1024];
    while ((nRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    return buffer.toByteArray();
  }

  private List<String> readHeaders(InputStream inputStream) throws IOException {
    int intRead;
    List<String> headerLines = new ArrayList<String>();
    StringBuilder sb = new StringBuilder();
    while ((intRead = inputStream.read()) != -1) {
      char charRead = (char) intRead;
      if (charRead != '\n') {
        sb.append(charRead);
      } else {
        if (sb.length() == 0) break;
        sb.deleteCharAt(0);
        headerLines.add(sb.toString());
        sb.setLength(0);
      }
    }

    return headerLines;
  }
}
