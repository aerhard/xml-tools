package com.aerhard.xml.tools;

import com.aerhard.xml.tools.error.ErrorPrintHandler;
import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.suggest.schemaless.impl.SuggesterImpl;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SuggesterThread extends Thread {

  private final SchemaProperties schemaProperties;
  private final ErrorPrintHandler eh;
  private byte[] bytes;
  private final String xmlPath;
  private JSONArray suggestions = null;

  public SuggesterThread(SchemaProperties schemaProperties, ErrorPrintHandler eh, byte[] bytes, String xmlPath) {
    this.schemaProperties = schemaProperties;
    this.eh = eh;
    this.bytes = bytes;
    this.xmlPath = xmlPath;
  }

  public JSONArray getSuggestions() {
    return suggestions;
  }

  @Override
  public void run() {
    Driver driver = null;

    if (Constants.SCHEMA_TYPE_RNG.equals(schemaProperties.getType()) ||
        Constants.SCHEMA_TYPE_RNC.equals(schemaProperties.getType()) ||
        Constants.SCHEMA_TYPE_XSD.equals(schemaProperties.getType())) {
      driver = getValidationDriver();
    }

    if (driver != null) {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      try {
        InputSource is = new InputSource(bais);
        is.setEncoding(schemaProperties.getRequestProperties().getEncoding());
        is.setSystemId(xmlPath);
        RequestProperties requestProperties = schemaProperties.getRequestProperties();
        suggestions = driver.runSuggester(is, eh, requestProperties.getSuggestionType(), requestProperties.getFragment());
      } catch (SAXException e) {
        suggestions = new JSONArray();
      } catch (NullPointerException e) {
        suggestions = new JSONArray();
      } finally {
        bytes = null;
      }
    } else if (Constants.SUGGESTION_TYPE_ELEMENT.equals(schemaProperties.getRequestProperties().getSuggestionType())) {
      suggestClosingTag();
    } else {
      suggestions = new JSONArray();
    }
  }

  private void suggestClosingTag() {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    SuggesterImpl suggester = new SuggesterImpl();

    try {
      Sax2XMLReaderCreator xrc = new Sax2XMLReaderCreator();
      XMLReader reader = xrc.createXMLReader();
      reader.setFeature("http://xml.org/sax/features/namespaces", true);
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
      reader.setFeature("http://xml.org/sax/features/validation", false);
      reader.setFeature("http://apache.org/xml/features/validation/schema", false);
      reader.setErrorHandler(eh);
      reader.setContentHandler(suggester);

      InputSource is = new InputSource(bais);

      is.setSystemId(xmlPath);
      is.setEncoding(schemaProperties.getRequestProperties().getEncoding());
      reader.parse(is);

    } catch (FileNotFoundException e) {
    } catch (SAXException e) {
    } catch (IOException e) {
    } finally {
      bytes = null;
    }

    suggestions = new JSONArray();

    String qName = suggester.suggestClosingTag();
    if (qName != null) {
      JSONObject suggestion = new JSONObject();
      suggestion.put("value", qName);
      suggestion.put("closing", true);
      suggestions.put(suggestion);
    }
  }

  private Driver getValidationDriver() {
    Driver driver = DriverCache.drivers.get(schemaProperties.hashCode());
    if (driver == null) {
      PropertyMap properties = createPropertyMap(schemaProperties, eh);
      driver = createValidationDriver(properties, schemaProperties);

      if (driver != null) {
        DriverCache.drivers.put(schemaProperties.hashCode(), driver);
      }
    }

    return driver;
  }

  private Driver createValidationDriver(PropertyMap properties, SchemaProperties schemaProperties) {
    Schema schema = SchemaFactory.createSchema(properties, schemaProperties);

    return (schema == null)
        ? null
        : new Driver(schema, properties);
  }

  private PropertyMap createPropertyMap(SchemaProperties schemaProperties, ErrorPrintHandler eh) {
    PropertyMapBuilder properties = new PropertyMapBuilder();

    String catalogUri = schemaProperties.getRequestProperties().getCatalogUri();
    if (catalogUri != null) {
      List<String> catalogUris = new ArrayList<String>();
      catalogUris.add(catalogUri);
      CatalogResolver resolver = new CatalogResolver(catalogUris);
      properties.put(ValidateProperty.RESOLVER, resolver);
    }

    properties.put(ValidateProperty.ERROR_HANDLER, eh);
    RngProperty.CHECK_ID_IDREF.add(properties);

    return properties.toPropertyMap();
  }
}
