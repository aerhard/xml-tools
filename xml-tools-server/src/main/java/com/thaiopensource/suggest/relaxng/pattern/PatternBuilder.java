package com.thaiopensource.suggest.relaxng.pattern;

public class PatternBuilder {
  private final com.thaiopensource.suggest.relaxng.pattern.EmptyPattern empty;
  protected final NotAllowedPattern notAllowed;
  protected final PatternInterner interner;

  public PatternBuilder() {
    empty = new com.thaiopensource.suggest.relaxng.pattern.EmptyPattern();
    notAllowed = new NotAllowedPattern();
    interner = new PatternInterner();
  }

  public PatternBuilder(PatternBuilder parent) {
    empty = parent.empty;
    notAllowed = parent.notAllowed;
    interner = new PatternInterner(parent.interner);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeEmpty() {
    return empty;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeNotAllowed() {
    return notAllowed;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeGroup(com.thaiopensource.suggest.relaxng.pattern.Pattern p1, com.thaiopensource.suggest.relaxng.pattern.Pattern p2) {
    if (p1 == empty)
      return p2;
    if (p2 == empty)
      return p1;
    if (p1 == notAllowed || p2 == notAllowed)
      return notAllowed;
    if (false && p1 instanceof com.thaiopensource.suggest.relaxng.pattern.GroupPattern) {
      com.thaiopensource.suggest.relaxng.pattern.GroupPattern sp = (com.thaiopensource.suggest.relaxng.pattern.GroupPattern)p1;
      return makeGroup(sp.p1, makeGroup(sp.p2, p2));
    }
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new com.thaiopensource.suggest.relaxng.pattern.GroupPattern(p1, p2);
    return interner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeInterleave(com.thaiopensource.suggest.relaxng.pattern.Pattern p1, com.thaiopensource.suggest.relaxng.pattern.Pattern p2) {
    if (p1 == empty)
      return p2;
    if (p2 == empty)
      return p1;
    if (p1 == notAllowed || p2 == notAllowed)
      return notAllowed;
    if (false && p1 instanceof InterleavePattern) {
      InterleavePattern ip = (InterleavePattern)p1;
      return makeInterleave(ip.p1, makeInterleave(ip.p2, p2));
    }
    if (false) {
    if (p2 instanceof InterleavePattern) {
      InterleavePattern ip = (InterleavePattern)p2;
      if (p1.hashCode() > ip.p1.hashCode())
	return makeInterleave(ip.p1, makeInterleave(p1, ip.p2));
    }
    else if (p1.hashCode() > p2.hashCode())
      return makeInterleave(p2, p1);
    }
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new InterleavePattern(p1, p2);
    return interner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeChoice(com.thaiopensource.suggest.relaxng.pattern.Pattern p1, com.thaiopensource.suggest.relaxng.pattern.Pattern p2) {
    if (p1 == empty && p2.isNullable())
      return p2;
    if (p2 == empty && p1.isNullable())
      return p1;
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new com.thaiopensource.suggest.relaxng.pattern.ChoicePattern(p1, p2);
    return interner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeOneOrMore(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    if (p == empty
	|| p == notAllowed
	|| p instanceof OneOrMorePattern)
      return p;
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = new OneOrMorePattern(p);
    return interner.intern(p1);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeOptional(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    return makeChoice(p, empty);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeZeroOrMore(Pattern p) {
    return makeOptional(makeOneOrMore(p));
  }
}
