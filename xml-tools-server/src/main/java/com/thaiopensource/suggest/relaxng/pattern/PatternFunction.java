package com.thaiopensource.suggest.relaxng.pattern;

interface PatternFunction<T> {
  T caseEmpty(com.thaiopensource.suggest.relaxng.pattern.EmptyPattern p);
  T caseNotAllowed(NotAllowedPattern p);
  T caseError(com.thaiopensource.suggest.relaxng.pattern.ErrorPattern p);
  T caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p);
  T caseInterleave(InterleavePattern p);
  T caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p);
  T caseOneOrMore(OneOrMorePattern p);
  T caseElement(ElementPattern p);
  T caseAttribute(AttributePattern p);
  T caseData(com.thaiopensource.suggest.relaxng.pattern.DataPattern p);
  T caseDataExcept(DataExceptPattern p);
  T caseValue(ValuePattern p);
  T caseText(com.thaiopensource.suggest.relaxng.pattern.TextPattern p);
  T caseList(com.thaiopensource.suggest.relaxng.pattern.ListPattern p);
  T caseRef(RefPattern p);
  T caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p);
}
