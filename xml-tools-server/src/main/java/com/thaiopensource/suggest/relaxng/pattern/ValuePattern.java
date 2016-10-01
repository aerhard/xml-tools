package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;

public class ValuePattern extends StringPattern {
  private final Object obj;
  private final Datatype dt;
  private final Name dtName;
  private final String stringValue;

  ValuePattern(Datatype dt, Name dtName, Object obj, String stringValue) {
    super(combineHashCode(VALUE_HASH_CODE, dt.valueHashCode(obj)));
    this.dt = dt;
    this.dtName = dtName;
    this.obj = obj;
    this.stringValue = stringValue;
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    if (!(other instanceof ValuePattern))
      return false;
    ValuePattern vp = (ValuePattern) other;
    return (dt.equals(((ValuePattern)other).dt)
	    && dt.sameValue(obj, ((ValuePattern)other).obj)) &&
        vp.getChildElementAnnotations() == getChildElementAnnotations() &&
        vp.getFollowingElementAnnotations() == getFollowingElementAnnotations();
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseValue(this);
  }

  public void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_value");
    }
  }

  public Datatype getDatatype() {
    return dt;
  }

  public Name getDatatypeName() {
    return dtName;
  }

  public Object getValue() {
    return obj;
  }

  public String getStringValue() {
    return stringValue;
  }
}
