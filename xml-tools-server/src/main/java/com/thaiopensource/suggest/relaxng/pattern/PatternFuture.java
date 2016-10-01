package com.thaiopensource.suggest.relaxng.pattern;

import org.xml.sax.SAXException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;

import java.io.IOException;

public interface PatternFuture {
  com.thaiopensource.suggest.relaxng.pattern.Pattern getPattern(boolean isAttributesPattern) throws IllegalSchemaException, SAXException, IOException;
}
