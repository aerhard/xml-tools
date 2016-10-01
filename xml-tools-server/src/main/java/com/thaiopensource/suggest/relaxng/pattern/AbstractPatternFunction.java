package com.thaiopensource.suggest.relaxng.pattern;

abstract class AbstractPatternFunction<T> implements com.thaiopensource.suggest.relaxng.pattern.PatternFunction<T> {
  public T caseEmpty(com.thaiopensource.suggest.relaxng.pattern.EmptyPattern p) {
    return caseOther(p);
  }

  public T caseNotAllowed(com.thaiopensource.suggest.relaxng.pattern.NotAllowedPattern p) {
    return caseOther(p);
  }

  public T caseError(com.thaiopensource.suggest.relaxng.pattern.ErrorPattern p) {
    return caseOther(p);
  }

  public T caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    return caseOther(p);
  }

  public T caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    return caseOther(p);
  }

  public T caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return caseOther(p);
  }

  public T caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    return caseOther(p);
  }

  public T caseElement(ElementPattern p) {
    return caseOther(p);
  }

  public T caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
    return caseOther(p);
  }

  public T caseData(com.thaiopensource.suggest.relaxng.pattern.DataPattern p) {
    return caseOther(p);
  }

  public T caseDataExcept(com.thaiopensource.suggest.relaxng.pattern.DataExceptPattern p) {
    return caseOther(p);
  }

  public T caseValue(ValuePattern p) {
    return caseOther(p);
  }

  public T caseText(com.thaiopensource.suggest.relaxng.pattern.TextPattern p) {
    return caseOther(p);
  }

  public T caseList(com.thaiopensource.suggest.relaxng.pattern.ListPattern p) {
    return caseOther(p);
  }

  public T caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    return caseOther(p);
  }

  public T caseRef(RefPattern p) {
    return caseOther(p);
  }

  public abstract T caseOther(Pattern p);
}
