package com.thaiopensource.suggest.relaxng.pattern;

class InconsistentDataDerivType extends com.thaiopensource.suggest.relaxng.pattern.DataDerivType {
  static private final InconsistentDataDerivType instance = new InconsistentDataDerivType();

  static InconsistentDataDerivType getInstance() {
    return instance;
  }

  private InconsistentDataDerivType() { }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType combine(com.thaiopensource.suggest.relaxng.pattern.DataDerivType ddt) {
    return this;
  }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType copy() {
    return this;
  }
}
