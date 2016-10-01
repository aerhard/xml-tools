package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

public interface NameClassVisitor {
  void visitChoice(com.thaiopensource.suggest.relaxng.pattern.NameClass nc1, com.thaiopensource.suggest.relaxng.pattern.NameClass nc2);
  void visitNsName(String ns);
  void visitNsNameExcept(String ns, com.thaiopensource.suggest.relaxng.pattern.NameClass nc);
  void visitAnyName();
  void visitAnyNameExcept(com.thaiopensource.suggest.relaxng.pattern.NameClass nc);
  void visitName(Name name);
  void visitNull();
  void visitError();
}
