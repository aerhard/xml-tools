package com.thaiopensource.suggest.relaxng.pattern;

class MixedTextDerivFunction extends com.thaiopensource.suggest.relaxng.pattern.EndAttributesFunction {

  MixedTextDerivFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseText(com.thaiopensource.suggest.relaxng.pattern.TextPattern p) {
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern q1 = memoApply(p1);
    com.thaiopensource.suggest.relaxng.pattern.Pattern tem = (q1 == p1) ? p : getPatternBuilder().makeGroup(q1, p2);
    if (!p1.isNullable())
      return tem;
    return getPatternBuilder().makeChoice(tem, memoApply(p2));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern q1 = memoApply(p1);
    final com.thaiopensource.suggest.relaxng.pattern.Pattern q2 = memoApply(p2);
    final com.thaiopensource.suggest.relaxng.pattern.Pattern i1 = (q1 == p1) ? p : getPatternBuilder().makeInterleave(q1, p2);
    final com.thaiopensource.suggest.relaxng.pattern.Pattern i2 = (q2 == p2) ? p : getPatternBuilder().makeInterleave(p1, q2);
    return getPatternBuilder().makeChoice(i1, i2);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    return getPatternBuilder().makeGroup(memoApply(p.getOperand()),
					 getPatternBuilder().makeOptional(p));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(Pattern p) {
    return getPatternBuilder().makeNotAllowed();
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.mixedTextDeriv(this);
  }
}
