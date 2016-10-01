package com.thaiopensource.suggest.relaxng.pattern;

abstract class ApplyAfterFunction extends AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
  private final ValidatorPatternBuilder builder;

  ApplyAfterFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    return builder.makeAfter(p.getOperand1(), apply(p.getOperand2()));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().apply(this),
                              p.getOperand2().apply(this));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseNotAllowed(com.thaiopensource.suggest.relaxng.pattern.NotAllowedPattern p) {
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    throw new AssertionError("ApplyAfterFunction applied to " + p.getClass().getName());
  }

  abstract com.thaiopensource.suggest.relaxng.pattern.Pattern apply(Pattern p);
}
