package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

class HasEmptyContentFunction extends AbstractPatternFunction<VoidValue> {

  private boolean empty = true;

  public VoidValue caseData(DataPattern p){
    empty = false;
    return VoidValue.VOID;
  }

  public VoidValue caseDataExcept(DataExceptPattern p){
    empty = false;
    return VoidValue.VOID;
  }

  public VoidValue caseElement(ElementPattern p) {
    empty = false;
    return VoidValue.VOID;
  }

  public VoidValue caseValue(ValuePattern p){
    empty = false;
    return VoidValue.VOID;
  }

  public VoidValue caseText(TextPattern p){
    empty = false;
    return VoidValue.VOID;
  }

  public VoidValue caseList(ListPattern p){
    empty = false;
    return VoidValue.VOID;
  }

  public VoidValue caseGroup(GroupPattern p) {
    p.getOperand1().apply(this);
    if (p.getOperand1().isNullable())
      p.getOperand2().apply(this);
    return VoidValue.VOID;
  }

  boolean applyTo(Pattern p) {
    p.apply(this);
    return empty;
  }

  public VoidValue caseAfter(AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public VoidValue caseBinary(BinaryPattern p) {
    p.getOperand1().apply(this);
    p.getOperand2().apply(this);
    return VoidValue.VOID;
  }

  public VoidValue caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseOneOrMore(OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }

  public VoidValue caseOther(Pattern p) {
    return VoidValue.VOID;
  }
}
