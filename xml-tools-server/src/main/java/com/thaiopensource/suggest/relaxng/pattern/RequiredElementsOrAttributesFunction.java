package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Common functionality between RequiredAttributesFunction and RequiredElementsFunction
 */
abstract class RequiredElementsOrAttributesFunction extends com.thaiopensource.suggest.relaxng.pattern.AbstractPatternFunction<Set<Name>> {
  public Set<Name> caseOther(Pattern p) {
    return Collections.emptySet();
  }

  public Set<Name> caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    Set<Name> s1 = p.getOperand1().apply(this);
    Set<Name> s2 = p.getOperand2().apply(this);
    if (s1.isEmpty())
      return s1;
    if (s2.isEmpty())
      return s2;
    s1.retainAll(s2);
    return s1;
  }

  protected Set<Name> caseNamed(NameClass nc) {
    if (!(nc instanceof com.thaiopensource.suggest.relaxng.pattern.SimpleNameClass))
      return Collections.emptySet();
    Set<Name> s = new HashSet<Name>();
    s.add(((SimpleNameClass)nc).getName());
    return s;
  }

  protected Set<Name> union(com.thaiopensource.suggest.relaxng.pattern.BinaryPattern p) {
    Set<Name> s1 = p.getOperand1().apply(this);
    Set<Name> s2 = p.getOperand2().apply(this);
    if (s1.isEmpty())
      return s2;
    if (s2.isEmpty())
      return s1;
    s1.addAll(s2);
    return s1;
  }

  public Set<Name> caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    return union(p);
  }

  public Set<Name> caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public Set<Name> caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }
}
