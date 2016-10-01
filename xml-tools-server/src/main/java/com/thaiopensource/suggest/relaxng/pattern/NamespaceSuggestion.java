package com.thaiopensource.suggest.relaxng.pattern;

public class NamespaceSuggestion {
  private final String ns;
  private final com.thaiopensource.suggest.relaxng.pattern.NameClass nc;
  private final com.thaiopensource.suggest.relaxng.pattern.Pattern p;

  public NamespaceSuggestion(String ns, com.thaiopensource.suggest.relaxng.pattern.NameClass nc, com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    this.ns = ns;
    this.nc = nc;
    this.p = p;
  }

  public String getNamespace() {
    return ns;
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass getNameClass() {
    return nc;
  }

  public Pattern getPattern() {
    return p;
  }
}