package com.aerhard.xml.tools.error;

import com.aerhard.xml.tools.error.ErrorPrintHandler;
import org.xml.sax.SAXParseException;

public class SilentErrorPrintHandler implements ErrorPrintHandler {
  @Override
  public void warning(SAXParseException exception) {}

  @Override
  public void error(SAXParseException exception) {}

  @Override
  public void fatalError(SAXParseException exception) {}

  @Override
  public void printException(Throwable e) {}

  @Override
  public void print(String message) {}
}
