package com.thaiopensource.suggest.relaxng.pattern;


import com.thaiopensource.xml.util.Name;

import java.util.List;

public class NameSuggestionVisitor implements com.thaiopensource.suggest.relaxng.pattern.NameClassVisitor {
  List<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> mentionedNames;
  List<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> namespaceSuggestions;
  private com.thaiopensource.suggest.relaxng.pattern.Pattern currentPattern;
  private com.thaiopensource.suggest.relaxng.pattern.NameClass currentNameClass;

  public NameSuggestionVisitor(List<com.thaiopensource.suggest.relaxng.pattern.NameSuggestion> mentionedNames, List<com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion> namespaceSuggestions) {
    this.mentionedNames = mentionedNames;
    this.namespaceSuggestions = namespaceSuggestions;
  }

  public void start(Pattern p, com.thaiopensource.suggest.relaxng.pattern.NameClass nameClass) {
    currentPattern = p;
    currentNameClass = nameClass;
    currentNameClass.accept(this);
  }

  public void visitName(Name name) {
    mentionedNames.add(new NameSuggestion(name, currentNameClass, currentPattern));
  }

  public void visitNsName(String ns) {
    namespaceSuggestions.add(new com.thaiopensource.suggest.relaxng.pattern.NamespaceSuggestion(ns, currentNameClass, currentPattern));
  }

  public void visitNsNameExcept(String ns, com.thaiopensource.suggest.relaxng.pattern.NameClass nc) {
    namespaceSuggestions.add(new NamespaceSuggestion(ns, currentNameClass, currentPattern));
    com.thaiopensource.suggest.relaxng.pattern.NameClass parentNameClass = currentNameClass;
    currentNameClass = nc;
    nc.accept(this);
    currentNameClass = parentNameClass;
  }

  public void visitChoice(com.thaiopensource.suggest.relaxng.pattern.NameClass nc1, com.thaiopensource.suggest.relaxng.pattern.NameClass nc2) {
    com.thaiopensource.suggest.relaxng.pattern.NameClass parentNameClass = currentNameClass;
    currentNameClass = nc1;
    nc1.accept(this);
    currentNameClass = nc2;
    nc2.accept(this);
    currentNameClass = parentNameClass;
  }

  public void visitAnyNameExcept(com.thaiopensource.suggest.relaxng.pattern.NameClass nc) {
    NameClass parentNameClass = currentNameClass;
    currentNameClass = nc;
    nc.accept(this);
    currentNameClass = parentNameClass;
  }

  public void visitAnyName() {
  }

  public void visitNull() {
  }

  public void visitError() {
  }
}