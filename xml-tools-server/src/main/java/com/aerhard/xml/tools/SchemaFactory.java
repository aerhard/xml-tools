package com.aerhard.xml.tools;

import com.thaiopensource.suggest.relaxng.CompactSchemaReader;
import com.thaiopensource.suggest.relaxng.SaxSchemaReader;
import com.aerhard.xml.tools.error.ErrorListenerAdapter;
import com.aerhard.xml.tools.error.ErrorPrintHandler;
import com.thaiopensource.suggest.xsd.impl.XsdSchemaReaderFactory;
import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.resolver.xml.ExternalIdentifier;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.validate.*;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

class SchemaFactory {
  public final static String SCHEMA_PATH_SPLIT_REGEX = "\\*";

  public static Schema createSchema(PropertyMap properties, SchemaProperties schemaProperties) {

    boolean resolveSchemaPath = schemaProperties.getRequestProperties().shouldResolveSchemaPath();
    String schemaPath = schemaProperties.getPath();

    ErrorPrintHandler eh = (ErrorPrintHandler) properties.get(ValidateProperty.ERROR_HANDLER);

    try {
      if (Constants.SCHEMA_TYPE_RNC.equals(schemaProperties.getType())) {
        InputSource inputSource = getInputSource(schemaPath, properties, resolveSchemaPath);
        return CompactSchemaReader.getInstance()
            .createSchema(new SAXSource(inputSource), properties);
      }
      if (Constants.SCHEMA_TYPE_RNG.equals(schemaProperties.getType())) {
        InputSource inputSource = getInputSource(schemaPath, properties, resolveSchemaPath);
        return SaxSchemaReader.getInstance()
            .createSchema(new SAXSource(inputSource), properties);
      }
      if (Constants.SCHEMA_TYPE_XSD.equals(schemaProperties.getType())) {
        Set<SAXSource> sources = new HashSet<SAXSource>();

        if (schemaPath != null) {
          String[] schemaPathTokens = schemaPath.trim().split(SCHEMA_PATH_SPLIT_REGEX);
          for (String schemaPathToken : schemaPathTokens) {
            InputSource inputSource = getInputSource(schemaPathToken, properties, resolveSchemaPath);
            SAXSource source = new SAXSource(inputSource);
            sources.add(source);
          }
        }

        return new XsdSchemaReaderFactory()
            .createSchemaReader(WellKnownNamespaces.XML_SCHEMA)
            .createSchema(sources, properties);
      }

      InputSource inputSource = getInputSource(schemaPath, properties, resolveSchemaPath);
      boolean transformationError = false;

      // TODO read schema content instead of extension!!!
      if (Constants.SCHEMA_TYPE_SCH_ISO.equals(schemaProperties.getType())) {
        if (schemaPath.endsWith(".rng")) {
          transformationError = !substituteInputStream(inputSource, eh, schemaPath, "ExtractSchFromRNG-2.xsl", properties);
        } else if (schemaPath.endsWith(".xsd")) {
          transformationError = !substituteInputStream(inputSource, eh, schemaPath, "ExtractSchFromXSD-2.xsl", properties);
        }
      } else if (Constants.SCHEMA_TYPE_SCH_15.equals(schemaProperties.getType())) {
        if (schemaPath.endsWith(".rng")) {
          transformationError = !substituteInputStream(inputSource, eh, schemaPath, "RNG2Schtrn.xsl", properties);
        } else if (schemaPath.endsWith(".xsd")) {
          transformationError = !substituteInputStream(inputSource, eh, schemaPath, "XSD2Schtrn.xsl", properties);
        }
      }

      if (!transformationError) {
        return new AutoSchemaReader()
            .createSchema(new SAXSource(inputSource), properties);
      }
    } catch (IncorrectSchemaException e) {
    } catch (SAXException e) {
      eh.printException(e);
    } catch (TransformerException e) {
      eh.print(schemaPath + ": fatal: " + e.getMessage());
    } catch (IOException e) {
      eh.print(schemaPath + ": fatal: " + e.getMessage());
    } catch (ResolverException e) {
      eh.print(schemaPath + ": fatal: " + e.getMessage());
    }

    return null;
  }

  private static InputSource getInputSource(String schemaPath, PropertyMap properties, boolean resolveSchemaPath) throws IOException, ResolverException {
    InputSource inputSource;
    if (schemaPath == null) {
      inputSource = null;
    } else {
      String resolvedSchemaURI = null;
      if (resolveSchemaPath && properties.contains(ValidateProperty.RESOLVER)) {
        CatalogResolver resolver = (CatalogResolver) properties.get(ValidateProperty.RESOLVER);
        String cwd = new File(".").getCanonicalPath();
        Identifier schemaIdentifier = new ExternalIdentifier(schemaPath, cwd, schemaPath);
        Input input = new Input();
        resolver.resolve(schemaIdentifier, input);
        resolvedSchemaURI = input.getUri();
      }

      inputSource = resolvedSchemaURI != null
          ? new InputSource(resolvedSchemaURI)
          : uriOrFileInputSource(schemaPath);
    }
    return inputSource;
  }

  private static boolean substituteInputStream(InputSource inputSource, final ErrorPrintHandler eh,
                                               String schemaPath, String xsltPath, PropertyMap properties)
      throws TransformerException, IOException, SAXException {
    InputStream xsltStream = SchemaFactory.class.getClassLoader().getResourceAsStream(xsltPath);
    ErrorListenerAdapter el = new ErrorListenerAdapter(schemaPath, eh);

    try {
      StreamSource xsltSource = new StreamSource(xsltStream);

      PropertyMapBuilder transformProperties = new PropertyMapBuilder(properties);
      Sax2XMLReaderCreator xrc = new Sax2XMLReaderCreator();
      transformProperties.put(ValidateProperty.XML_READER_CREATOR, xrc);

      XMLReader reader = ResolverFactory.createResolver(transformProperties.toPropertyMap()).createXMLReader();
      reader.setFeature("http://xml.org/sax/features/namespaces", true);
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
      reader.setFeature("http://xml.org/sax/features/validation", false);
      reader.setErrorHandler(eh);
      SAXSource xmlInput = new SAXSource(reader, inputSource);

      String systemId = inputSource.getSystemId();
      xsltSource.setSystemId(systemId);

      SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer(xsltSource);

      transformer.setErrorListener(el);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      transformer.transform(xmlInput, new StreamResult(baos));
      byte[] ba = baos.toByteArray();
      inputSource.setByteStream(new ByteArrayInputStream(ba));
      return !el.getHadErrorOrFatalError();
    } finally {
      xsltStream.close();
    }
  }

  private static InputSource uriOrFileInputSource(String uriOrFile) {
    return new InputSource(UriOrFile.toUri(uriOrFile));
  }

}
