package com.thaiopensource.suggest;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;

import java.util.Set;

public interface Suggester extends ContentHandler, DTDHandler {
  Set<ElementSuggestion> suggestElements(boolean suggestWildcards, boolean suggestNamespaceWildcard);
  String suggestClosingTag();
  Set<AttributeNameSuggestion> suggestAttributeNames(boolean suggestWildcards, boolean suggestNamespaceWildcard);
  Set<AttributeValueSuggestion> suggestAttributeValues(String fragment, byte[] bytes);
  void reset();
}
