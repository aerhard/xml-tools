package com.thaiopensource.suggest.relaxng.pattern;

class RecoverAfterFunction extends AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
  private final ValidatorPatternBuilder builder;

  RecoverAfterFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    throw new RuntimeException("recover after botch");
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().apply(this),
                              p.getOperand2().apply(this));
  }

  public Pattern caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    return p.getOperand2();
  }
}
