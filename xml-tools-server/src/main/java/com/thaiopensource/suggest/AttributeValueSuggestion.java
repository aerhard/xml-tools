package com.thaiopensource.suggest;

import java.util.List;
import java.util.Objects;

public class AttributeValueSuggestion implements com.thaiopensource.suggest.Suggestion {
  private final String value;
  private final List<String> documentation;
  private final boolean listItem;

  public AttributeValueSuggestion(String value, List<String> documentation, boolean listItem) {
    this.value = value;
    this.documentation = documentation;
    this.listItem = listItem;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public List<String> getDocumentation() {
    return documentation;
  }

  public boolean isListItem() {
    return listItem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AttributeValueSuggestion that = (AttributeValueSuggestion) o;
    return listItem == that.listItem &&
            Objects.equals(value, that.value) &&
            Objects.equals(documentation, that.documentation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, documentation, listItem);
  }
}
