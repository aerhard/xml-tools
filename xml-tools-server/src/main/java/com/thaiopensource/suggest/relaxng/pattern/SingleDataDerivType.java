package com.thaiopensource.suggest.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

import java.util.List;

/**
 * DerivType for a Pattern whose derivative wrt any data is always the same.
 */
class SingleDataDerivType extends com.thaiopensource.suggest.relaxng.pattern.DataDerivType {
  private com.thaiopensource.suggest.relaxng.pattern.PatternMemo memo;

  SingleDataDerivType() { }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                                                                   List<DataDerivFailure> fail) {
    if (memo == null)
      // this type never adds any failures
      memo = super.dataDeriv(builder, p, str, vc, null);
    return memo;
  }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType copy() {
    return new SingleDataDerivType();
  }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType combine(com.thaiopensource.suggest.relaxng.pattern.DataDerivType ddt) {
    return ddt;
  }
}