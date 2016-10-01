package com.thaiopensource.suggest.relaxng.pattern;

/**
 * Normalizes the union of zero or more name classes.
 */
public class UnionNameClassNormalizer extends com.thaiopensource.suggest.relaxng.pattern.NameClassNormalizer {
  public UnionNameClassNormalizer() {
    super(new com.thaiopensource.suggest.relaxng.pattern.NullNameClass());
  }

  public void add(NameClass nameClass) {
    setNameClass(new ChoiceNameClass(getNameClass(), nameClass));
  }
}
