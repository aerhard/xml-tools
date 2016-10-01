package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import org.xml.sax.SAXException;

public class ElementPattern extends Pattern {
  private Pattern p;
  private final NameClass origNameClass;
  private NameClass nameClass;
  private boolean expanded = false;
  private boolean checkedRestrictions = false;
  private final SourceLocation loc;

  public ElementPattern(NameClass nameClass, Pattern p, SourceLocation loc) {
    super(false,
	  ELEMENT_CONTENT_TYPE,
	  combineHashCode(ELEMENT_HASH_CODE,
			  nameClass.hashCode(),
			  p.hashCode()));
    this.nameClass = nameClass;
    this.origNameClass = nameClass;
    this.p = p;
    this.loc = loc;
  }

  public void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    if (alpha != null)
      alpha.addElement(origNameClass);
    if (checkedRestrictions)
      return;
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_element");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_element");
    case ATTRIBUTE_CONTEXT:
      throw new RestrictionViolationException("attribute_contains_element");
    }
    checkedRestrictions = true;
    try {
      p.checkRestrictions(ELEMENT_CONTEXT, new DuplicateAttributeDetector(), null);
    }
    catch (RestrictionViolationException e) {
      checkedRestrictions = false;
      e.maybeSetLocator(loc);
      throw e;
    }
  }

    public Pattern expand(SchemaPatternBuilder b) {
    if (!expanded) {
      expanded = true;
      p = p.expand(b);
      if (p.isNotAllowed())
	nameClass = new NullNameClass();
    }
    return this;
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof ElementPattern))
      return false;
    ElementPattern ep = (ElementPattern)other;
    return nameClass.equals(ep.nameClass) && p == ep.p &&
        ep.getChildElementAnnotations() == getChildElementAnnotations() &&
        ep.getFollowingElementAnnotations() == getFollowingElementAnnotations();
  }

  public void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth + 1);
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseElement(this);
  }

  void setContent(Pattern p) {
    this.p = p;
  }

  Pattern getContent() {
    return p;
  }

  public NameClass getNameClass() {
    return nameClass;
  }

  SourceLocation getLocator() {
    return loc;
  }
}
