package com.aerhard.xml.tools.error;

/**
 * based on com.thaiopensource.xml.sax.SilentErrorPrintHandler
 */

import com.aerhard.xml.tools.error.ErrorPrintHandler;
import com.thaiopensource.util.UriOrFile;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class AccumulatingErrorPrintHandler implements ErrorPrintHandler {

  private final Set<String> messages = new HashSet<String>();

  private ResourceBundle bundle = null;
  private final String schemaType;

  public AccumulatingErrorPrintHandler(String schemaType) {
    this.schemaType = schemaType;
  }

  private String getString(String key) {
    String bundleName = "com.thaiopensource.xml.sax.resources.Messages";
    if (bundle == null)
      bundle = ResourceBundle.getBundle(bundleName);
    return bundle.getString(key);
  }

  public Set<String> getMessages() {
    return messages;
  }

  private String format(String key, Object[] args) {
    return MessageFormat.format(getString(key), args);
  }

  @Override
  public void warning(SAXParseException e) throws SAXParseException {
    print(format("warning",
        new Object[]{formatMessage(e), formatSaxParseLocation(e)}));
  }

  @Override
  public void error(SAXParseException e) {
    print(format("error",
        new Object[]{formatMessage(e), formatSaxParseLocation(e)}));
  }

  @Override
  public void fatalError(SAXParseException e) throws SAXParseException {
    throw e;
  }

  @Override
  public void printException(Throwable e) {
    String loc;
    if (e instanceof SAXParseException)
      loc = formatSaxParseLocation((SAXParseException) e);
    else
      loc = "";
    String message;
    if (e instanceof SAXException)
      message = formatMessage((SAXException) e);
    else
      message = formatMessage(e);
    print(format("fatal", new Object[]{message, loc}));
  }

  @Override
  public synchronized void print(String message) {
    if (message.length() != 0) {
      messages.add(schemaType + ":" + message);
    }
  }

  private String formatSaxParseLocation(SAXParseException e) {
    String systemId = e.getSystemId();
    int n = e.getLineNumber();
    Integer lineNumber = n >= 0 ? n : null;
    n = e.getColumnNumber();
    Integer columnNumber = n >= 0 ? n : null;
    return formatLocation(systemId, lineNumber, columnNumber);
  }

  private String formatLocation(String systemId, Integer lineNumber, Integer columnNumber) {
    if (systemId != null) {
      systemId = UriOrFile.uriToUriOrFile(systemId);
      if (lineNumber != null) {
        if (columnNumber != null)
          return format("locator_system_id_line_number_column_number",
              new Object[]{systemId, lineNumber, columnNumber});
        else
          return format("locator_system_id_line_number",
              new Object[]{systemId, lineNumber});
      } else
        return format("locator_system_id",
            new Object[]{systemId});
    } else if (lineNumber != null) {
      if (columnNumber != null)
        return format("locator_line_number_column_number",
            new Object[]{lineNumber, columnNumber});
      else
        return format("locator_line_number",
            new Object[]{lineNumber});
    } else
      return "";
  }

  private String formatMessage(SAXException se) {
    Exception e = se.getException();
    String detail = se.getMessage();
    if (e != null) {
      String detail2 = e.getMessage();
      // Crimson stupidity
      if (detail2 == detail || e.getClass().getName().equals(detail))
        return formatMessage(e);
      else if (detail2 == null)
        return format("exception",
            new Object[]{e.getClass().getName(), detail});
      else
        return format("tunnel_exception",
            new Object[]{e.getClass().getName(),
                detail,
                detail2});
    } else {
      if (detail == null)
        detail = getString("no_detail");
      return detail;
    }
  }

  private String formatMessage(Throwable e) {
    String detail = e.getMessage();
    if (detail == null)
      detail = getString("no_detail");
    if (e instanceof FileNotFoundException)
      return format("file_not_found", new Object[]{detail});
    return format("exception",
        new Object[]{e.getClass().getName(), detail});
  }
}
