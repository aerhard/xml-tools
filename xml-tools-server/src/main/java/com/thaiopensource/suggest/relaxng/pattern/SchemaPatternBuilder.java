package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;

import java.util.List;

public class SchemaPatternBuilder extends com.thaiopensource.suggest.relaxng.pattern.PatternBuilder {
  private boolean idTypes;
  private final UnexpandedNotAllowedPattern unexpandedNotAllowed = new UnexpandedNotAllowedPattern();
  private final com.thaiopensource.suggest.relaxng.pattern.TextPattern text = new com.thaiopensource.suggest.relaxng.pattern.TextPattern();
  private final com.thaiopensource.suggest.relaxng.pattern.PatternInterner schemaInterner = new com.thaiopensource.suggest.relaxng.pattern.PatternInterner();

  public SchemaPatternBuilder() { }

  public boolean hasIdTypes() {
    return idTypes;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeElement(com.thaiopensource.suggest.relaxng.pattern.NameClass nameClass, com.thaiopensource.suggest.relaxng.pattern.Pattern content, SourceLocation loc) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new ElementPattern(nameClass, content, loc);
    return schemaInterner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeAttribute(NameClass nameClass, com.thaiopensource.suggest.relaxng.pattern.Pattern value, SourceLocation loc) {
    if (value == notAllowed)
      return value;
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new com.thaiopensource.suggest.relaxng.pattern.AttributePattern(nameClass, value, loc);
    return schemaInterner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeData(Datatype dt, Name dtName, List<String> params) {
    noteDatatype(dt);
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new com.thaiopensource.suggest.relaxng.pattern.DataPattern(dt, dtName, params);
    return schemaInterner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeDataExcept(Datatype dt, Name dtName, List<String> params, com.thaiopensource.suggest.relaxng.pattern.Pattern except, SourceLocation loc) {
    noteDatatype(dt);
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new com.thaiopensource.suggest.relaxng.pattern.DataExceptPattern(dt, dtName, params, except, loc);
    return schemaInterner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeValue(Datatype dt, Name dtName, Object value, String stringValue) {
    noteDatatype(dt);
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new ValuePattern(dt, dtName, value, stringValue);
    return schemaInterner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeText() {
    return text;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeOneOrMore(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    if (p == text)
      return p;
    return super.makeOneOrMore(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeUnexpandedNotAllowed() {
    return unexpandedNotAllowed;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeError() {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new com.thaiopensource.suggest.relaxng.pattern.ErrorPattern();
    return schemaInterner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeChoice(com.thaiopensource.suggest.relaxng.pattern.Pattern p1, com.thaiopensource.suggest.relaxng.pattern.Pattern p2) {
    if (p1 == notAllowed || p1 == p2)
      return p2;
    if (p2 == notAllowed)
      return p1;
    return super.makeChoice(p1, p2);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeList(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc) {
    if (p == notAllowed)
      return p;
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = new com.thaiopensource.suggest.relaxng.pattern.ListPattern(p, loc);
    return schemaInterner.intern(p1);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeMixed(Pattern p) {
    return makeInterleave(text, p);
  }

  private void noteDatatype(Datatype dt) {
    if (dt.getIdType() != Datatype.ID_TYPE_NULL)
      idTypes = true;
  }
}
