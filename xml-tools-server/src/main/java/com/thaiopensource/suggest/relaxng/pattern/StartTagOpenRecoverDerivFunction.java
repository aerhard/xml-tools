package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

class StartTagOpenRecoverDerivFunction extends com.thaiopensource.suggest.relaxng.pattern.StartTagOpenDerivFunction {
  StartTagOpenRecoverDerivFunction(Name name, ValidatorPatternBuilder builder) {
    super(name, builder);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    Pattern tem = super.caseGroup(p);
    if (p.getOperand1().isNullable())
      return tem;
    return getPatternBuilder().makeChoice(tem, memoApply(p.getOperand2()));
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.startTagOpenRecoverDeriv(this);
  }
}
