package com.aerhard.xml.tools;

import com.aerhard.xml.tools.error.ErrorPrintHandler;
import com.thaiopensource.suggest.*;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Driver implements Comparable {
  private XMLReader xr;
  private final PropertyMap properties;
  private final Schema schema;
  private long lastActive = 0;

  public Driver(Schema schema, PropertyMap properties) {
    this.schema = schema;
    this.properties = properties;
  }

  synchronized private void parse(InputSource in, ErrorPrintHandler eh, ContentHandler ch, DTDHandler dh, String schemaPath) {
    try {
      if (xr == null) {
        xr = ResolverFactory.createResolver(properties).createXMLReader();
      }

      xr.setErrorHandler(eh);
      xr.setContentHandler(ch);
      if (dh != null) {
        xr.setDTDHandler(dh);
      }

      xr.parse(in);
    } catch (IOException e) {
      eh.print(schemaPath + ": fatal: " + e.getMessage());
    } catch (SAXException e) {
      eh.printException(e);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public JSONArray runSuggester(InputSource in, ErrorPrintHandler eh, String suggestionType,
                                String fragment, String schemaPath) {

    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    builder.put(ValidateProperty.ERROR_HANDLER, eh);
    PropertyMap instanceProperties = builder.toPropertyMap();

    SuggesterSchema suggesterSchema = (SuggesterSchema) schema;
    Suggester suggester = suggesterSchema.createSuggester(instanceProperties);

    parse(in, eh, suggester, suggester, schemaPath);

    JSONArray jsonData = new JSONArray();

    if (Constants.SUGGESTION_TYPE_ELEMENT.equals(suggestionType)) {
      List<ElementSuggestion> suggestions = suggester.suggestElements();
      Collections.sort(suggestions, new Comparator<Suggestion>() {
        @Override
        public int compare(Suggestion a, Suggestion b) {
          return a.getValue().compareTo(b.getValue());
        }
      });
      String closingTag = suggester.suggestClosingTag();
      if (closingTag != null) {
        suggestions.add(new ElementSuggestion(closingTag, null, null, false, true));
      }
      for (ElementSuggestion s : suggestions) {
        JSONObject item = new JSONObject();
        item.put("value", s.getValue());
        if (s.getDocumentation() != null) {
          item.put("documentation", new JSONArray(s.getDocumentation()));
        }
        if (s.getAttributes() != null) {
          item.put("attributes", new JSONArray(s.getAttributes()));
        }
        if (s.isEmpty()) {
          item.put("empty", true);
        }
        if (s.isClosing()) {
          item.put("closing", true);
        }
        jsonData.put(item);
      }
    }
    if (Constants.SUGGESTION_TYPE_ATT_NAME.equals(suggestionType)) {
      List<AttributeNameSuggestion> suggestions = suggester.suggestAttributeNames();
      Collections.sort(suggestions, new Comparator<Suggestion>() {
        @Override
        public int compare(Suggestion a, Suggestion b) {
          return a.getValue().compareTo(b.getValue());
        }
      });
      for (AttributeNameSuggestion s : suggestions) {
        JSONObject item = new JSONObject();
        item.put("value", s.getValue());
        if (s.getDocumentation() != null) {
          item.put("documentation", new JSONArray(s.getDocumentation()));
        }
        jsonData.put(item);
      }
    }
    if (Constants.SUGGESTION_TYPE_ATT_VALUE.equals(suggestionType)) {
      List<AttributeValueSuggestion> suggestions = suggester.suggestAttributeValues(fragment);
      Collections.sort(suggestions, new Comparator<Suggestion>() {
        @Override
        public int compare(Suggestion a, Suggestion b) {
          return a.getValue().compareTo(b.getValue());
        }
      });
      for (AttributeValueSuggestion s : suggestions) {
        JSONObject item = new JSONObject();
        item.put("value", s.getValue());
        if (s.getDocumentation() != null) {
          item.put("documentation", new JSONArray(s.getDocumentation()));
        }
        if (s.isListItem()) {
          item.put("listItem", true);
        }
        jsonData.put(item);
      }
    }

    lastActive = System.currentTimeMillis();
    suggester.reset();
    return jsonData;
  }

  public void runValidator(InputSource in, ErrorPrintHandler veh, ErrorPrintHandler reh, String schemaPath) {
    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    builder.put(ValidateProperty.ERROR_HANDLER, veh);
    PropertyMap instanceProperties = builder.toPropertyMap();

    Validator validator = schema.createValidator(instanceProperties);

    ContentHandler ch = validator.getContentHandler();
    DTDHandler dh = validator.getDTDHandler();

    parse(in, reh, ch, dh, schemaPath);
    lastActive = System.currentTimeMillis();
    validator.reset();
  }

  private long getLastActive() {
    return lastActive;
  }

  @Override
  public int compareTo(Object obj) {
    long objLastActive = ((Driver) obj).getLastActive();
    if (this.lastActive > objLastActive) return 1;
    if (this.lastActive > objLastActive) return -1;
    return 0;
  }
}
