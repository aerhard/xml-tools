package com.thaiopensource.suggest.relaxng.pattern;

import java.util.ArrayList;
import java.util.List;

class DuplicateAttributeDetector {
  private final List<com.thaiopensource.suggest.relaxng.pattern.NameClass> nameClasses = new ArrayList<com.thaiopensource.suggest.relaxng.pattern.NameClass>();
  private Alternative alternatives = null;

  private static class Alternative {
    private final int startIndex;
    private int endIndex;
    private final Alternative parent;

    private Alternative(int startIndex, Alternative parent) {
      this.startIndex = startIndex;
      this.endIndex = startIndex;
      this.parent = parent;
    }
  }

  void addAttribute(com.thaiopensource.suggest.relaxng.pattern.NameClass nc) throws RestrictionViolationException {
    int lim = nameClasses.size();
    for (Alternative a = alternatives; a != null; a = a.parent) {
      for (int i = a.endIndex; i < lim; i++)
	checkAttributeOverlap(nc, nameClasses.get(i));
      lim = a.startIndex;
    }
    for (int i = 0; i < lim; i++)
      checkAttributeOverlap(nc, nameClasses.get(i));
    nameClasses.add(nc);
  }

  static private void checkAttributeOverlap(com.thaiopensource.suggest.relaxng.pattern.NameClass nc1, com.thaiopensource.suggest.relaxng.pattern.NameClass nc2) throws RestrictionViolationException {
    com.thaiopensource.suggest.relaxng.pattern.OverlapDetector.checkOverlap(nc1, nc2,
                                 "duplicate_attribute_name",
                                 "duplicate_attribute_ns",
                                 "duplicate_attribute");
  }
  
  void startChoice() {
    alternatives = new Alternative(nameClasses.size(), alternatives);
  }

  void alternative() {
    alternatives.endIndex = nameClasses.size();
  }

  void endChoice() {
    alternatives = alternatives.parent;
  }

}
