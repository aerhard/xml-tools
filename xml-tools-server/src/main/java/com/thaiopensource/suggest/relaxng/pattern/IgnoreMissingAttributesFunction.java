package com.thaiopensource.suggest.relaxng.pattern;

class IgnoreMissingAttributesFunction extends com.thaiopensource.suggest.relaxng.pattern.EndAttributesFunction {
  IgnoreMissingAttributesFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }

  public Pattern caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
    return getPatternBuilder().makeEmpty();
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.ignoreMissingAttributes(this);
  }
}
