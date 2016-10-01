package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

/**
 * PatternFunction to compute the name class of possible attributes.
 * Computes a NormalizedNameClass.
 */
class PossibleAttributeNameSuggestionsFunction extends com.thaiopensource.suggest.relaxng.pattern.PossibleNameSuggestionsFunction {
  public VoidValue caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
    add(p, p.getNameClass());
    return VoidValue.VOID;
  }

  public VoidValue caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    return caseBinary(p);
  }
}
