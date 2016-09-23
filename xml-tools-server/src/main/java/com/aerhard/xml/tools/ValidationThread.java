package com.aerhard.xml.tools;

import com.aerhard.xml.tools.error.ErrorPrintHandler;
import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class ValidationThread extends Thread {

  private final SchemaProperties schemaProperties;
  private final ErrorPrintHandler veh;
  private final ErrorPrintHandler reh;
  private byte[] bytes;
  private final String xmlPath;

  public ValidationThread(SchemaProperties schemaProperties, ErrorPrintHandler veh, ErrorPrintHandler reh, byte[] bytes, String xmlPath) {
    this.schemaProperties = schemaProperties;
    this.veh = veh;
    this.reh = reh;
    this.bytes = bytes;
    this.xmlPath = xmlPath;
  }

  @Override
  public void run() {
    String schemaPath = schemaProperties.getPath();

    if (Constants.SCHEMA_TYPE_DTD.equals(schemaProperties.getType()) || Constants.SCHEMA_TYPE_NONE.equals(schemaProperties.getType())) {
      validateWithInternalSchemata();
      return;
    }

    Driver driver = getValidationDriver();
    if (driver != null) {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      try {
        InputSource in = new InputSource(bais);
        in.setSystemId(xmlPath);
        driver.runValidator(in, veh, reh);
      } catch (IOException e) {
        reh.print(schemaPath + ": fatal: " + e.getMessage());
      } catch (SAXException e) {
        reh.printException(e);
      } finally {
        bytes = null;
      }
    }
  }

  private void validateWithInternalSchemata() {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

    try {
      boolean validateWithDTD = Constants.SCHEMA_TYPE_DTD.equals(schemaProperties.getType());

      PropertyMapBuilder properties = new PropertyMapBuilder(createPropertyMap(schemaProperties, reh));

      Sax2XMLReaderCreator xrc = new Sax2XMLReaderCreator();
      properties.put(ValidateProperty.XML_READER_CREATOR, xrc);

      XMLReader reader = ResolverFactory.createResolver(properties.toPropertyMap()).createXMLReader();
      reader.setFeature("http://xml.org/sax/features/namespaces", true);
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
      reader.setFeature("http://xml.org/sax/features/validation", validateWithDTD);
      reader.setFeature("http://apache.org/xml/features/validation/schema", false);
      reader.setErrorHandler(reh);

      InputSource xmlIn = new InputSource(bais);

      xmlIn.setSystemId(xmlPath);
      reader.parse(xmlIn);

    } catch (FileNotFoundException e) {
      reh.printException(e);
    } catch (SAXException e) {
      reh.printException(e);
    } catch (IOException e) {
      reh.print(schemaProperties.getPath() + ": fatal: " + e.getMessage());
    } finally {
      bytes = null;
    }
  }

  private Driver getValidationDriver() {
    Driver driver = DriverCache.drivers.get(schemaProperties.hashCode());
    if (driver == null) {
      PropertyMap properties = createPropertyMap(schemaProperties, veh);
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
