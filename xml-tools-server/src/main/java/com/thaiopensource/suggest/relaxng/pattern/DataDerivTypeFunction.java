package com.thaiopensource.suggest.relaxng.pattern;

class DataDerivTypeFunction extends AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.DataDerivType> {
  private final ValidatorPatternBuilder builder;

  DataDerivTypeFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  static com.thaiopensource.suggest.relaxng.pattern.DataDerivType dataDerivType(ValidatorPatternBuilder builder, com.thaiopensource.suggest.relaxng.pattern.Pattern pattern) {
    return pattern.apply(builder.getDataDerivTypeFunction());
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    return new com.thaiopensource.suggest.relaxng.pattern.SingleDataDerivType();
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseRef(com.thaiopensource.suggest.relaxng.pattern.RefPattern p) {
    return apply(p.getPattern());
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    com.thaiopensource.suggest.relaxng.pattern.DataDerivType ddt = apply(p.getOperand1());
    if (!p1.isNullable())
      return ddt;
    return ddt.combine(new com.thaiopensource.suggest.relaxng.pattern.BlankDataDerivType());
  }

  private com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseBinary(com.thaiopensource.suggest.relaxng.pattern.BinaryPattern p) {
    return apply(p.getOperand1()).combine(apply(p.getOperand2()));
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return caseBinary(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    return caseBinary(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    return caseBinary(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    return apply(p.getOperand());
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseList(com.thaiopensource.suggest.relaxng.pattern.ListPattern p) {
    return InconsistentDataDerivType.getInstance();
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseValue(ValuePattern p) {
    return new com.thaiopensource.suggest.relaxng.pattern.ValueDataDerivType(p.getDatatype(), p.getDatatypeName());
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseData(com.thaiopensource.suggest.relaxng.pattern.DataPattern p) {
    if (p.allowsAnyString())
      return new com.thaiopensource.suggest.relaxng.pattern.SingleDataDerivType();
    return new com.thaiopensource.suggest.relaxng.pattern.DataDataDerivType(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.DataDerivType caseDataExcept(com.thaiopensource.suggest.relaxng.pattern.DataExceptPattern p) {
    if (p.allowsAnyString())
      return apply(p.getExcept());
    return new com.thaiopensource.suggest.relaxng.pattern.DataDataDerivType(p).combine(apply(p.getExcept()));
  }

  private com.thaiopensource.suggest.relaxng.pattern.DataDerivType apply(Pattern p) {
    return builder.getPatternMemo(p).dataDerivType();
  }
}
