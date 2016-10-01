package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

class StartAttributeDerivFunction extends com.thaiopensource.suggest.relaxng.pattern.StartTagOpenDerivFunction {
  StartAttributeDerivFunction(Name name, ValidatorPatternBuilder builder) {
    super(name, builder);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseElement(ElementPattern p) {
    return getPatternBuilder().makeNotAllowed();
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    return getPatternBuilder().makeChoice(
            memoApply(p1).apply(new com.thaiopensource.suggest.relaxng.pattern.ApplyAfterFunction(getPatternBuilder()) {
              com.thaiopensource.suggest.relaxng.pattern.Pattern apply(com.thaiopensource.suggest.relaxng.pattern.Pattern x) {
                return getPatternBuilder().makeGroup(x, p2);
              }
            }),
            memoApply(p2).apply(new com.thaiopensource.suggest.relaxng.pattern.ApplyAfterFunction(getPatternBuilder()) {
              com.thaiopensource.suggest.relaxng.pattern.Pattern apply(com.thaiopensource.suggest.relaxng.pattern.Pattern x) {
                return getPatternBuilder().makeGroup(p1, x);
              }
            }));
  }

  public Pattern caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
    if (!p.getNameClass().contains(getName()))
      return getPatternBuilder().makeNotAllowed();
    return getPatternBuilder().makeAfter(p.getContent(),
					 getPatternBuilder().makeEmpty());
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.startAttributeDeriv(this);
  }
}
