package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;

import java.util.List;

class DataExceptPattern extends com.thaiopensource.suggest.relaxng.pattern.DataPattern {
  private final com.thaiopensource.suggest.relaxng.pattern.Pattern except;
  private final SourceLocation loc;

  DataExceptPattern(Datatype dt, Name dtName, List<String> params, com.thaiopensource.suggest.relaxng.pattern.Pattern except, SourceLocation loc) {
    super(dt, dtName, params);
    this.except = except;
    this.loc = loc;
  }

  boolean samePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern other) {
    if (!super.samePattern(other))
      return false;
    return except.samePattern(((DataExceptPattern)other).except);
  }

  <T> T apply(com.thaiopensource.suggest.relaxng.pattern.PatternFunction<T> f) {
    return f.caseDataExcept(this);
  }

  public void checkRestrictions(int context, DuplicateAttributeDetector dad, com.thaiopensource.suggest.relaxng.pattern.Alphabet alpha)
    throws RestrictionViolationException {
    super.checkRestrictions(context, dad, alpha);
    try {
      except.checkRestrictions(DATA_EXCEPT_CONTEXT, null, null);
    }
    catch (RestrictionViolationException e) {
      e.maybeSetLocator(loc);
      throw e;
    }
  }

  Pattern getExcept() {
    return except;
  }
}
