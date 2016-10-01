package com.thaiopensource.suggest.relaxng.pattern;

class EndAttributesFunction extends AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
  private final ValidatorPatternBuilder builder;

  EndAttributesFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    com.thaiopensource.suggest.relaxng.pattern.Pattern q1 = memoApply(p1);
    com.thaiopensource.suggest.relaxng.pattern.Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeGroup(q1, q2);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    com.thaiopensource.suggest.relaxng.pattern.Pattern q1 = memoApply(p1);
    com.thaiopensource.suggest.relaxng.pattern.Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeInterleave(q1, q2);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    com.thaiopensource.suggest.relaxng.pattern.Pattern q1 = memoApply(p1);
    com.thaiopensource.suggest.relaxng.pattern.Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeChoice(q1, q2);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand();
    com.thaiopensource.suggest.relaxng.pattern.Pattern q1 = memoApply(p1);
    if (p1 == q1)
      return p;
    return builder.makeOneOrMore(q1);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    com.thaiopensource.suggest.relaxng.pattern.Pattern q1 = memoApply(p1);
    if (p1 == q1)
      return p;
    return builder.makeAfter(q1, p.getOperand2());
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
    return builder.makeNotAllowed();
  }

  final com.thaiopensource.suggest.relaxng.pattern.Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.endAttributes(this);
  }

  ValidatorPatternBuilder getPatternBuilder() {
    return builder;
  }
}
