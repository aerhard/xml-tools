package com.thaiopensource.suggest.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

import java.util.List;

abstract class DataDerivType {
  abstract DataDerivType copy();
  abstract DataDerivType combine(DataDerivType ddt);
  com.thaiopensource.suggest.relaxng.pattern.PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                                                                   List<DataDerivFailure> fail) {
    return builder.getPatternMemo(p.apply(new DataDerivFunction(str, vc, builder, fail)));
  }
}
