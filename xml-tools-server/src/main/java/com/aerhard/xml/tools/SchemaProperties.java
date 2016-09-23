package com.aerhard.xml.tools;

class SchemaProperties {
  private final String type;
  private final String path;
  private final RequestProperties requestProperties;

  public SchemaProperties(String schemaLine, RequestProperties requestProperties) {
    String[] tokens = schemaLine.split("\\s", 2);
    this.type = tokens[0];
    this.path = "".equals(tokens[1]) ? null : tokens[1];
    this.requestProperties = requestProperties;
  }

  public String getType() {
    return type;
  }

  public String getPath() {
    return path;
  }

  public RequestProperties getRequestProperties() {
    return requestProperties;
  }

  @Override
  public String toString() {
    return this.requestProperties.toString() + "\n" + this.type + "\n" + this.path;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof SchemaProperties && hashCode() == obj.hashCode();
  }
}
