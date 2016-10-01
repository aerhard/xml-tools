package com.thaiopensource.suggest.relaxng.pattern;

class EndTagDerivFunction extends AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
  private final ValidatorPatternBuilder builder;

  EndTagDerivFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    return builder.makeNotAllowed();
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return builder.makeChoice(memoApply(p.getOperand1()),
			      memoApply(p.getOperand2()));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    if (p.getOperand1().isNullable())
      return p.getOperand2();
    else
      return builder.makeNotAllowed();
  }

  private com.thaiopensource.suggest.relaxng.pattern.Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  private com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.endTagDeriv(this);
  }
}
