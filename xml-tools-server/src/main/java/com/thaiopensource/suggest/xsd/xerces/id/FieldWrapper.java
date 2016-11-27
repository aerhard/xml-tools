package com.thaiopensource.suggest.xsd.xerces.id;

import org.apache.xerces.impl.xs.identity.Field;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.identity.ValueStore;
import org.apache.xerces.impl.xs.identity.XPathMatcher;
import org.apache.xerces.xs.ShortList;

public class FieldWrapper extends Field {

  public FieldWrapper(XPath xpath,
                      IdentityConstraint identityConstraint) {
    super(xpath, identityConstraint);
  }

  public XPathMatcher createMatcher(ValueStore store) {
    return new Matcher(fXPath, store);
  }

  public class Matcher extends Field.Matcher {
    public Matcher(XPath xpath, ValueStore store) {
      super(xpath, store);
    }

    public ValueStore getValueStore() {
      return fStore;
    }

    public FieldWrapper getField() {
      return FieldWrapper.this;
    }

    protected void matched(Object actualValue, short valueType, ShortList itemValueType, boolean isNil) {
      super.matched(actualValue, valueType, itemValueType, isNil);
      fMayMatch = true;
    }
  }
}
