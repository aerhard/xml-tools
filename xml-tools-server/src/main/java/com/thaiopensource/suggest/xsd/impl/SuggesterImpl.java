package com.thaiopensource.suggest.xsd.impl;

import com.thaiopensource.suggest.*;
import com.thaiopensource.suggest.xsd.xerces.XmlSchemaValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.xs.*;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.*;
import org.apache.xerces.util.*;
import org.apache.xerces.xni.*;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.*;
import org.apache.xerces.xs.*;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.*;

import static org.apache.xerces.xs.XSAnnotation.SAX_CONTENTHANDLER;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.*;

public class SuggesterImpl extends ParserConfigurationSettings implements Suggester, XMLLocator, XMLEntityResolver, EntityState {

  private final XmlSchemaValidator schemaValidator = new XmlSchemaValidator();
  private final ValidationManager validationManager = new ValidationManager();
  private final NamespaceContext namespaceContext = new NamespaceSupport();

  private Attributes originalAttributes = new AttributesImpl();

  private final XMLAttributes attributes = new XMLAttributesImpl();
  private final SymbolTable symbolTable;
  private final XSModel model;
  private final XMLComponent[] components;
  private Locator locator;
  private final Set<String> entities = new HashSet<String>();
  private boolean pushedContext = false;

  // XXX deal with baseURI

  static private final String[] recognizedFeatures = {
      com.thaiopensource.suggest.xsd.impl.Features.SCHEMA_AUGMENT_PSVI,
      com.thaiopensource.suggest.xsd.impl.Features.SCHEMA_FULL_CHECKING,
      com.thaiopensource.suggest.xsd.impl.Features.VALIDATION,
      com.thaiopensource.suggest.xsd.impl.Features.SCHEMA_VALIDATION,
  };

  static private final String[] recognizedProperties = {
      com.thaiopensource.suggest.xsd.impl.Properties.XMLGRAMMAR_POOL,
      com.thaiopensource.suggest.xsd.impl.Properties.SYMBOL_TABLE,
      com.thaiopensource.suggest.xsd.impl.Properties.ERROR_REPORTER,
      com.thaiopensource.suggest.xsd.impl.Properties.ERROR_HANDLER,
      com.thaiopensource.suggest.xsd.impl.Properties.VALIDATION_MANAGER,
      com.thaiopensource.suggest.xsd.impl.Properties.ENTITY_MANAGER,
      com.thaiopensource.suggest.xsd.impl.Properties.ENTITY_RESOLVER,
  };
  private Stack<String> qNames = new Stack<String>();

  SuggesterImpl(SymbolTable symbolTable, XMLGrammarPool grammarPool, XSModel model, PropertyMap properties) {
    this.symbolTable = symbolTable;
    this.model = model;

    XMLErrorHandler errorHandlerWrapper = new ErrorHandlerWrapper(properties.get(ValidateProperty.ERROR_HANDLER));
    XMLEntityManager entityManager = new XMLEntityManager();
    XMLErrorReporter errorReporter = new XMLErrorReporter();
    components = new XMLComponent[]{errorReporter, schemaValidator, entityManager};
    for (XMLComponent component : components) {
      addRecognizedFeatures(component.getRecognizedFeatures());
      addRecognizedProperties(component.getRecognizedProperties());
    }

    addRecognizedFeatures(recognizedFeatures);
    addRecognizedProperties(recognizedProperties);
    setFeature(com.thaiopensource.suggest.xsd.impl.Features.SCHEMA_AUGMENT_PSVI, true);
    setFeature(com.thaiopensource.suggest.xsd.impl.Features.SCHEMA_FULL_CHECKING, true);
    setFeature(com.thaiopensource.suggest.xsd.impl.Features.VALIDATION, true);
    setFeature(com.thaiopensource.suggest.xsd.impl.Features.SCHEMA_VALIDATION, true);
    setFeature(com.thaiopensource.suggest.xsd.impl.Features.ID_IDREF_CHECKING, true);
    setFeature(com.thaiopensource.suggest.xsd.impl.Features.IDC_CHECKING, true);
    setProperty(com.thaiopensource.suggest.xsd.impl.Properties.XMLGRAMMAR_POOL, grammarPool);
    setProperty(com.thaiopensource.suggest.xsd.impl.Properties.SYMBOL_TABLE, symbolTable);
    errorReporter.setDocumentLocator(this);
    setProperty(com.thaiopensource.suggest.xsd.impl.Properties.ERROR_REPORTER, errorReporter);
    setProperty(com.thaiopensource.suggest.xsd.impl.Properties.ERROR_HANDLER, errorHandlerWrapper);
    setProperty(com.thaiopensource.suggest.xsd.impl.Properties.VALIDATION_MANAGER, validationManager);
    setProperty(com.thaiopensource.suggest.xsd.impl.Properties.ENTITY_MANAGER, entityManager);
    setProperty(com.thaiopensource.suggest.xsd.impl.Properties.ENTITY_RESOLVER, this);
    reset();
  }

