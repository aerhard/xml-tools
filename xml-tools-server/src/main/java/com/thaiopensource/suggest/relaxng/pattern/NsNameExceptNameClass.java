package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

public class NsNameExceptNameClass extends com.thaiopensource.suggest.relaxng.pattern.NameClass {

  private final com.thaiopensource.suggest.relaxng.pattern.NameClass nameClass;
  private final String namespaceURI;

  public NsNameExceptNameClass(String namespaceURI, com.thaiopensource.suggest.relaxng.pattern.NameClass nameClass) {
    this.namespaceURI = namespaceURI;
    this.nameClass = nameClass;
  }

  public boolean contains(Name name) {
    return (this.namespaceURI.equals(name.getNamespaceUri())
	    && !nameClass.contains(name));
  }

  public int containsSpecificity(Name name) {
    return contains(name) ? SPECIFICITY_NS_NAME : SPECIFICITY_NONE;
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NsNameExceptNameClass))
      return false;
    NsNameExceptNameClass other = (NsNameExceptNameClass)obj;
    return (namespaceURI.equals(other.namespaceURI)
	    && nameClass.equals(other.nameClass));
  }

  public int hashCode() {
    return namespaceURI.hashCode() ^ nameClass.hashCode();
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitNsNameExcept(namespaceURI, nameClass);
  }

  public boolean isOpen() {
    return true;
  }
}

