package com.thaiopensource.suggest.relaxng.pattern;

class InterleavePattern extends com.thaiopensource.suggest.relaxng.pattern.BinaryPattern {
  InterleavePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern p1, com.thaiopensource.suggest.relaxng.pattern.Pattern p2) {
    super(p1.isNullable() && p2.isNullable(),
	  combineHashCode(INTERLEAVE_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }
    public com.thaiopensource.suggest.relaxng.pattern.Pattern expand(SchemaPatternBuilder b) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern ep1 = p1.expand(b);
    Pattern ep2 = p2.expand(b);
    if (ep1 != p1 || ep2 != p2)
      return b.makeInterleave(ep1, ep2);
    else
      return this;
  }
  public void checkRestrictions(int context, DuplicateAttributeDetector dad, com.thaiopensource.suggest.relaxng.pattern.Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_interleave");
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_interleave");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_interleave");
    }
    if (context == ELEMENT_REPEAT_CONTEXT)
      context = ELEMENT_REPEAT_INTERLEAVE_CONTEXT;
    com.thaiopensource.suggest.relaxng.pattern.Alphabet a1;
    if (alpha != null && alpha.isEmpty())
      a1 = alpha;
    else
      a1 = new com.thaiopensource.suggest.relaxng.pattern.Alphabet();
    p1.checkRestrictions(context, dad, a1);
    if (a1.isEmpty())
      p2.checkRestrictions(context, dad, a1);
    else {
      com.thaiopensource.suggest.relaxng.pattern.Alphabet a2 = new com.thaiopensource.suggest.relaxng.pattern.Alphabet();
      p2.checkRestrictions(context, dad, a2);
      a1.checkOverlap(a2);
      if (alpha != null) {
	if (alpha != a1)
	  alpha.addAlphabet(a1);
	alpha.addAlphabet(a2);
      }
    }
    if (!contentTypeGroupable(p1.getContentType(), p2.getContentType()))
      throw new RestrictionViolationException("interleave_string");
    if (p1.getContentType() == MIXED_CONTENT_TYPE
	&& p2.getContentType() == MIXED_CONTENT_TYPE)
      throw new RestrictionViolationException("interleave_text_overlap");
  }

  <T> T apply(com.thaiopensource.suggest.relaxng.pattern.PatternFunction<T> f) {
    return f.caseInterleave(this);
  }
}
