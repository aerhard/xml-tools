package com.thaiopensource.suggest.relaxng.pattern;

public class ValueSuggestion {
  private final Pattern p;
  private final boolean inList;

  public ValueSuggestion(Pattern p, boolean inList) {
    this.p = p;
    this.inList = inList;
  }

  public Pattern getPattern() {
    return p;
  }

  public boolean isInList() {
    return inList;
  }
}