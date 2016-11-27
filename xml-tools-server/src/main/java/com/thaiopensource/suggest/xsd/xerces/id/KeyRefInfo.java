package com.thaiopensource.suggest.xsd.xerces.id;

import java.util.Set;

public class KeyRefInfo {

  private final int elementIndex;
  private final Set<Integer> fieldIndices;

  public KeyRefInfo(int elementIndex, Set<Integer> fieldIndices) {
    this.elementIndex = elementIndex;
    this.fieldIndices = fieldIndices;
  }

  public int getElementIndex() {
    return elementIndex;
  }

  public Set<Integer> getFieldIndices() {
    return fieldIndices;
  }
}
