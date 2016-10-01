package com.thaiopensource.suggest.relaxng.pattern;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeException;

import java.util.List;

// invariant: if return is not notAllowed, then no failures are added to fail
class DataDerivFunction extends AbstractPatternFunction<com.thaiopensource.suggest.relaxng.pattern.Pattern> {
  private final ValidatorPatternBuilder builder;
  private final ValidationContext vc;
  private final String str;
  private final List<com.thaiopensource.suggest.relaxng.pattern.DataDerivFailure> fail;

  DataDerivFunction(String str, ValidationContext vc, ValidatorPatternBuilder builder, List<com.thaiopensource.suggest.relaxng.pattern.DataDerivFailure> fail) {
    this.str = str;
    this.vc = vc;
    this.builder = builder;
    this.fail = fail;
  }

  static boolean isBlank(String str) {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	break;
      default:
	return false;
      }
    }
    return true;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseText(com.thaiopensource.suggest.relaxng.pattern.TextPattern p) {
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseRef(RefPattern p) {
    return memoApply(p.getPattern());
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseList(com.thaiopensource.suggest.relaxng.pattern.ListPattern p) {
    int len = str.length();
    int tokenIndex = 0;
    int tokenStart = -1;
    com.thaiopensource.suggest.relaxng.pattern.PatternMemo memo = builder.getPatternMemo(p.getOperand());
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	if (tokenStart >= 0) {
	  memo = tokenDeriv(memo, tokenIndex++, tokenStart, i);
	  tokenStart = -1;
	}
	break;
      default:
	if (tokenStart < 0)
	  tokenStart = i;
	break;
      }
    }
    if (tokenStart >= 0)
      memo = tokenDeriv(memo, tokenIndex++, tokenStart, len);
    if (memo.getPattern().isNullable())
      return builder.makeEmpty();
    if (memo.isNotAllowed())
      return memo.getPattern();
    // pseudo-token to try and force some failures
    tokenDeriv(memo, tokenIndex, len, len);
    // XXX handle the case where this didn't produce any failures
    return builder.makeNotAllowed();
  }

  private com.thaiopensource.suggest.relaxng.pattern.PatternMemo tokenDeriv(com.thaiopensource.suggest.relaxng.pattern.PatternMemo p, int tokenIndex, int start, int end) {
    int failStartSize = failSize();
    PatternMemo deriv = p.dataDeriv(str.substring(start, end), vc, fail);
    if (fail != null && deriv.isNotAllowed()) {
      for (int i = fail.size() - 1; i >= failStartSize; --i)
        fail.get(i).setToken(tokenIndex, start, end);
    }
    return deriv;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseValue(ValuePattern p) {
    Datatype dt = p.getDatatype();
    Object value = dt.createValue(str, vc);
    if (value != null && dt.sameValue(p.getValue(), value))
      return builder.makeEmpty();
    if (fail != null) {
      if (value == null) {
        try {
          dt.checkValid(str, vc);
        }
        catch (DatatypeException e) {
          fail.add(new com.thaiopensource.suggest.relaxng.pattern.DataDerivFailure(dt, p.getDatatypeName(), e));
        }
      }
      else
        fail.add(new com.thaiopensource.suggest.relaxng.pattern.DataDerivFailure(p));
    }
    return builder.makeNotAllowed();
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseData(com.thaiopensource.suggest.relaxng.pattern.DataPattern p) {
    if (p.allowsAnyString())
      return builder.makeEmpty();
    if (fail != null) {
      try {
        p.getDatatype().checkValid(str, vc);
        return builder.makeEmpty();
      }
      catch (DatatypeException e) {
        fail.add(new com.thaiopensource.suggest.relaxng.pattern.DataDerivFailure(p, e));
        return builder.makeNotAllowed();
      }
    }
    if (p.getDatatype().isValid(str, vc))
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseDataExcept(DataExceptPattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern tem = caseData(p);
    if (tem.isNullable() && memoApply(p.getExcept()).isNullable()) {
      if (fail != null)
        fail.add(new DataDerivFailure(p));
      return builder.makeNotAllowed();
    }
    return tem;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseAfter(com.thaiopensource.suggest.relaxng.pattern.AfterPattern p) {
    com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final int failStartSize = failSize();
    if (memoApplyWithFailure(p1).isNullable())
      return p.getOperand2();
    if (p1.isNullable() && isBlank(str)) {
      clearFailures(failStartSize);
      return p.getOperand2();
    }
    return builder.makeNotAllowed();
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
    final int failStartSize = failSize();
    com.thaiopensource.suggest.relaxng.pattern.Pattern tem = builder.makeChoice(memoApplyWithFailure(p.getOperand1()),
		  	             memoApplyWithFailure(p.getOperand2()));
    if (!tem.isNotAllowed())
      clearFailures(failStartSize);
    return tem;
  }
  
  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
    final int failStartSize = failSize();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    com.thaiopensource.suggest.relaxng.pattern.Pattern tem = builder.makeGroup(memoApplyWithFailure(p1), p2);
    if (p1.isNullable())
      tem = builder.makeChoice(tem, memoApplyWithFailure(p2));
    if (!tem.isNotAllowed())
      clearFailures(failStartSize);
    return tem;
  }

  // list//interleave is prohibited, so I don't think this can happen
  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseInterleave(InterleavePattern p) {
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p1 = p.getOperand1();
    final com.thaiopensource.suggest.relaxng.pattern.Pattern p2 = p.getOperand2();
    return builder.makeChoice(builder.makeInterleave(memoApply(p1), p2),
			      builder.makeInterleave(p1, memoApply(p2)));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOneOrMore(com.thaiopensource.suggest.relaxng.pattern.OneOrMorePattern p) {
    return builder.makeGroup(memoApplyWithFailure(p.getOperand()),
			     builder.makeOptional(p));
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    return builder.makeNotAllowed();
  }

  private com.thaiopensource.suggest.relaxng.pattern.Pattern memoApply(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
    return builder.getPatternMemo(p).dataDeriv(str, vc).getPattern();
  }

  private com.thaiopensource.suggest.relaxng.pattern.Pattern memoApplyWithFailure(Pattern p) {
    return builder.getPatternMemo(p).dataDeriv(str, vc, fail).getPattern();
  }

  private int failSize() {
    return fail == null ? 0 : fail.size(); 
  }

  private void clearFailures(int failStartSize) {
    if (fail != null) {
      for (int i = fail.size() - 1; i >= failStartSize; --i)
        fail.remove(i);
    }
  }
}
