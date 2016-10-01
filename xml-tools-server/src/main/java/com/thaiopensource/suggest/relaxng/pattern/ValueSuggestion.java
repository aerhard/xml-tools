package com.thaiopensource.suggest.relaxng.pattern;

public class ValueSuggestion {
  private final com.thaiopensource.suggest.relaxng.pattern.ValuePattern p;
  private final boolean inList;

  public ValueSuggestion(com.thaiopensource.suggest.relaxng.pattern.ValuePattern p, boolean inList) {
    this.p = p;
    this.inList = inList;
  }

  public com.thaiopensource.suggest.relaxng.pattern.ValuePattern getPattern() {
    return p;
  }

  public boolean isInList() {
    return inList;
  }
}