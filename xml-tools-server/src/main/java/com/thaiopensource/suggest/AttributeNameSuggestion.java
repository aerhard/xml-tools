package com.thaiopensource.suggest;

import java.util.List;
import java.util.Objects;

public class AttributeNameSuggestion implements Suggestion {
  private final String value;
  private final List<String> documentation;

  public AttributeNameSuggestion(String value, List<String> documentation) {
    this.value = value;
    this.documentation = documentation;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public List<String> getDocumentation() {
    return documentation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AttributeNameSuggestion that = (AttributeNameSuggestion) o;
    return Objects.equals(value, that.value) &&
            Objects.equals(documentation, that.documentation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, documentation);
  }
}
