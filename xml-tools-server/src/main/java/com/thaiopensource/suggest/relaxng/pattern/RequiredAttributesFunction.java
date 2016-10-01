package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.Set;

/**
 * Implements a function on a pattern that returns the set of required attributes.
 * The return value is a non-null Set each member of is a non-null Name. Note that
 * in the schema attribute foo|bar { text }, neither foo nor bar are required attributes.
 */
class RequiredAttributesFunction extends RequiredElementsOrAttributesFunction {
  public Set<Name> caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
    return caseNamed(p.getNameClass());
  }

  public Set<Name> caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    return union(p);
  }
}
