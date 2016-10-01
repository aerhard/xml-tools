package com.thaiopensource.suggest;

import java.util.List;

public class ElementSuggestion implements com.thaiopensource.suggest.Suggestion {
  private final String value;
  private final List<String> documentation;
  private final List<String> attributes;
  private final boolean empty;
  private final boolean closing;

  public ElementSuggestion(String value, List<String> documentation, List<String> attributes, boolean empty, boolean closing) {
    this.value = value;
    this.documentation = documentation;
    this.attributes = attributes;
    this.empty = empty;
    this.closing = closing;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public List<String> getDocumentation() {
    return documentation;
  }

  public List<String> getAttributes() {
    return attributes;
  }

  public boolean isEmpty() {
    return empty;
  }

  public boolean isClosing() { return closing; }
}
