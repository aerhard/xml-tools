package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import org.xml.sax.SAXException;

class ListPattern extends com.thaiopensource.suggest.relaxng.pattern.Pattern {
  private final com.thaiopensource.suggest.relaxng.pattern.Pattern p;
  private final SourceLocation locator;

  ListPattern(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation locator) {
    super(false,
	  DATA_CONTENT_TYPE,
	  combineHashCode(LIST_HASH_CODE, p.hashCode()));
    this.p = p;
    this.locator = locator;
  }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern expand(SchemaPatternBuilder b) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeList(ep, locator);
    else
      return this;
  }

  public void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  boolean samePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern other) {
    return (other instanceof ListPattern
	    && p == ((ListPattern)other).p);
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseList(this);
  }

  public void checkRestrictions(int context, DuplicateAttributeDetector dad, com.thaiopensource.suggest.relaxng.pattern.Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_list");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_list");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_list");
    }
    try {
      p.checkRestrictions(LIST_CONTEXT, dad, null);
    }
    catch (RestrictionViolationException e) {
      e.maybeSetLocator(locator);
      throw e;
    }
  }
  
  Pattern getOperand() {
    return p;
  }
}
