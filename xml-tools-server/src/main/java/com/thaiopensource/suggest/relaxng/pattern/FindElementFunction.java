package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

import java.util.HashSet;
import java.util.Set;

class FindElementFunction extends AbstractPatternFunction<VoidValue> {
  private final ValidatorPatternBuilder builder;
  private final Name name;
  private final Set<com.thaiopensource.suggest.relaxng.pattern.Pattern> processed = new HashSet<com.thaiopensource.suggest.relaxng.pattern.Pattern>();
  private int specificity = com.thaiopensource.suggest.relaxng.pattern.NameClass.SPECIFICITY_NONE;
  private com.thaiopensource.suggest.relaxng.pattern.Pattern pattern = null;

  static public com.thaiopensource.suggest.relaxng.pattern.Pattern findElement(ValidatorPatternBuilder builder, Name name, com.thaiopensource.suggest.relaxng.pattern.Pattern start) {
    FindElementFunction f = new FindElementFunction(builder, name);
    start.apply(f);
    if (f.pattern == null)
      return builder.makeNotAllowed();
    return f.pattern;
  }

  private FindElementFunction(ValidatorPatternBuilder builder, Name name) {
    this.builder = builder;
    this.name = name;
  }

  private boolean haveProcessed(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    if (processed.contains(p))
      return true;
    processed.add(p);
    return false;
  }

  private VoidValue caseBinary(com.thaiopensource.suggest.relaxng.pattern.BinaryPattern p) {
    if (!haveProcessed(p)) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
    }
    return VoidValue.VOID;

 }

  public VoidValue caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    return caseBinary(p);
  }

  public VoidValue caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    if (!haveProcessed(p))
      p.getOperand().apply(this);
    return VoidValue.VOID;
  }

  public VoidValue caseElement(ElementPattern p) {
    if (!haveProcessed(p)) {
      int s = p.getNameClass().containsSpecificity(name);
      if (s > specificity) {
        specificity = s;
        pattern = p.getContent();
      }
      else if (s == specificity && s != NameClass.SPECIFICITY_NONE)
        pattern = builder.makeChoice(pattern, p.getContent());
      p.getContent().apply(this);
    }
    return VoidValue.VOID;
  }

  public VoidValue caseOther(Pattern p) {
    return VoidValue.VOID;
  }
}
