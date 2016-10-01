package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

/**
 * Common base class for PossibleAttributeNamesFunction and PossibleStartTagNamesFunction.
 * @see PossibleAttributeNamesFunction
 * @see PossibleStartTagNamesFunction
 */
abstract class PossibleNamesFunction extends AbstractPatternFunction<VoidValue> {
  private final UnionNameClassNormalizer normalizer = new UnionNameClassNormalizer();

  com.thaiopensource.suggest.relaxng.pattern.NormalizedNameClass applyTo(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    normalizer.setNameClass(new com.thaiopensource.suggest.relaxng.pattern.NullNameClass());
    p.apply(this);
    return normalizer.normalize();
  }

  void add(NameClass nc) {
    normalizer.add(nc);
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
