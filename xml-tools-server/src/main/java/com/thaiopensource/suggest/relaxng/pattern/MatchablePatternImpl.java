package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.match.MatchablePattern;
import com.thaiopensource.relaxng.match.Matcher;

public class MatchablePatternImpl implements MatchablePattern {
  private final SchemaPatternBuilder spb;
  private final com.thaiopensource.suggest.relaxng.pattern.Pattern start;

  public MatchablePatternImpl(SchemaPatternBuilder spb, com.thaiopensource.suggest.relaxng.pattern.Pattern start) {
    this.spb = spb;
    this.start = start;
  }

  public Matcher createMatcher() {
    return new PatternMatcher(start, new ValidatorPatternBuilder(spb));
  }

}
