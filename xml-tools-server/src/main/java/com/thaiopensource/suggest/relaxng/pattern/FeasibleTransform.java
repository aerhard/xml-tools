package com.thaiopensource.suggest.relaxng.pattern;

import java.util.HashSet;
import java.util.Set;

public class FeasibleTransform {
  private static class FeasiblePatternFunction extends AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
    private final SchemaPatternBuilder spb;
    private final Set<com.thaiopensource.suggest.relaxng.pattern.ElementPattern> elementDone = new HashSet<com.thaiopensource.suggest.relaxng.pattern.ElementPattern>();

    FeasiblePatternFunction(SchemaPatternBuilder spb) {
      this.spb = spb;
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
      return spb.makeChoice(p.getOperand1().apply(this), p.getOperand2().apply(this));
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
      return spb.makeGroup(p.getOperand1().apply(this), p.getOperand2().apply(this));
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseInterleave(InterleavePattern p) {
      return spb.makeInterleave(p.getOperand1().apply(this), p.getOperand2().apply(this));
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
      return spb.makeOneOrMore(p.getOperand().apply(this));
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseElement(ElementPattern p) {
      if (!elementDone.contains(p)) {
        elementDone.add(p);
        p.setContent(p.getContent().apply(this));
      }
      return spb.makeOptional(p);
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
      return spb.makeOptional(p);
    }
  }

  public static com.thaiopensource.suggest.relaxng.pattern.Pattern transform(SchemaPatternBuilder spb, Pattern p) {
    return p.apply(new FeasiblePatternFunction(spb));
  }
}
