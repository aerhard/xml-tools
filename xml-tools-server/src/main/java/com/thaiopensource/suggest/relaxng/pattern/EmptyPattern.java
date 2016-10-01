package com.thaiopensource.suggest.relaxng.pattern;

class EmptyPattern extends com.thaiopensource.suggest.relaxng.pattern.Pattern {
  EmptyPattern() {
    super(true, EMPTY_CONTENT_TYPE, EMPTY_HASH_CODE);
  }
  boolean samePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern other) {
    return other instanceof EmptyPattern;
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseEmpty(this);
  }
  public void checkRestrictions(int context, DuplicateAttributeDetector dad, com.thaiopensource.suggest.relaxng.pattern.Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_empty");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_empty");
    }
  }
}
