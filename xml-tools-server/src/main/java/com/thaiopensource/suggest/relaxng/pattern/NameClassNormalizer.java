package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

/**
 *  Normalizes a name classes.
 */
public class NameClassNormalizer extends AbstractNameClassNormalizer {
  private com.thaiopensource.suggest.relaxng.pattern.NameClass nameClass;

  public NameClassNormalizer(com.thaiopensource.suggest.relaxng.pattern.NameClass nameClass) {
    this.nameClass = nameClass;
  }

  protected boolean contains(Name name) {
    return nameClass.contains(name);
  }

  protected void accept(com.thaiopensource.suggest.relaxng.pattern.NameClassVisitor visitor) {
    nameClass.accept(visitor);
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass getNameClass() {
    return nameClass;
  }

  public void setNameClass(NameClass nameClass) {
    this.nameClass = nameClass;
  }
}
