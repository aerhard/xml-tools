package com.aerhard.xml.tools.error;

import com.aerhard.xml.tools.error.ErrorPrintHandler;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class ErrorListenerAdapter implements ErrorListener {
  private final String schemaPath;
  private final ErrorPrintHandler eh;
  private boolean hadErrorOrFatalError = false;

  public boolean getHadErrorOrFatalError() {
    return hadErrorOrFatalError;
  }

  public ErrorListenerAdapter(String schemaPath, ErrorPrintHandler eh) {
    this.schemaPath = schemaPath;
    this.eh = eh;
  }

  @Override
    public void warning(TransformerException e) throws TransformerException {
      eh.print(schemaPath + ": warning: " + e.getMessage());
    }

    @Override
    public void error(TransformerException e) throws TransformerException {
      hadErrorOrFatalError = true;
      eh.print(schemaPath + ": error: " + e.getMessage());
    }

    @Override
    public void fatalError(TransformerException e) throws TransformerException {
      hadErrorOrFatalError = true;
      eh.print(schemaPath + ": fatal: " + e.getMessage());
      throw e;
    }
}
