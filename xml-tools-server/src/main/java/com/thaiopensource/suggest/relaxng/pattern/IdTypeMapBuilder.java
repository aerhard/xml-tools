package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class IdTypeMapBuilder {
  private boolean hadError;
  private final ErrorHandler eh;
  private final com.thaiopensource.suggest.relaxng.pattern.PatternFunction<Integer> idTypeFunction = new IdTypeFunction();
  private final IdTypeMapImpl idTypeMap = new IdTypeMapImpl();
  private final Set<com.thaiopensource.suggest.relaxng.pattern.ElementPattern> elementProcessed = new HashSet<com.thaiopensource.suggest.relaxng.pattern.ElementPattern>();
  private final Stack<com.thaiopensource.suggest.relaxng.pattern.ElementPattern> elementsToProcess = new Stack<com.thaiopensource.suggest.relaxng.pattern.ElementPattern>();
  private final List<PossibleConflict> possibleConflicts = new ArrayList<PossibleConflict>();

  private void notePossibleConflict(com.thaiopensource.suggest.relaxng.pattern.NameClass elementNameClass, com.thaiopensource.suggest.relaxng.pattern.NameClass attributeNameClass, SourceLocation loc) {
    possibleConflicts.add(new PossibleConflict(elementNameClass, attributeNameClass, loc));
  }

  private static class WrappedSAXException extends RuntimeException {
    private final SAXException cause;
    WrappedSAXException(SAXException cause) {
      this.cause = cause;
    }
  }

  private static class PossibleConflict {
    private final com.thaiopensource.suggest.relaxng.pattern.NameClass elementNameClass;
    private final com.thaiopensource.suggest.relaxng.pattern.NameClass attributeNameClass;
    private final SourceLocation locator;

    private PossibleConflict(com.thaiopensource.suggest.relaxng.pattern.NameClass elementNameClass, com.thaiopensource.suggest.relaxng.pattern.NameClass attributeNameClass, SourceLocation locator) {
      this.elementNameClass = elementNameClass;
      this.attributeNameClass = attributeNameClass;
      this.locator = locator;
    }
  }

  private static class ScopedName {
    private final Name elementName;
    private final Name attributeName;

    private ScopedName(Name elementName, Name attributeName) {
      this.elementName = elementName;
      this.attributeName = attributeName;
    }

    public int hashCode() {
      return elementName.hashCode() ^ attributeName.hashCode();
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof ScopedName))
        return false;
      ScopedName other = (ScopedName)obj;
      return elementName.equals(other.elementName) && attributeName.equals(other.attributeName);
    }
  }

  private static class IdTypeMapImpl implements com.thaiopensource.suggest.relaxng.pattern.IdTypeMap {
    private final Map<ScopedName, Integer> table = new HashMap<ScopedName, Integer>();
    public int getIdType(Name elementName, Name attributeName) {
      Integer n = table.get(new ScopedName(elementName, attributeName));
      if (n == null)
        return Datatype.ID_TYPE_NULL;
      return n;
    }
    private void add(Name elementName, Name attributeName, int idType) {
      table.put(new ScopedName(elementName, attributeName), idType);
    }
  }

  private class IdTypeFunction extends AbstractPatternFunction<Integer> {
    public Integer caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
      return Datatype.ID_TYPE_NULL;
    }

    public Integer caseData(com.thaiopensource.suggest.relaxng.pattern.DataPattern p) {
      return p.getDatatype().getIdType();
    }

    public Integer caseDataExcept(DataExceptPattern p) {
      return p.getDatatype().getIdType();
    }

    public Integer caseValue(com.thaiopensource.suggest.relaxng.pattern.ValuePattern p) {
      return p.getDatatype().getIdType();
    }
  }

  private class BuildFunction extends AbstractPatternFunction<VoidValue> {
    private final com.thaiopensource.suggest.relaxng.pattern.NameClass elementNameClass;
    private final SourceLocation locator;
    private final boolean attributeIsParent;

    BuildFunction(com.thaiopensource.suggest.relaxng.pattern.NameClass elementNameClass, SourceLocation locator) {
      this.elementNameClass = elementNameClass;
      this.locator = locator;
      this.attributeIsParent = false;
    }

   BuildFunction(com.thaiopensource.suggest.relaxng.pattern.NameClass elementNameClass, SourceLocation locator, boolean attributeIsParent) {
      this.elementNameClass = elementNameClass;
      this.locator = locator;
      this.attributeIsParent = attributeIsParent;
    }

    private BuildFunction down() {
      if (!attributeIsParent)
        return this;
      return new BuildFunction(elementNameClass, locator, false);
    }

    public VoidValue caseChoice(com.thaiopensource.suggest.relaxng.pattern.ChoicePattern p) {
      BuildFunction f = down();
      p.getOperand1().apply(f);
      p.getOperand2().apply(f);
      return VoidValue.VOID;
    }

    public VoidValue caseInterleave(InterleavePattern p) {
      BuildFunction f = down();
      p.getOperand1().apply(f);
      p.getOperand2().apply(f);
      return VoidValue.VOID;
    }

    public VoidValue caseGroup(com.thaiopensource.suggest.relaxng.pattern.GroupPattern p) {
      BuildFunction f = down();
      p.getOperand1().apply(f);
      p.getOperand2().apply(f);
      return VoidValue.VOID;
    }

    public VoidValue caseOneOrMore(OneOrMorePattern p) {
      p.getOperand().apply(down());
      return VoidValue.VOID;
    }

    public VoidValue caseElement(com.thaiopensource.suggest.relaxng.pattern.ElementPattern p) {
      if (elementProcessed.contains(p))
        return VoidValue.VOID;
      elementProcessed.add(p);
      elementsToProcess.push(p);
      return VoidValue.VOID;
    }

    public VoidValue caseAttribute(com.thaiopensource.suggest.relaxng.pattern.AttributePattern p) {
      int idType = p.getContent().apply(idTypeFunction);
      if (idType != Datatype.ID_TYPE_NULL) {
        com.thaiopensource.suggest.relaxng.pattern.NameClass attributeNameClass = p.getNameClass();
        if (!(attributeNameClass instanceof com.thaiopensource.suggest.relaxng.pattern.SimpleNameClass)) {
          error("id_attribute_name_class", p.getLocator());
          return VoidValue.VOID;
        }
        elementNameClass.accept(new ElementNameClassVisitor(((com.thaiopensource.suggest.relaxng.pattern.SimpleNameClass)attributeNameClass).getName(),
                                                            locator,
                                                            idType));
      }
      else
        notePossibleConflict(elementNameClass, p.getNameClass(), locator);
      p.getContent().apply(new BuildFunction(null, p.getLocator(), true));
      return VoidValue.VOID;
    }

    private void datatype(Datatype dt) {
      if (dt.getIdType() != Datatype.ID_TYPE_NULL && !attributeIsParent)
        error("id_parent", locator);
    }

    public VoidValue caseData(com.thaiopensource.suggest.relaxng.pattern.DataPattern p) {
      datatype(p.getDatatype());
      return VoidValue.VOID;
    }

    public VoidValue caseDataExcept(DataExceptPattern p) {
      datatype(p.getDatatype());
      p.getExcept().apply(down());
      return VoidValue.VOID;
    }

    public VoidValue caseValue(ValuePattern p) {
      datatype(p.getDatatype());
      return VoidValue.VOID;
    }

    public VoidValue caseList(com.thaiopensource.suggest.relaxng.pattern.ListPattern p) {
      p.getOperand().apply(down());
      return VoidValue.VOID;
    }

    public VoidValue caseOther(com.thaiopensource.suggest.relaxng.pattern.Pattern p) {
      return VoidValue.VOID;
    }
  }

  private class ElementNameClassVisitor implements NameClassVisitor {
    private final Name attributeName;
    private final SourceLocation locator;
    private final int idType;

    ElementNameClassVisitor(Name attributeName, SourceLocation locator, int idType) {
      this.attributeName = attributeName;
      this.locator = locator;
      this.idType = idType;
    }

    public void visitChoice(com.thaiopensource.suggest.relaxng.pattern.NameClass nc1, com.thaiopensource.suggest.relaxng.pattern.NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }

    public void visitName(Name elementName) {
      int tem = idTypeMap.getIdType(elementName, attributeName);
      if (tem !=  Datatype.ID_TYPE_NULL && tem != idType)
        error("id_type_conflict", elementName, attributeName, locator);
      idTypeMap.add(elementName, attributeName, idType);
    }

    public void visitNsName(String ns) {
      visitOther();
    }

    public void visitNsNameExcept(String ns, com.thaiopensource.suggest.relaxng.pattern.NameClass nc) {
      visitOther();
    }

    public void visitAnyName() {
      visitOther();
    }

    public void visitAnyNameExcept(NameClass nc) {
      visitOther();
    }

    public void visitNull() {
    }

    public void visitError() {
    }

    private void visitOther() {
      error("id_element_name_class", locator);
    }
  }

  private void error(String key, SourceLocation locator) {
    hadError = true;
    if (eh != null)
      try {
        eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key), SchemaBuilderImpl.makeLocation(locator)));
      }
      catch (SAXException e) {
        throw new WrappedSAXException(e);
      }
  }

  private void error(String key, Name arg1, Name arg2, SourceLocation locator) {
   hadError = true;
   if (eh != null)
     try {
       eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key, com.thaiopensource.suggest.relaxng.pattern.NameFormatter.format(arg1), NameFormatter.format(arg2)),
                                      SchemaBuilderImpl.makeLocation(locator)));
     }
     catch (SAXException e) {
       throw new WrappedSAXException(e);
     }
  }

  public IdTypeMapBuilder(ErrorHandler eh, Pattern pattern) throws SAXException {
    this.eh = eh;
    try {
      pattern.apply(new BuildFunction(null, null));
      while (elementsToProcess.size() > 0) {
        ElementPattern p = elementsToProcess.pop();
        p.getContent().apply(new BuildFunction(p.getNameClass(), p.getLocator()));
      }
      for (PossibleConflict pc : possibleConflicts) {
        if (pc.elementNameClass instanceof com.thaiopensource.suggest.relaxng.pattern.SimpleNameClass
            && pc.attributeNameClass instanceof com.thaiopensource.suggest.relaxng.pattern.SimpleNameClass) {
          Name elementName = ((com.thaiopensource.suggest.relaxng.pattern.SimpleNameClass)pc.elementNameClass).getName();
          Name attributeName = ((SimpleNameClass)pc.attributeNameClass).getName();
          int idType = idTypeMap.getIdType(elementName,
                                           attributeName);
          if (idType != Datatype.ID_TYPE_NULL)
            error("id_type_conflict", elementName, attributeName, pc.locator);
        }
        else {
          for (ScopedName sn : idTypeMap.table.keySet()) {
            if (pc.elementNameClass.contains(sn.elementName)
                && pc.attributeNameClass.contains(sn.attributeName)) {
              error("id_type_conflict", sn.elementName, sn.attributeName, pc.locator);
              break;
            }
          }
        }
      }
    }
    catch (WrappedSAXException e) {
      throw e.cause;
    }
  }

  public IdTypeMap getIdTypeMap() {
    if (hadError)
      return null;
    return idTypeMap;
  }
}
