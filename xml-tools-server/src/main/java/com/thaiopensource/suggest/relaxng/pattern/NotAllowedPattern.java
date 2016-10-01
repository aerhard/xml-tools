package com.thaiopensource.suggest.relaxng.pattern;

class NotAllowedPattern extends com.thaiopensource.suggest.relaxng.pattern.Pattern {
  NotAllowedPattern() {
    super(false, EMPTY_CONTENT_TYPE, NOT_ALLOWED_HASH_CODE);
  }
  boolean isNotAllowed() {
    return true;
  }
  boolean samePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern other) {
    // needs to work for UnexpandedNotAllowedPattern
    return other.getClass() == this.getClass();
  }

  <T> T apply(com.thaiopensource.suggest.relaxng.pattern.PatternFunction<T> f) {
    return f.caseNotAllowed(this);
  }
}
