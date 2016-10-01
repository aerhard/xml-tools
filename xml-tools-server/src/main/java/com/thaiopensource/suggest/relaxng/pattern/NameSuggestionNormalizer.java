package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.*;

public class NameSuggestionNormalizer {
  private static final String IMPOSSIBLE = "\u0000";

  public List<com.thaiopensource.suggest.relaxng.pattern.Pattern> patterns = new ArrayList<com.thaiopensource.suggest.relaxng.pattern.Pattern>();
  public List<com.thaiopensource.suggest.relaxng.pattern.NameClass> nameClasses = new ArrayList<com.thaiopensource.suggest.relaxng.pattern.NameClass>();

  private boolean nameClassesContain(Name name) {
    for (com.thaiopensource.suggest.relaxng.pattern.NameClass nc : nameClasses) {
      if (nc.contains(name)) return true;
    }
    return false;
  }

  public com.thaiopensource.suggest.relaxng.pattern.NormalizedSuggestions normalize() {
    List<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> mentionedNames = new ArrayList<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion>();
    List<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> namespaceSuggestions = new ArrayList<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion>();

    com.thaiopensource.suggest.relaxng.pattern.NameSuggestionVisitor nameSuggestionVisitor = new com.thaiopensource.suggest.relaxng.pattern.NameSuggestionVisitor(mentionedNames, namespaceSuggestions);

    for (com.thaiopensource.suggest.relaxng.pattern.Pattern p : patterns) {
      if (p instanceof com.thaiopensource.suggest.relaxng.pattern.ElementPattern) {
        nameSuggestionVisitor.start(p, ((ElementPattern) p).getNameClass());
      } else if (p instanceof com.thaiopensource.suggest.relaxng.pattern.AttributePattern) {
        nameSuggestionVisitor.start(p, ((com.thaiopensource.suggest.relaxng.pattern.AttributePattern) p).getNameClass());
      }
    }

    if (nameClassesContain(new Name(IMPOSSIBLE, IMPOSSIBLE))) {
      Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> includedNames = new HashSet<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion>();
      Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> excludedNamespaces = new HashSet<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion>();
      Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> excludedNames = new HashSet<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion>();
      Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> includedNamespaces = new HashSet<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion>();

      for (com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion mns : namespaceSuggestions) {
        if (!nameClassesContain(new Name(mns.getNamespace(), IMPOSSIBLE))) {
          excludedNamespaces.add(mns);
        }

        String ns = mns.getNamespace();
        if (nameClassesContain(new Name(ns, IMPOSSIBLE))) {
          includedNamespaces.add(mns);
        }
      }

      for (com.thaiopensource.suggest.relaxng.pattern.NameSuggestion mns : mentionedNames) {
        Name name = mns.getName();
        boolean in = nameClassesContain(name);
        if (in) {
          includedNames.add(mns);
        } else {
          excludedNames.add(mns);
        }
      }
      return new com.thaiopensource.suggest.relaxng.pattern.NormalizedSuggestions(true, includedNames, includedNamespaces, excludedNames, excludedNamespaces);
    }

    Set<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> includedNamespaces = new HashSet<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion>();
    Map<String, HashSet<String>> nsMap = new HashMap<String, HashSet<String>>();
    for (NamespaceSuggestion mns : namespaceSuggestions) {
      String ns = mns.getNamespace();
      if (nameClassesContain(new Name(ns, IMPOSSIBLE)) && nsMap.get(ns) == null) {
        nsMap.put(ns, new HashSet<String>());
        includedNamespaces.add(mns);
      }
    }

    Set<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> includedNames = new HashSet<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion>();
    for (NameSuggestion mn : mentionedNames) {
      Name name = mn.getName();
      boolean in = nameClassesContain(name);
      Set<String> excluded = nsMap.get(name.getNamespaceUri());
      if (excluded == null) {
        if (in) {
          includedNames.add(mn);
        }
      } else if (!in) {
        excluded.add(name.getLocalName());
      }
    }
    return new NormalizedSuggestions(false, includedNames, includedNamespaces);
  }

  public void add(Pattern p, NameClass nameClass) {
    patterns.add(p);
    nameClasses.add(nameClass);
  }
}
