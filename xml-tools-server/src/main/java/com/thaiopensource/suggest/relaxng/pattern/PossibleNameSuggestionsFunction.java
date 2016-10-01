package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

abstract class PossibleNameSuggestionsFunction extends com.thaiopensource.suggest.relaxng.pattern.AbstractPatternFunction<VoidValue> {
  private final com.thaiopensource.suggest.relaxng.pattern.NameSuggestionNormalizer normalizer = new NameSuggestionNormalizer();

  NormalizedSuggestions applyTo(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    p.apply(this);
    return normalizer.normalize();
  }

  void add(com.thaiopensource.suggest.relaxng.pattern.Pattern p, NameClass nc) {
    normalizer.add(p, nc);
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
