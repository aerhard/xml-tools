package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

class StartTagOpenDerivFunction extends com.thaiopensource.suggest.relaxng.pattern.AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
  private final Name name;
  private final com.thaiopensource.suggest.relaxng.pattern.ValidatorPatternBuilder builder;

  StartTagOpenDerivFunction(Name name, com.thaiopensource.suggest.relaxng.pattern.ValidatorPatternBuilder builder) {
    this.name = name;
    this.builder = builder;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    return builder.makeChoice(memoApply(p.getOperand1()),
			      memoApply(p.getOperand2()));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    com.thaiopensource.suggest.relaxng.pattern.Pattern tem = memoApply(p1).apply(new com.thaiopensource.suggest.relaxng.pattern.ApplyAfterFunction(builder) {
      com.thaiopensource.suggest.relaxng.pattern.Pattern apply(com.thaiopensource.suggest.relaxng.pattern.Pattern x) {
        return builder.makeGroup(x, p2);
      }
    });
    return p1.isNullable() ? builder.makeChoice(tem, memoApply(p2)) : tem;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseInterleave(com.thaiopensource.suggest.relaxng.pattern.InterleavePattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    return builder.makeChoice(
            memoApply(p1).apply(new com.thaiopensource.suggest.relaxng.pattern.ApplyAfterFunction(builder) {
              com.thaiopensource.suggest.relaxng.pattern.Pattern apply(com.thaiopensource.suggest.relaxng.pattern.Pattern x) {
                return builder.makeInterleave(x, p2);
              }
            }),
            memoApply(p2).apply(new com.thaiopensource.suggest.relaxng.pattern.ApplyAfterFunction(builder) {
              com.thaiopensource.suggest.relaxng.pattern.Pattern apply(com.thaiopensource.suggest.relaxng.pattern.Pattern x) {
                return builder.makeInterleave(p1, x);
              }
            }));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    return memoApply(p1).apply(new com.thaiopensource.suggest.relaxng.pattern.ApplyAfterFunction(builder) {
				   com.thaiopensource.suggest.relaxng.pattern.Pattern apply(com.thaiopensource.suggest.relaxng.pattern.Pattern x) {
				     return builder.makeAfter(x, p2);
				   }
				 });
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOneOrMore(final com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand();
    return memoApply(p1).apply(new com.thaiopensource.suggest.relaxng.pattern.ApplyAfterFunction(builder) {
				   com.thaiopensource.suggest.relaxng.pattern.Pattern apply(com.thaiopensource.suggest.relaxng.pattern.Pattern x) {
				     return builder.makeGroup(x,
							      builder.makeOptional(p));
				   }
				 });
  }


  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseElement(ElementPattern p) {
    if (!p.getNameClass().contains(name))
      return builder.makeNotAllowed();
    return builder.makeAfter(p.getContent(), builder.makeEmpty());
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    return builder.makeNotAllowed();
  }

  final com.thaiopensource.suggest.relaxng.pattern.Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  com.thaiopensource.suggest.relaxng.pattern.PatternMemo apply(PatternMemo memo) {
    return memo.startTagOpenDeriv(this);
  }

  Name getName() {
    return name;
  }

  ValidatorPatternBuilder getPatternBuilder() {
    return builder;
  }
}
