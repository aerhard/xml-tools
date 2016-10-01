package com.thaiopensource.suggest.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

import java.util.List;

class BlankDataDerivType extends com.thaiopensource.suggest.relaxng.pattern.DataDerivType {
  private com.thaiopensource.suggest.relaxng.pattern.PatternMemo blankMemo;
  private com.thaiopensource.suggest.relaxng.pattern.PatternMemo nonBlankMemo;

  BlankDataDerivType() { }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                                                                   List<DataDerivFailure> fail) {
    if (com.thaiopensource.suggest.relaxng.pattern.DataDerivFunction.isBlank(str)) {
      if (blankMemo == null || (fail != null && blankMemo.isNotAllowed()))
        blankMemo = super.dataDeriv(builder, p, str, vc, fail);
      return blankMemo;
    }
    else {
      if (nonBlankMemo == null || (fail != null && nonBlankMemo.isNotAllowed()))
        nonBlankMemo = super.dataDeriv(builder, p, str, vc, fail);
      return nonBlankMemo;
    }
  }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType copy() {
    return new BlankDataDerivType();
  }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType combine(com.thaiopensource.suggest.relaxng.pattern.DataDerivType ddt) {
    if (ddt instanceof BlankDataDerivType || ddt instanceof SingleDataDerivType)
      return this;
    return InconsistentDataDerivType.getInstance();
  }
}