package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.xml.util.Name;

public class AttributeNameClassChecker implements com.thaiopensource.suggest.relaxng.pattern.NameClassVisitor {
  private String errorMessageId = null;
  
  public void visitChoice(com.thaiopensource.suggest.relaxng.pattern.NameClass nc1, com.thaiopensource.suggest.relaxng.pattern.NameClass nc2) {
    nc1.accept(this);
    nc2.accept(this);
  }

  public void visitNsName(String ns) {
    if (ns.equals(WellKnownNamespaces.XMLNS))
      errorMessageId = "xmlns_uri_attribute";
  }

  public void visitNsNameExcept(String ns, com.thaiopensource.suggest.relaxng.pattern.NameClass nc) {
    visitNsName(ns);
    nc.accept(this);
  }

  public void visitAnyName() { }

  public void visitAnyNameExcept(com.thaiopensource.suggest.relaxng.pattern.NameClass nc) {
    nc.accept(this);
  }

  public void visitName(Name name) {
    visitNsName(name.getNamespaceUri());
    if (name.equals(new Name("", "xmlns")))
      errorMessageId = "xmlns_attribute";
  }

  public void visitNull() { }

  public void visitError() { }

  public String checkNameClass(NameClass nc) {
    errorMessageId = null;
    nc.accept(this);
    return errorMessageId;
  }
}
