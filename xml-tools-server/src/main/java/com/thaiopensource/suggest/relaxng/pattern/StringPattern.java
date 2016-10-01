package com.thaiopensource.suggest.relaxng.pattern;

abstract class StringPattern extends com.thaiopensource.suggest.relaxng.pattern.Pattern {
  StringPattern(int hc) {
    super(false, DATA_CONTENT_TYPE, hc);
  }
}
