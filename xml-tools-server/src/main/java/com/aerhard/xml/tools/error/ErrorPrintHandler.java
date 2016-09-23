package com.aerhard.xml.tools.error;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public interface ErrorPrintHandler extends ErrorHandler {
  void warning(SAXParseException e) throws SAXParseException;

  void error(SAXParseException e);

  void fatalError(SAXParseException e) throws SAXParseException;

  void printException(Throwable e);

  void print(String message);
}
