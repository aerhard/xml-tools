package com.thaiopensource.suggest.relaxng.impl;

import com.thaiopensource.suggest.relaxng.pattern.IdTypeMap;
import com.thaiopensource.suggest.relaxng.pattern.SchemaBuilderImpl;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.StringSplitter;
import org.relaxng.datatype.Datatype;
import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class IdSuggester implements ContentHandler {
  private final IdTypeMap idTypeMap;
  private final ErrorHandler eh;

  private final Set ids = new HashSet<String>();

  public IdSuggester(IdTypeMap idTypeMap, ErrorHandler eh) {
    this.idTypeMap = idTypeMap;
    this.eh = eh;
  }

  @Override
  public void setDocumentLocator(Locator locator) {}

  @Override
  public void startDocument() throws SAXException {}

  @Override
  public void endDocument() throws SAXException {}

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {}

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {}

  @Override
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    Name name = new Name(namespaceURI, localName);

    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));
      String attValue = atts.getValue(i);

      maybeAddId(name, attName, attValue);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {}

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {}

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

  @Override
  public void processingInstruction(String target, String data) throws SAXException {}

  @Override
  public void skippedEntity(String name) throws SAXException {}

  public void reset() {
    ids.clear();
  }

  public Set<String> getIds(byte[] bytes) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

    try {
      Sax2XMLReaderCreator xrc = new Sax2XMLReaderCreator();
      XMLReader reader = xrc.createXMLReader();
      reader.setFeature("http://xml.org/sax/features/namespaces", true);
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
      reader.setFeature("http://xml.org/sax/features/validation", false);
      reader.setFeature("http://apache.org/xml/features/validation/schema", false);
      reader.setErrorHandler(eh);
      reader.setContentHandler(this);

      InputSource is = new InputSource(bais);

      reader.parse(is);

    } catch (FileNotFoundException e) {
    } catch (SAXException e) {
    } catch (IOException e) {
    } finally {
      bytes = null;
      return ids;
    }
  }

  public void maybeAddId(Name elementName, Name attributeName, String value)
      throws SAXException {
    int idType = idTypeMap.getIdType(elementName, attributeName);
    if (idType == Datatype.ID_TYPE_ID && value != null && !value.isEmpty()) {
      ids.add(value);
    }
  }

  public int getIdType(Name elementName, Name attributeName) {
    return idTypeMap.getIdType(elementName, attributeName);
  }
}
