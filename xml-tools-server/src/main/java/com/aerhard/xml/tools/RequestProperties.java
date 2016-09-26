package com.aerhard.xml.tools;

import com.thaiopensource.util.UriOrFile;

public class RequestProperties {

  private final String catalogUri;
  private final boolean resolveSchemaPath;
  private final String suggestionType;
  private final String encoding;
  private final String fragment;

  public RequestProperties(String catalog, String optionsString, String encoding) {
    this(catalog, optionsString, encoding, null, null);
  }

  public RequestProperties(String catalog, String optionsString, String encoding, String suggestionType, String fragment) {
    this.encoding = encoding;
    this.fragment = fragment;
    final boolean resolveSchemaPath = optionsString.contains("r");

    this.catalogUri = "".equals(catalog) ? null : UriOrFile.toUri(catalog);
    this.resolveSchemaPath = resolveSchemaPath;
    this.suggestionType = suggestionType;
  }

  public String getCatalogUri() {
    return catalogUri;
  }
  public String getSuggestionType() { return suggestionType; }
  public String getEncoding() {
    return encoding;
  }
  public String getFragment() { return fragment; }

  public boolean shouldResolveSchemaPath() {
    return resolveSchemaPath;
  }

  @Override
  public String toString() {
    return this.resolveSchemaPath + "\n" + this.catalogUri;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof RequestProperties && hashCode() == obj.hashCode();
  }
}
