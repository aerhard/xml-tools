package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidatorPatternBuilder extends com.thaiopensource.suggest.relaxng.pattern.PatternBuilder {
  private final Map<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.PatternMemo> patternMemoMap = new HashMap<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.PatternMemo>();
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> endAttributesFunction;
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> ignoreMissingAttributesFunction;
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> endTagDerivFunction;
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> mixedTextDerivFunction;
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> textOnlyFunction;
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> recoverAfterFunction;
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.DataDerivType> dataDerivTypeFunction;

  private final Map<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.Pattern> choiceMap = new HashMap<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.Pattern>();
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> removeChoicesFunction = new RemoveChoicesFunction();
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<VoidValue> noteChoicesFunction = new NoteChoicesFunction();
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<Set<Name>> requiredElementsFunction = new RequiredElementsFunction();
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<Set<Name>> requiredAttributesFunction = new RequiredAttributesFunction();
  private final com.thaiopensource.suggest.relaxng.pattern.PossibleNamesFunction possibleStartTagNamesFunction = new PossibleStartTagNamesFunction();
  private final com.thaiopensource.suggest.relaxng.pattern.PossibleNamesFunction possibleAttributeNamesFunction = new PossibleAttributeNamesFunction();

  private class NoteChoicesFunction extends com.thaiopensource.suggest.relaxng.pattern.AbstractPatternFunction<VoidValue> {
    public VoidValue caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
      choiceMap.put(p, p);
      return VoidValue.VOID;
    }

    public VoidValue caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      return VoidValue.VOID;
    }
  }

  private class RemoveChoicesFunction extends com.thaiopensource.suggest.relaxng.pattern.AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
      if (choiceMap.get(p) != null)
        return notAllowed;
      return p;
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
      com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1().apply(this);
      com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2().apply(this);
      if (p1 == p.getOperand1() && p2 == p.getOperand2())
        return p;
      if (p1 == notAllowed)
        return p2;
      if (p2 == notAllowed)
        return p1;
      com.thaiopensource.suggest.relaxng.pattern.Pattern p3 = new com.thaiopensource.suggest.relaxng.pattern.ChoicePattern(p1, p2);
      return interner.intern(p3);
    }
  }

  public ValidatorPatternBuilder(PatternBuilder builder) {
    super(builder);
    endAttributesFunction = new com.thaiopensource.suggest.relaxng.pattern.EndAttributesFunction(this);
    ignoreMissingAttributesFunction = new IgnoreMissingAttributesFunction(this);
    endTagDerivFunction = new com.thaiopensource.suggest.relaxng.pattern.EndTagDerivFunction(this);
    mixedTextDerivFunction = new com.thaiopensource.suggest.relaxng.pattern.MixedTextDerivFunction(this);
    textOnlyFunction = new com.thaiopensource.suggest.relaxng.pattern.TextOnlyFunction(this);
    recoverAfterFunction = new com.thaiopensource.suggest.relaxng.pattern.RecoverAfterFunction(this);
    dataDerivTypeFunction = new com.thaiopensource.suggest.relaxng.pattern.DataDerivTypeFunction(this);
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo getPatternMemo(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    com.thaiopensource.suggest.relaxng.pattern.PatternMemo memo = patternMemoMap.get(p);
    if (memo == null) {
      memo = new PatternMemo(p, this);
      patternMemoMap.put(p, memo);
    }
    return memo;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> getEndAttributesFunction() {
    return endAttributesFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> getIgnoreMissingAttributesFunction() {
    return ignoreMissingAttributesFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<Set<Name>> getRequiredElementsFunction() {
    return requiredElementsFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<Set<Name>> getRequiredAttributesFunction() {
    return requiredAttributesFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PossibleNamesFunction getPossibleStartTagNamesFunction() {
    return possibleStartTagNamesFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PossibleNamesFunction getPossibleAttributeNamesFunction() {
    return possibleAttributeNamesFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> getEndTagDerivFunction() {
    return endTagDerivFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> getMixedTextDerivFunction() {
    return mixedTextDerivFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> getTextOnlyFunction() {
    return textOnlyFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> getRecoverAfterFunction() {
    return recoverAfterFunction;
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternFunction<com.thaiopensource.suggest.relaxng.pattern.DataDerivType> getDataDerivTypeFunction() {
    return dataDerivTypeFunction;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeAfter(com.thaiopensource.suggest.relaxng.pattern.Pattern p1, com.thaiopensource.suggest.relaxng.pattern.Pattern p2) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p = new com.thaiopensource.suggest.relaxng.pattern.AfterPattern(p1, p2);
    return interner.intern(p);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeChoice(com.thaiopensource.suggest.relaxng.pattern.Pattern p1, Pattern p2) {
    if (p1 == p2)
      return p1;
    if (p1 == notAllowed)
      return p2;
    if (p2 == notAllowed)
      return p1;
    if (!(p1 instanceof com.thaiopensource.suggest.relaxng.pattern.ChoicePattern)) {
      if (p2.containsChoice(p1))
        return p2;
    }
    else if (!(p2 instanceof com.thaiopensource.suggest.relaxng.pattern.ChoicePattern)) {
      if (p1.containsChoice(p2))
        return p1;
    }
    else {
      p1.apply(noteChoicesFunction);
      p2 = p2.apply(removeChoicesFunction);
      if (choiceMap.size() > 0)
        choiceMap.clear();
      if (p2 == notAllowed)
        return p1;
    }
    if (p1 instanceof com.thaiopensource.suggest.relaxng.pattern.AfterPattern && p2 instanceof com.thaiopensource.suggest.relaxng.pattern.AfterPattern) {
      com.thaiopensource.suggest.relaxng.pattern.AfterPattern ap1 = (com.thaiopensource.suggest.relaxng.pattern.AfterPattern)p1;
      com.thaiopensource.suggest.relaxng.pattern.AfterPattern ap2 = (com.thaiopensource.suggest.relaxng.pattern.AfterPattern)p2;
      if (ap1.getOperand1() == ap2.getOperand1())
        return makeAfter(ap1.getOperand1(), makeChoice(ap1.getOperand2(), ap2.getOperand2()));
      if (ap1.getOperand1() == notAllowed)
        return ap2;
      if (ap2.getOperand1() == notAllowed)
        return ap1;
      if (ap1.getOperand2() == ap2.getOperand2())
        return makeAfter(makeChoice(ap1.getOperand1(), ap2.getOperand1()), ap1.getOperand2());
    }
    return super.makeChoice(p1, p2);
  }
}
