package com.thaiopensource.suggest.relaxng.pattern;

class ErrorPattern extends com.thaiopensource.suggest.relaxng.pattern.Pattern {
  ErrorPattern() {
    super(false, EMPTY_CONTENT_TYPE, ERROR_HASH_CODE);
  }
  boolean samePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern other) {
    return other instanceof ErrorPattern;
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseError(this);
  }
}
