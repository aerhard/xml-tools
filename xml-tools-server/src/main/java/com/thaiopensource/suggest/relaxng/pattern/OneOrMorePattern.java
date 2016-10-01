package com.thaiopensource.suggest.relaxng.pattern;

import org.xml.sax.SAXException;

class OneOrMorePattern extends com.thaiopensource.suggest.relaxng.pattern.Pattern {
  private final com.thaiopensource.suggest.relaxng.pattern.Pattern p;

  OneOrMorePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    super(p.isNullable(),
	  p.getContentType(),
	  combineHashCode(ONE_OR_MORE_HASH_CODE, p.hashCode()));
    this.p = p;
  }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern expand(SchemaPatternBuilder b) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeOneOrMore(ep);
    else
      return this;
  }

  public void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  public void checkRestrictions(int context, DuplicateAttributeDetector dad, com.thaiopensource.suggest.relaxng.pattern.Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_one_or_more");
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_one_or_more");
    }
    
    p.checkRestrictions(context == ELEMENT_CONTEXT
			? ELEMENT_REPEAT_CONTEXT
			: context,
			dad,
			alpha);
    if (context != LIST_CONTEXT
	&& !contentTypeGroupable(p.getContentType(), p.getContentType()))
      throw new RestrictionViolationException("one_or_more_string");
  }

  boolean samePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern other) {
    return (other instanceof OneOrMorePattern
	    && p == ((OneOrMorePattern)other).p);
  }

  <T> T apply(com.thaiopensource.suggest.relaxng.pattern.PatternFunction<T> f) {
    return f.caseOneOrMore(this);
  }

  Pattern getOperand() {
    return p;
  }
}
