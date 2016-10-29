package com.thaiopensource.suggest.xsd.impl;

import com.thaiopensource.suggest.xsd.xerces.XmlIdValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.ValidateProperty;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.util.*;
import org.apache.xerces.xni.*;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.*;
import org.apache.xerces.xs.*;
import org.xml.sax.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class IdSuggester extends ParserConfigurationSettings implements ContentHandler, DTDHandler, XMLLocator, XMLEntityResolver, EntityState {

  private final XmlIdValidator xmlIdValidator = new XmlIdValidator();
  private final ValidationManager validationManager = new ValidationManager();
  private final NamespaceContext namespaceContext = new NamespaceSupport();
  private final PropertyMap properties;
  private final ErrorHandler eh;

  private final XMLAttributes attributes = new XMLAttributesImpl();
  private final SymbolTable symbolTable;
  private final XMLComponent[] components;
  private Locator locator;
  private final Set<String> entities = new HashSet<String>();
  private boolean pushedContext = false;

  private Set ids = new HashSet<String>();

  // XXX deal with baseURI

  static private final String[] recognizedFeatures = {
      Features.SCHEMA_AUGMENT_PSVI,
      Features.SCHEMA_FULL_CHECKING,
      Features.VALIDATION,
      Features.SCHEMA_VALIDATION,
  };

  static private final String[] recognizedProperties = {
      Properties.XMLGRAMMAR_POOL,
      Properties.SYMBOL_TABLE,
      Properties.ERROR_REPORTER,
      Properties.ERROR_HANDLER,
      Properties.VALIDATION_MANAGER,
      Properties.ENTITY_MANAGER,
      Properties.ENTITY_RESOLVER,
  };
  private Stack<String> qNames = new Stack<String>();

  IdSuggester(SymbolTable symbolTable, XMLGrammarPool grammarPool, XSModel model, PropertyMap properties) {
    this.symbolTable = symbolTable;
    this.properties = properties;

    eh = properties.get(ValidateProperty.ERROR_HANDLER);
    XMLErrorHandler errorHandlerWrapper = new ErrorHandlerWrapper(eh);
    XMLEntityManager entityManager = new XMLEntityManager();
    XMLErrorReporter errorReporter = new XMLErrorReporter();
    components = new XMLComponent[]{errorReporter, xmlIdValidator, entityManager};
    for (XMLComponent component : components) {
      addRecognizedFeatures(component.getRecognizedFeatures());
      addRecognizedProperties(component.getRecognizedProperties());
    }

    addRecognizedFeatures(recognizedFeatures);
    addRecognizedProperties(recognizedProperties);
    setFeature(Features.SCHEMA_AUGMENT_PSVI, true);
    setFeature(Features.SCHEMA_FULL_CHECKING, true);
    setFeature(Features.VALIDATION, true);
    setFeature(Features.SCHEMA_VALIDATION, true);
    setFeature(Features.ID_IDREF_CHECKING, true);
    setFeature(Features.IDC_CHECKING, true);
    setProperty(Properties.XMLGRAMMAR_POOL, grammarPool);
    setProperty(Properties.SYMBOL_TABLE, symbolTable);
    errorReporter.setDocumentLocator(this);
    setProperty(Properties.ERROR_REPORTER, errorReporter);
    setProperty(Properties.ERROR_HANDLER, errorHandlerWrapper);
    setProperty(Properties.VALIDATION_MANAGER, validationManager);
    setProperty(Properties.ENTITY_MANAGER, entityManager);
    setProperty(Properties.ENTITY_RESOLVER, this);
    reset();
  }

  public void parse(byte[] bytes) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

    XMLReader xr;

    try {
      xr = ResolverFactory.createResolver(properties).createXMLReader();

      xr.setErrorHandler(eh);
      xr.setContentHandler(this);
      xr.setDTDHandler(this);

      InputSource is = new InputSource(bais);
      xr.parse(is);

      ids = xmlIdValidator.getIds();
    } catch (IOException e) {
    } catch (SAXException e) {
    } catch (Exception e) {
    } finally {
      bytes = null;
    }
  }

  public Set<String> getIds() {
    return ids;
  }

  public void reset() {
    validationManager.reset();
    namespaceContext.reset();
    for (XMLComponent component : components) component.reset(this);
    validationManager.setEntityState(this);
    ids.clear();
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId) {
    // nothing needed
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName) {
    entities.add(name);
  }

  public boolean isEntityDeclared(String name) {
    return entities.contains(name);
  }

  public boolean isEntityUnparsed(String name) {
    return entities.contains(name);
  }

  public void startDocument()
      throws SAXException {
    try {
      xmlIdValidator.startDocument(locator == null ? null : this, null, namespaceContext, null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endDocument()
      throws SAXException {
    try {
      xmlIdValidator.endDocument(null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts)
      throws SAXException {
    qNames.push(qName);

    try {
      if (!pushedContext)
        namespaceContext.pushContext();
      else
        pushedContext = false;
      for (int i = 0, len = atts.getLength(); i < len; i++) {
        attributes.addAttribute(makeQName(atts.getURI(i), atts.getLocalName(i), atts.getQName(i)),
            symbolTable.addSymbol(atts.getType(i)),
            atts.getValue(i));
      }
      xmlIdValidator.startElement(makeQName(namespaceURI, localName, qName), attributes, null);
      attributes.removeAllAttributes();
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endElement(String namespaceURI, String localName,
                         String qName)
      throws SAXException {
    qNames.pop();
    try {
      xmlIdValidator.endElement(makeQName(namespaceURI, localName, qName), null);
      namespaceContext.popContext();
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void startPrefixMapping(String prefix, String uri)
      throws SAXException {
    try {
      if (!pushedContext) {
        namespaceContext.pushContext();
        pushedContext = true;
      }
      if (prefix == null)
        prefix = XMLSymbols.EMPTY_STRING;
      else
        prefix = symbolTable.addSymbol(prefix);
      if (uri != null) {
        if (uri.equals(""))
          uri = null;
        else
          uri = symbolTable.addSymbol(uri);
      }
      namespaceContext.declarePrefix(prefix, uri);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endPrefixMapping(String prefix)
      throws SAXException {
    // do nothing
  }

  public void characters(char ch[], int start, int length)
      throws SAXException {
    try {
      xmlIdValidator.characters(new XMLString(ch, start, length), null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void ignorableWhitespace(char ch[], int start, int length)
      throws SAXException {
    try {
      xmlIdValidator.ignorableWhitespace(new XMLString(ch, start, length), null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void processingInstruction(String target, String data)
      throws SAXException {
    // do nothing
  }

  public void skippedEntity(String name)
      throws SAXException {
    // do nothing
  }

  private QName makeQName(String namespaceURI, String localName, String qName) {
    localName = symbolTable.addSymbol(localName);
    String prefix;
    if (namespaceURI.equals("")) {
      namespaceURI = null;
      prefix = XMLSymbols.EMPTY_STRING;
      qName = localName;
    } else {
      namespaceURI = symbolTable.addSymbol(namespaceURI);
      if (qName.equals("")) {
        prefix = namespaceContext.getPrefix(namespaceURI);
        if (prefix == XMLSymbols.EMPTY_STRING)
          qName = localName;
        else if (prefix == null)
          qName = localName; // XXX what to do?
        else
          qName = symbolTable.addSymbol(prefix + ":" + localName);
      } else {
        qName = symbolTable.addSymbol(qName);
        int colon = qName.indexOf(':');
        if (colon > 0)
          prefix = symbolTable.addSymbol(qName.substring(0, colon));
        else
          prefix = XMLSymbols.EMPTY_STRING;
      }
    }
    return new QName(prefix, localName, qName, namespaceURI);
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
      throws XNIException, IOException {
    return null;
  }

  public String getPublicId() {
    return locator.getPublicId();
  }

  public String getEncoding() {
    return null;
  }

  public String getBaseSystemId() {
    return null;
  }

  public String getLiteralSystemId() {
    return null;
  }

  public String getExpandedSystemId() {
    return locator.getSystemId();
  }

  public int getLineNumber() {
    return locator.getLineNumber();
  }

  public int getColumnNumber() {
    return locator.getColumnNumber();
  }

  public int getCharacterOffset() {
    return -1;
  }

  public String getXMLVersion() {
    return "1.0";
  }

  static SAXException toSAXException(XNIException e) {
    if (e instanceof XMLParseException) {
      XMLParseException pe = (XMLParseException) e;
      return new SAXParseException(pe.getMessage(),
          pe.getPublicId(),
          pe.getExpandedSystemId(),
          pe.getLineNumber(),
          pe.getColumnNumber(),
          pe.getException());
    }
    Exception nested = e.getException();
    if (nested == null)
      return new SAXException(e.getMessage());
    if (nested instanceof SAXException)
      return (SAXException) nested;
    if (nested instanceof RuntimeException)
      throw (RuntimeException) nested;
    return new SAXException(nested);
  }

}