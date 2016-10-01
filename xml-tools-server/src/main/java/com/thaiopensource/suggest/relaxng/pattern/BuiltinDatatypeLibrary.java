package com.thaiopensource.suggest.relaxng.pattern;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;

public class BuiltinDatatypeLibrary implements DatatypeLibrary {
  private final DatatypeBuilder tokenDatatypeBuilder
    = new com.thaiopensource.suggest.relaxng.pattern.BuiltinDatatypeBuilder(new com.thaiopensource.suggest.relaxng.pattern.TokenDatatype());
  private final DatatypeBuilder stringDatatypeBuilder
    = new com.thaiopensource.suggest.relaxng.pattern.BuiltinDatatypeBuilder(new com.thaiopensource.suggest.relaxng.pattern.StringDatatype());
  public DatatypeBuilder createDatatypeBuilder(String type)
    throws DatatypeException {
    if (type.equals("token"))
      return tokenDatatypeBuilder;
    else if (type.equals("string"))
      return stringDatatypeBuilder;
    throw new DatatypeException();
  }
  public Datatype createDatatype(String type) throws DatatypeException {
    return createDatatypeBuilder(type).createDatatype();
  }
}
