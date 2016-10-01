package com.thaiopensource.suggest.relaxng.pattern;

class TextOnlyFunction extends EndAttributesFunction {
  TextOnlyFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }
  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
    return p;
  }
  public Pattern caseElement(ElementPattern p) {
    return getPatternBuilder().makeNotAllowed();
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.textOnly(this);
  }

}

