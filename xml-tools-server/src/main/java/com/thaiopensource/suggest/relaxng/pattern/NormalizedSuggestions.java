package com.thaiopensource.suggest.relaxng.pattern;

import java.util.Set;

public class NormalizedSuggestions {
  private final boolean anyNameIncluded;
  private final Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> includedNames;
  private final Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> includedNamespaces;
  private final Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> excludedNamespaces;
  private final Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> excludedNames;

  public NormalizedSuggestions(boolean anyNameIncluded, Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> includedNames, Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> includedNamespaces, Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> excludedNames, Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> excludedNamespaces) {
    this.anyNameIncluded = anyNameIncluded;
    this.includedNames = includedNames;
    this.includedNamespaces = includedNamespaces;
    this.excludedNamespaces = excludedNamespaces;
    this.excludedNames = excludedNames;
  }

  public NormalizedSuggestions(boolean anyNameIncluded, Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> includedNames, Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> includedNamespaces) {
    this(anyNameIncluded, includedNames, includedNamespaces, null, null);
  }

  public boolean isAnyNameIncluded() {
    return anyNameIncluded;
  }

  public boolean hasNamedInclusions() {
    return !includedNames.isEmpty() || !includedNamespaces.isEmpty();
  }

  public Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> getIncludedNames() {
    return includedNames;
  }

  public Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> getIncludedNamespaces() {
    return includedNamespaces;
  }

  public Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> getExcludedNames() {
    return excludedNames;
  }

  public Set<NamespaceSuggestion> getExcludedNamespaces() {
    return excludedNamespaces;
  }
}
