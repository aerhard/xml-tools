package com.aerhard.xml.tools;

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

  synchronized private void parse(InputSource in, ErrorHandler eh, ContentHandler ch, DTDHandler dh) throws SAXException {
    if (xr == null) {
      xr = ResolverFactory.createResolver(properties).createXMLReader();
    }

    xr.setErrorHandler(eh);
    xr.setContentHandler(ch);
    if (dh != null) {
      xr.setDTDHandler(dh);
    }

    try {
      xr.parse(in);
    } catch (Exception e) {}
  }

  public JSONArray runSuggester(InputSource in, ErrorHandler eh, String suggestionType,
                                String fragment) throws SAXException {

    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    builder.put(ValidateProperty.ERROR_HANDLER, eh);
    PropertyMap instanceProperties = builder.toPropertyMap();

    SuggesterSchema suggesterSchema = (SuggesterSchema) schema;
    Suggester suggester = suggesterSchema.createSuggester(instanceProperties);

    parse(in, eh, suggester, suggester);

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

  public void runValidator(InputSource in, ErrorHandler veh, ErrorHandler reh) throws SAXException, IOException {
    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    builder.put(ValidateProperty.ERROR_HANDLER, veh);
    PropertyMap instanceProperties = builder.toPropertyMap();

    Validator validator = schema.createValidator(instanceProperties);

    ContentHandler ch = validator.getContentHandler();
    DTDHandler dh = validator.getDTDHandler();

    parse(in, reh, ch, dh);
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