  public void reset() {
    validationManager.reset();
    namespaceContext.reset();
    for (XMLComponent component : components) component.reset(this);
    validationManager.setEntityState(this);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
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
      schemaValidator.startDocument(locator == null ? null : this, null, namespaceContext, null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endDocument()
      throws SAXException {
    try {
      schemaValidator.endDocument(null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts)
      throws SAXException {
    originalAttributes = atts;
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
      schemaValidator.startElement(makeQName(namespaceURI, localName, qName), attributes, null);
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
      schemaValidator.endElement(makeQName(namespaceURI, localName, qName), null);
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
      schemaValidator.characters(new XMLString(ch, start, length), null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void ignorableWhitespace(char ch[], int start, int length)
      throws SAXException {
    try {
      schemaValidator.ignorableWhitespace(new XMLString(ch, start, length), null);
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

  @Override
  public String suggestClosingTag() {
    return qNames.size() > 0 ? qNames.peek() : null;
  }

  @Override
  public List<ElementSuggestion> suggestElements(boolean suggestWildcards, boolean suggestNamespaceWildcard) {

    List<ElementSuggestion> suggestions = new ArrayList<ElementSuggestion>();

    if (schemaValidator.getSkipValidationDepth() >= 0) {
      return suggestions;
    }

    AnnotationSerializer annotationSerializer = new AnnotationSerializer();

    Set<Object> expectedEls = new HashSet<Object>();

    if (schemaValidator.getElementDepth() == -1 && schemaValidator.getCurrentCM() == null) {

      if (model != null) {
        XSNamedMap elementDeclarations = model.getComponents(XSConstants.ELEMENT_DECLARATION);
        expectedEls.addAll(elementDeclarations.values());
      }

    } else if (schemaValidator.getElementDepth() != -1) {
      if (schemaValidator.getCurrentType() instanceof XSComplexTypeDecl) {
        XSComplexTypeDecl ctype = (XSComplexTypeDecl) schemaValidator.getCurrentType();
        Set<Object> next;
        if (ctype.getParticle() != null
            && (next = new HashSet<Object>(schemaValidator.getCurrentCM().whatCanGoHere(schemaValidator.getCurrCMState()))).size() > 0) {
          expectedEls = next;
        }
      }
    }

    if (expectedEls.size() > 0) {
      Map<String, String> elementNsPrefixMap = getElementNsPrefixMap(namespaceContext);
      Map<String, String> attributeNsPrefixMap = getAttributeNsPrefixMap(namespaceContext);

      Set<String> nsUris = new HashSet<String>();

      Set<Object> wildcardComponents = new HashSet<Object>();

      for (Object obj : expectedEls) {
        if (obj instanceof XSWildcardDecl) {
          XSWildcardDecl wildcardDecl = (XSWildcardDecl) obj;

          addWildcardComponents(wildcardDecl, XSConstants.ELEMENT_DECLARATION, wildcardComponents, nsUris,
              elementNsPrefixMap, suggestWildcards, suggestNamespaceWildcard);
        }
      }

      expectedEls.addAll(wildcardComponents);

      Set<Object> substitutions = new HashSet<Object>();

      for (Object obj : expectedEls) {
        if (obj instanceof XSElementDecl) {
          XSElementDecl current = (XSElementDecl) obj;
          XSObjectList subGroup = model.getSubstitutionGroup(current);
          if (subGroup != null) {
            substitutions.addAll(subGroup);
          }
        }
      }

      expectedEls.addAll(substitutions);

      for (Object obj : expectedEls) {
        if (obj instanceof XSElementDecl) {
          XSElementDecl elDecl = (XSElementDecl) obj;

          if (!elDecl.getAbstract()) {
            String value = createNameValue(elDecl.getName(), elDecl.getNamespace(), elementNsPrefixMap);

            // emptiness expressed as SimpleType with enum "" is not taken into account; include?
            // see http://docstore.mik.ua/orelly/xml/schema/ch07_06.htm

            XSTypeDefinition type = elDecl.getTypeDefinition();
            boolean isEmpty = type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE &&
                ((XSComplexTypeDefinition) type).getContentType() == XSComplexTypeDefinition.CONTENTTYPE_EMPTY;

            List<String> attributes = null;
            XSAttributeGroupDecl attrGrp = getAttributeGroup(elDecl);
            if (attrGrp != null) {
              Set<String> attributesSet = getRequiredAttributes(attrGrp, attributeNsPrefixMap);
              if (!attributesSet.isEmpty()) {
                attributes = new ArrayList<String>();
                attributes.addAll(attributesSet);
              }
            }

            List<String> annotations = getAnnotations(annotationSerializer, elDecl.getAnnotation());

            suggestions.add(new ElementSuggestion(value, annotations, attributes, isEmpty, false));
          }
        }
      }

      for (String nsUri : nsUris) {
        String value = createNameValue("*", nsUri, elementNsPrefixMap);
        suggestions.add(new ElementSuggestion(value, null, null, false, false));
      }
    }

    return suggestions;
  }

  private String createNameValue(String localName, String nsUri, Map<String, String> nsPrefixMap) {
    String prefix = nsPrefixMap.get(nsUri);

    // when the nsUri maps to nothing it needs to get included in the result
    if (prefix == null) {
      return nsUri == null
        ? localName + "#"
        : localName + '#' + nsUri;
    }

    // a prefix of "" means it matches the default namespace => return only localName
    if ("".equals(prefix)) return localName;

    return prefix + ":" + localName;
  }

  private List<String> getAnnotations(AnnotationSerializer annotationSerializer, XSAnnotation annot) {
    List<String> annotations = null;
    if (annot != null) {
      annot.writeAnnotation(annotationSerializer, SAX_CONTENTHANDLER);
      annotations = annotationSerializer.getAnnotationStrings();
      annotationSerializer.reset();
    }
    return annotations;
  }

  @Override
  public List<AttributeNameSuggestion> suggestAttributeNames(boolean suggestWildcards, boolean suggestNamespaceWildcard) {
    List<AttributeNameSuggestion> suggestions = new ArrayList<AttributeNameSuggestion>();

    XSElementDeclaration elDecl = schemaValidator.getCurrentPSVIElementDecl();
    if (elDecl != null) {
      Map<String, String> attributeNsPrefixMap = getAttributeNsPrefixMap(namespaceContext);

      if (elDecl.getNillable()) {
        String value = createNameValue("nil", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attributeNsPrefixMap);
        suggestions.add(new AttributeNameSuggestion(value, null));
      }

      XSAttributeGroupDecl attrGrp = getAttributeGroup(elDecl);
      if (attrGrp != null) {
        Set<Object> attrDecls = new HashSet<Object>();
        Set<String> nsUris = new HashSet<String>();

        attrDecls.addAll(getUseAttrDecls(attrGrp, true));

        XSWildcardDecl wildcardDecl = (XSWildcardDecl) attrGrp.getAttributeWildcard();
        if (wildcardDecl != null) {
          addWildcardComponents(wildcardDecl, XSConstants.ATTRIBUTE_DECLARATION, attrDecls, nsUris,
              attributeNsPrefixMap, suggestWildcards, suggestNamespaceWildcard);
        }

        AnnotationSerializer annotationSerializer = new AnnotationSerializer();

        for (Object obj : attrDecls) {
          if (obj instanceof XSAttributeDecl) {
            XSAttributeDecl attrDecl = (XSAttributeDecl) obj;
            String nsUri = attrDecl.getNamespace() == null ? "" : attrDecl.getNamespace();

            if (originalAttributes.getIndex(nsUri, attrDecl.getName()) == -1) {
              String value = createNameValue(attrDecl.getName(),
                  attrDecl.getNamespace(), attributeNsPrefixMap);
              List<String> annotations = getAnnotations(annotationSerializer, attrDecl.getAnnotation());
              suggestions.add(new AttributeNameSuggestion(value, annotations));
            }
          }
        }

        for (String nsUri : nsUris) {
          String value = createNameValue("*", nsUri, attributeNsPrefixMap);
          suggestions.add(new AttributeNameSuggestion(value, null));
        }
      }
    }

    return suggestions;
  }

  @Override
  public List<AttributeValueSuggestion> suggestAttributeValues(String fragment, byte[] bytes) {
    String[] tokens = fragment.split(" ", 2);
    String attrQName = tokens[0];
//    String currentValue = tokens.length > 1 ? tokens[1] : null;

    List<AttributeValueSuggestion> suggestions = new ArrayList<AttributeValueSuggestion>();

    XSElementDeclaration elDecl = schemaValidator.getCurrentPSVIElementDecl();
    if (elDecl != null) {
      XSAttributeGroupDecl attrGrp = getAttributeGroup(elDecl);
      if (attrGrp != null) {
        Map<String, String> attributeNsPrefixMap = getAttributeNsPrefixMap(namespaceContext);

        XSObjectList attrUses = attrGrp.getAttributeUses();
        XSAttributeUseImpl currUse = null;
        XSAttributeDecl currDecl = null;
        boolean foundMatch = false;

        int len = attrUses.getLength();
        for (int i = 0; i < len; i++) {
          currUse = (XSAttributeUseImpl) attrUses.item(i);
          currDecl = currUse.fAttrDecl;

          String value = createNameValue(currDecl.getName(),
              currDecl.getNamespace(), attributeNsPrefixMap);
          if (value.equals(attrQName)) {
            foundMatch = true;
            break;
          }
        }

        if (foundMatch && currDecl != null) {
          if (currUse.getConstraintType() == XSConstants.VC_FIXED) {
            String value = currUse.getConstraintValue();
            suggestions.add(new AttributeValueSuggestion(value, null, false));
          } else if (currDecl.getTypeDefinition() instanceof XSSimpleTypeDecl) {
            XSSimpleTypeDecl type = (XSSimpleTypeDecl) currDecl.getTypeDefinition();

            AnnotationSerializer annotationSerializer = new AnnotationSerializer();

            for (Object[] objs : getValueSuggestions(type)) {
              try {
                type.validate(objs[0], null, null);

                String value = objs[0].toString();
                List<String> annotations = null;

                if (objs[1] != null) {
                  annotations = getAnnotations(annotationSerializer, (XSAnnotation) objs[1]);
                }
                suggestions.add(new AttributeValueSuggestion(value, annotations, (Boolean) objs[2]));

              } catch (InvalidDatatypeValueException e) {
              }

            }
          }
        }
      }
    }

    return suggestions;
  }

  private Set<Object[]> getValueSuggestions(XSSimpleTypeDecl type) {
    Set<Object[]> suggestions = new HashSet<Object[]>();

    boolean isList = type.getVariety() == VARIETY_LIST;

    if (isList && type.getItemType() instanceof XSSimpleTypeDecl) {
      type = (XSSimpleTypeDecl) type.getItemType();
    }

    boolean isUnion = type.getVariety() == VARIETY_UNION;

    if (isUnion) {
      XSObjectList memberTypes = type.getMemberTypes();
      int memberLen = memberTypes.getLength();
      for (int i = 0; i<memberLen;i++) {
        Object obj = memberTypes.get(i);
        if (obj instanceof XSSimpleTypeDecl) {
          XSSimpleTypeDecl memberType = (XSSimpleTypeDecl) obj;
          suggestions.addAll(getValueSuggestions(memberType));
        }
      }
    }

    XSObjectList tl = type.enumerationAnnotations;

    ObjectList enumeration = type.getActualEnumeration();
    int valueLen = enumeration.getLength();
    for (int j = 0; j < valueLen; j++) {
      Object suggestion = enumeration.item(j);
      Object annot;
      if (tl != null && (annot = tl.item(j)) != null) {
        suggestions.add(new Object[] { suggestion, annot, false });
      } else {
        suggestions.add(new Object[] { suggestion, null, false });
      }
    }

    if (isList) {
      for (Object[] suggestion : suggestions) {
        suggestion[2] = true;
      }
    }

    return suggestions;
  }

  private Map<String, String> getElementNsPrefixMap(NamespaceContext namespaceContext) {
    Map<String, String> nsPrefixMap = new HashMap<String, String>();

    Enumeration prefixes = namespaceContext.getAllPrefixes();
    while (prefixes.hasMoreElements()) {
      String prefix = (String) prefixes.nextElement();
      String nsUri = namespaceContext.getURI(prefix);
      nsPrefixMap.put(nsUri, prefix);
    }

    // when there's no default namespace, map null to ""
    if (!nsPrefixMap.containsValue("")) {
      nsPrefixMap.put(null, "");
    }

    return nsPrefixMap;
  }

  private Map<String, String> getAttributeNsPrefixMap(NamespaceContext namespaceContext) {
    Map<String, String> nsPrefixMap = new HashMap<String, String>();

    Enumeration prefixes = namespaceContext.getAllPrefixes();
    while (prefixes.hasMoreElements()) {
      String prefix = (String) prefixes.nextElement();

      // default NS needs to get skipped in attribute prefix map
      if (!"".equals(prefix)) {
        String nsUri = namespaceContext.getURI(prefix);
        nsPrefixMap.put(nsUri, prefix);
      }
    }

    // add xml NS mapping
    nsPrefixMap.put(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);

    // add attribute default NS mapping
    nsPrefixMap.put(null, "");

    return nsPrefixMap;
  }

  private XSAttributeGroupDecl getAttributeGroup(XSElementDeclaration elDecl) {
    XSTypeDefinition type = elDecl.getTypeDefinition();
    if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
      XSComplexTypeDecl ctype = (XSComplexTypeDecl) type;
      return ctype.getAttrGrp();
    }
    return null;
  }

  private Set<String> getRequiredAttributes(XSAttributeGroupDecl attrGrp,
                                            Map<String, String> nsPrefixMap) {

    Set<XSAttributeDecl> attrDecls = getUseAttrDecls(attrGrp, false);

    Set<String> attrStrings = new HashSet<String>();

    for (XSAttributeDecl attrDecl : attrDecls) {
      String str = createNameValue(attrDecl.getName(),
          attrDecl.getNamespace(), nsPrefixMap);
      attrStrings.add(str);
    }

    return attrStrings;
  }

  private void addWildcardComponents(XSWildcardDecl wildcardDecl, short componentType,
                                     Set<Object> expectedComponents, Set<String> nsUris,
                                     Map<String, String> nsPrefixMap, boolean suggestWildcards,
                                     boolean suggestNamespaceWildcard) {
    SchemaGrammar[] grammars = schemaValidator.getGrammarBucket().getGrammars();

    boolean isStrict = wildcardDecl.getProcessContents() == XSWildcard.PC_STRICT;


    if (wildcardDecl.getConstraintType() == XSWildcard.NSCONSTRAINT_LIST) {
      // only the listed nsUris...

      StringList nsConstraintList = wildcardDecl.getNsConstraintList();
      int len = nsConstraintList.getLength();

      for (int i = 0; i < len; i++) {
        String nsUri = (String) nsConstraintList.get(i);
        XSNamedMap components = model.getComponentsByNamespace(componentType, nsUri);
        expectedComponents.addAll(components.values());
        if (suggestWildcards) {
          nsUris.add(nsUri);
        }
      }

    } else if (wildcardDecl.getConstraintType() == XSWildcard.NSCONSTRAINT_NOT) {
      // all namespaces but...

      StringList excludedNamespaces = wildcardDecl.getNsConstraintList();

      for (SchemaGrammar grammar : grammars) {
        String grammarNamespace = grammar.getTargetNamespace();
        if (!excludedNamespaces.contains(grammarNamespace)) {
          XSNamedMap components = model.getComponentsByNamespace(componentType, grammarNamespace);
          expectedComponents.addAll(components.values());
          if (suggestWildcards) {
            nsUris.add(grammarNamespace);
          }
        }
      }

      if (suggestWildcards && !isStrict) {
        if (suggestNamespaceWildcard) {
          nsUris.add("*");
        }
        Set<String> prefixMapNsUris = new HashSet<String>(nsPrefixMap.keySet());
        if (componentType == XSConstants.ELEMENT_DECLARATION) {
          prefixMapNsUris.remove(XMLConstants.XML_NS_URI);
        }
        for (String nsUri : prefixMapNsUris) {
          if (!excludedNamespaces.contains(nsUri)) {
            nsUris.add(nsUri);
          }
        }
      }
    } else {
      // no namespace constraints...

      XSNamedMap components = model.getComponents(componentType);
      Collection elDecl = components.values();
      expectedComponents.addAll(elDecl);

      if (suggestWildcards) {
        for (SchemaGrammar grammar : grammars) {
          String grammarNamespace = grammar.getTargetNamespace();
          nsUris.add(grammarNamespace);
        }

        if (componentType == XSConstants.ATTRIBUTE_DECLARATION) {
          nsUris.add(null);
        }

        if (!isStrict) {
          if (suggestNamespaceWildcard) {
            nsUris.add("*");
          }
          Set<String> prefixMapNsUris = new HashSet<String>(nsPrefixMap.keySet());
          if (componentType == XSConstants.ELEMENT_DECLARATION) {
            prefixMapNsUris.remove(XMLConstants.XML_NS_URI);
          }
          for (String nsUri : prefixMapNsUris) {
            nsUris.add(nsUri);
          }
        }
      }
    }
  }

  private Set<XSAttributeDecl> getUseAttrDecls(XSAttributeGroupDecl attrGrp, boolean allAttributes) {
    XSObjectList attrUses = attrGrp.getAttributeUses();
    XSAttributeUseImpl currUse;
    XSAttributeDecl currDecl;

    Set<XSAttributeDecl> attrDecls = new HashSet<XSAttributeDecl>();

    int len = attrUses.getLength();
    for (int i = 0; i < len; i++) {
      currUse = (XSAttributeUseImpl) attrUses.item(i);
      currDecl = currUse.fAttrDecl;

      if (allAttributes || currUse.getRequired()) {
        attrDecls.add(currDecl);
      }
    }

    return attrDecls;
  }
}