package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataDerivType for a pattern which is a choice of values of the same datatype.
 */
class ValueDataDerivType extends com.thaiopensource.suggest.relaxng.pattern.DataDerivType {
  private final Datatype dt;
  private final Name dtName;
  private com.thaiopensource.suggest.relaxng.pattern.PatternMemo noValue;
  public Map<com.thaiopensource.suggest.relaxng.pattern.DatatypeValue, com.thaiopensource.suggest.relaxng.pattern.PatternMemo> valueMap;

  ValueDataDerivType(Datatype dt, Name dtName) {
    this.dt = dt;
    this.dtName = dtName;
  }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType copy() {
    return new ValueDataDerivType(dt, dtName);
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo dataDeriv(ValidatorPatternBuilder builder, com.thaiopensource.suggest.relaxng.pattern.Pattern p, String str, ValidationContext vc,
                                                                   List<com.thaiopensource.suggest.relaxng.pattern.DataDerivFailure> fail) {
    Object value = dt.createValue(str, vc);
    if (value == null) {
      if (noValue == null)
        noValue = super.dataDeriv(builder, p, str, vc, fail);
      else if (fail != null && noValue.isNotAllowed()) {
        try {
          dt.checkValid(str, vc);
        }
        catch (DatatypeException e) {
          fail.add(new DataDerivFailure(dt, dtName, e));
        }
      }
      return noValue;
    }
    else {
      com.thaiopensource.suggest.relaxng.pattern.DatatypeValue dtv = new com.thaiopensource.suggest.relaxng.pattern.DatatypeValue(value, dt);
      if (valueMap == null)
        valueMap = new HashMap<com.thaiopensource.suggest.relaxng.pattern.DatatypeValue, com.thaiopensource.suggest.relaxng.pattern.PatternMemo>();
      PatternMemo tem = valueMap.get(dtv);
      if (tem == null) {
        tem = super.dataDeriv(builder, p, str, vc, fail);
        valueMap.put(dtv, tem);
      }
      else if (tem.isNotAllowed() && fail != null)
        super.dataDeriv(builder, p, str, vc, fail);
      return tem;
    }
  }

  com.thaiopensource.suggest.relaxng.pattern.DataDerivType combine(com.thaiopensource.suggest.relaxng.pattern.DataDerivType ddt) {
    if (ddt instanceof ValueDataDerivType) {
      if (((ValueDataDerivType)ddt).dt == this.dt)
        return this;
      else
        return InconsistentDataDerivType.getInstance();
    }
    else
      return ddt.combine(this);
  }

  Datatype getDatatype() {
    return dt;
  }
}
