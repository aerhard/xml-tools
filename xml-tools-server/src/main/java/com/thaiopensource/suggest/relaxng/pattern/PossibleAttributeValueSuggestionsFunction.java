package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

import java.util.HashSet;
import java.util.Set;

class PossibleAttributeValueSuggestionsFunction extends com.thaiopensource.suggest.relaxng.pattern.AbstractPatternFunction<VoidValue> {
  private boolean inList = false;

  private Set<com.thaiopensource.suggest.relaxng.pattern.ValueSuggestion> patterns = new HashSet<com.thaiopensource.suggest.relaxng.pattern.ValueSuggestion>();

 Set<com.thaiopensource.suggest.relaxng.pattern.ValueSuggestion> applyTo(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    p.apply(this);
    return patterns;
  }

  void add(com.thaiopensource.suggest.relaxng.pattern.ValuePattern p) {
    patterns.add(new ValueSuggestion(p, inList));
  }

  public VoidValue caseValue(ValuePattern p) {
    add(p);
    return VoidValue.VOID;
  }

  public VoidValue caseList(com.thaiopensource.suggest.relaxng.pattern.ListPattern p) {
    inList = true;
    p.getOperand().apply(this);
    inList = false;
    return VoidValue.VOID;
  }

  public VoidValue caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    return caseBinary(p);
  }
  public VoidValue caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public VoidValue caseBinary(com.thaiopensource.suggest.relaxng.pattern.BinaryPattern p) {
    p.getOperand1().apply(this);
    p.getOperand2().apply(this);
    return VoidValue.VOID;
  }

  public VoidValue caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return caseBinary(p);
  }
  public VoidValue caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    return caseBinary(p);
  }
  public VoidValue caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }
  public VoidValue caseOther(Pattern p) {
    return VoidValue.VOID;
  }
}
