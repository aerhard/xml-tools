package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the normalized intersection of zero or more name classes.
 */
public class IntersectionNameClassNormalizer extends com.thaiopensource.suggest.relaxng.pattern.AbstractNameClassNormalizer {
  private final List<com.thaiopensource.suggest.relaxng.pattern.NameClass> nameClasses = new ArrayList<com.thaiopensource.suggest.relaxng.pattern.NameClass>();

  public void add(com.thaiopensource.suggest.relaxng.pattern.NameClass nc) {
    nameClasses.add(nc);
  }

  protected void accept(NameClassVisitor visitor) {
    for (com.thaiopensource.suggest.relaxng.pattern.NameClass nameClass : nameClasses)
      (nameClass).accept(visitor);
  }

  protected boolean contains(Name name) {
    Iterator<NameClass> iter = nameClasses.iterator();
    if (!iter.hasNext())
      return false;
    for (;;) {
      if (!(iter.next()).contains(name))
        return false;
      if (!iter.hasNext())
        break;
    }
    return true;
  }
}
