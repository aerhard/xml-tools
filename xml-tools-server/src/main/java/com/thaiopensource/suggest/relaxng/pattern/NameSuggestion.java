package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

public class NameSuggestion {
  private final Name name;
  private final com.thaiopensource.suggest.relaxng.pattern.NameClass nc;
  private final com.thaiopensource.suggest.relaxng.pattern.Pattern p;

  public NameSuggestion(Name name, com.thaiopensource.suggest.relaxng.pattern.NameClass nc, com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    this.name = name;
    this.nc = nc;
    this.p = p;
  }

  public Name getName() {
    return name;
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass getNameClass() {
    return nc;
  }

  public Pattern getPattern() {
    return p;
  }
}