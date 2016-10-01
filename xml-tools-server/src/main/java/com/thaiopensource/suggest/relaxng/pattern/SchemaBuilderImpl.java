package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.parse.*;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.*;
import org.xml.sax.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaBuilderImpl extends com.thaiopensource.suggest.relaxng.pattern.CommentListImpl implements
    SchemaBuilder<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.NameClass, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> {
  private final SchemaBuilderImpl parent;
  private boolean hadError = false;
  private final SubParser<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.NameClass, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> subParser;
  private final SchemaPatternBuilder pb;
  private final DatatypeLibraryFactory datatypeLibraryFactory;
  private final String inheritNs;
  private final ErrorHandler eh;
  private final OpenIncludes openIncludes;
  private final AttributeNameClassChecker attributeNameClassChecker = new AttributeNameClassChecker();
  static final Localizer localizer = new Localizer(com.thaiopensource.relaxng.pattern.SchemaBuilderImpl.class);

  static class OpenIncludes {
    final String uri;
    final OpenIncludes parent;

    OpenIncludes(String uri, OpenIncludes parent) {
      this.uri = uri;
      this.parent = parent;
    }
  }

  static public com.thaiopensource.suggest.relaxng.pattern.Pattern parse(Parseable<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.NameClass, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> parseable,
                                                                         ErrorHandler eh,
                                                                         DatatypeLibraryFactory datatypeLibraryFactory,
                                                                         SchemaPatternBuilder pb,
                                                                         boolean isAttributesPattern)
      throws IllegalSchemaException, IOException, SAXException {
    try {
      SchemaBuilderImpl sb = new SchemaBuilderImpl(parseable,
          eh,
          new BuiltinDatatypeLibraryFactory(datatypeLibraryFactory),
          pb);
      com.thaiopensource.suggest.relaxng.pattern.Pattern pattern = parseable.parse(sb, new RootScope(sb));
      if (isAttributesPattern)
        pattern = sb.wrapAttributesPattern(pattern);
      return sb.expandPattern(pattern);
    } catch (BuildException e) {
      throw unwrapBuildException(e);
    }
  }


  static public com.thaiopensource.suggest.relaxng.pattern.PatternFuture installHandlers(ParseReceiver<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.NameClass, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> parser,
                                                                                         XMLReader xr,
                                                                                         ErrorHandler eh,
                                                                                         DatatypeLibraryFactory dlf,
                                                                                         SchemaPatternBuilder pb)
      throws SAXException {
    final SchemaBuilderImpl sb = new SchemaBuilderImpl(parser,
        eh,
        new BuiltinDatatypeLibraryFactory(dlf),
        pb);
    final ParsedPatternFuture<com.thaiopensource.suggest.relaxng.pattern.Pattern> pf = parser.installHandlers(xr, sb, new RootScope(sb));
    return new com.thaiopensource.suggest.relaxng.pattern.PatternFuture() {
      public com.thaiopensource.suggest.relaxng.pattern.Pattern getPattern(boolean isAttributesPattern) throws IllegalSchemaException, SAXException, IOException {
        try {
          com.thaiopensource.suggest.relaxng.pattern.Pattern pattern = pf.getParsedPattern();
          if (isAttributesPattern)
            pattern = sb.wrapAttributesPattern(pattern);
          return sb.expandPattern(pattern);
        } catch (BuildException e) {
          throw unwrapBuildException(e);
        }
      }
    };
  }

  static public RuntimeException unwrapBuildException(BuildException e) throws SAXException, IllegalSchemaException, IOException {
    Throwable t = e.getCause();
    if (t instanceof IOException)
      throw (IOException) t;
    if (t instanceof RuntimeException)
      return (RuntimeException) t;
    if (t instanceof IllegalSchemaException)
      throw new IllegalSchemaException();
    if (t instanceof SAXException)
      throw (SAXException) t;
    if (t instanceof Exception)
      throw new SAXException((Exception) t);
    throw new SAXException(t.getClass().getName() + " thrown");
  }

  private com.thaiopensource.suggest.relaxng.pattern.Pattern wrapAttributesPattern(com.thaiopensource.suggest.relaxng.pattern.Pattern pattern) {
    // XXX where can we get a locator from?
    return makeElement(makeAnyName(null, null), pattern, null, null);
  }

  private com.thaiopensource.suggest.relaxng.pattern.Pattern expandPattern(com.thaiopensource.suggest.relaxng.pattern.Pattern pattern) throws IllegalSchemaException, BuildException {
    if (!hadError) {
      try {
        pattern.checkRecursion(0);
        pattern = pattern.expand(pb);
        pattern.checkRestrictions(com.thaiopensource.suggest.relaxng.pattern.Pattern.START_CONTEXT, null, null);
        if (!hadError)
          return pattern;
      } catch (SAXParseException e) {
        error(e);
      } catch (SAXException e) {
        throw new BuildException(e);
      } catch (RestrictionViolationException e) {
        if (e.getName() != null)
          error(e.getMessageId(), NameFormatter.format(e.getName()), e.getLocator());
        else if (e.getNamespaceUri() != null)
          error(e.getMessageId(), e.getNamespaceUri(), e.getLocator());
        else
          error(e.getMessageId(), e.getLocator());
      }
    }
    throw new IllegalSchemaException();
  }

  private SchemaBuilderImpl(SubParser<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.NameClass, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> subParser,
                            ErrorHandler eh,
                            DatatypeLibraryFactory datatypeLibraryFactory,
                            SchemaPatternBuilder pb) {
    this.parent = null;
    this.subParser = subParser;
    this.eh = eh;
    this.datatypeLibraryFactory = datatypeLibraryFactory;
    this.pb = pb;
    this.inheritNs = "";
    this.openIncludes = null;
  }

  private SchemaBuilderImpl(String inheritNs,
                            String uri,
                            SchemaBuilderImpl parent) {
    this.parent = parent;
    this.subParser = parent.subParser;
    this.eh = parent.eh;
    this.datatypeLibraryFactory = parent.datatypeLibraryFactory;
    this.pb = parent.pb;
    this.inheritNs = parent.resolveInherit(inheritNs);
    this.openIncludes = new OpenIncludes(uri, parent.openIncludes);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeChoice(List<com.thaiopensource.suggest.relaxng.pattern.Pattern> patterns, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    int nPatterns = patterns.size();
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    com.thaiopensource.suggest.relaxng.pattern.Pattern result = patterns.get(0);
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeChoice(result, patterns.get(i));
    return finishPattern(result, loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeInterleave(List<com.thaiopensource.suggest.relaxng.pattern.Pattern> patterns, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    int nPatterns = patterns.size();
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    com.thaiopensource.suggest.relaxng.pattern.Pattern result = patterns.get(0);
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeInterleave(result, patterns.get(i));
    return finishPattern(result, loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeGroup(List<com.thaiopensource.suggest.relaxng.pattern.Pattern> patterns, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    int nPatterns = patterns.size();
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    com.thaiopensource.suggest.relaxng.pattern.Pattern result = patterns.get(0);
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeGroup(result, patterns.get(i));
    return finishPattern(result, loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeOneOrMore(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    return finishPattern(pb.makeOneOrMore(p), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeZeroOrMore(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    return finishPattern(pb.makeZeroOrMore(p), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeOptional(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    return finishPattern(pb.makeOptional(p), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeList(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    return finishPattern(pb.makeList(p, loc), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeMixed(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    return finishPattern(pb.makeMixed(p), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeEmpty(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishPattern(pb.makeEmpty(), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeNotAllowed(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishPattern(pb.makeUnexpandedNotAllowed(), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeText(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishPattern(pb.makeText(), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeErrorPattern() {
    return pb.makeError();
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass makeErrorNameClass() {
    return new ErrorNameClass();
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeAttribute(com.thaiopensource.suggest.relaxng.pattern.NameClass nc, com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    String messageId = attributeNameClassChecker.checkNameClass(nc);
    if (messageId != null)
      error(messageId, loc);
    return finishPattern(pb.makeAttribute(nc, p, loc), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeElement(com.thaiopensource.suggest.relaxng.pattern.NameClass nc, com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    return finishPattern(pb.makeElement(nc, p, loc), loc, anno);
  }

  private class DummyDataPatternBuilder implements DataPatternBuilder<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> {
    public void addParam(String name, String value, Context context, String ns, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
    }

    public void annotation(ElementAnnotationBuilderImpl ea)
        throws BuildException {
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern makePattern(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
      return pb.makeError();
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern makePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern except, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
      return pb.makeError();
    }
  }

  private class ValidationContextImpl implements ValidationContext {
    private final ValidationContext vc;
    private final String ns;

    ValidationContextImpl(ValidationContext vc, String ns) {
      this.vc = vc;
      this.ns = ns.length() == 0 ? null : ns;
    }

    public String resolveNamespacePrefix(String prefix) {
      String result = prefix.length() == 0 ? ns : vc.resolveNamespacePrefix(prefix);
      if (result == INHERIT_NS) {
        if (inheritNs.length() == 0)
          return null;
        return inheritNs;
      }
      return result;
    }

    public String getBaseUri() {
      return vc.getBaseUri();
    }

    public boolean isUnparsedEntity(String entityName) {
      return vc.isUnparsedEntity(entityName);
    }

    public boolean isNotation(String notationName) {
      return vc.isNotation(notationName);
    }
  }

  private class DataPatternBuilderImpl implements DataPatternBuilder<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> {
    private final DatatypeBuilder dtb;
    private final Name dtName;
    private final List<String> params = new ArrayList<String>();

    DataPatternBuilderImpl(DatatypeBuilder dtb, Name dtName) {
      this.dtb = dtb;
      this.dtName = dtName;
    }

    public void addParam(String name, String value, Context context, String ns, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
      try {
        dtb.addParameter(name, value, new ValidationContextImpl(context, ns));
        params.add(name);
        params.add(value);
      } catch (DatatypeException e) {
        String detail = e.getMessage();
        int pos = e.getIndex();
        String displayedParam;
        if (pos == DatatypeException.UNKNOWN)
          displayedParam = null;
        else
          displayedParam = displayParam(value, pos);
        if (displayedParam != null) {
          if (detail != null)
            error("invalid_param_detail_display", detail, displayedParam, loc);
          else
            error("invalid_param_display", displayedParam, loc);
        } else if (detail != null)
          error("invalid_param_detail", detail, loc);
        else
          error("invalid_param", loc);
      }
    }

    public void annotation(ElementAnnotationBuilderImpl ea)
        throws BuildException {
    }

    String displayParam(String value, int pos) {
      if (pos < 0)
        pos = 0;
      else if (pos > value.length())
        pos = value.length();
      return localizer.message("display_param", value.substring(0, pos), value.substring(pos));
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern makePattern(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
      try {
        return finishPattern(pb.makeData(dtb.createDatatype(), dtName, params), loc, anno);
      } catch (DatatypeException e) {
        String detail = e.getMessage();
        if (detail != null)
          error("invalid_params_detail", detail, loc);
        else
          error("invalid_params", loc);
        return pb.makeError();
      }
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern makePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern except, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
      try {
        return finishPattern(pb.makeDataExcept(dtb.createDatatype(), dtName, params, except, loc), loc, anno);
      } catch (DatatypeException e) {
        String detail = e.getMessage();
        if (detail != null)
          error("invalid_params_detail", detail, loc);
        else
          error("invalid_params", loc);
        return pb.makeError();
      }
    }
  }

  public DataPatternBuilder<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl>
  makeDataPatternBuilder(String datatypeLibrary, String type, SourceLocation loc)
      throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null)
      error("unrecognized_datatype_library", datatypeLibrary, loc);
    else {
      try {
        return new DataPatternBuilderImpl(dl.createDatatypeBuilder(type), new Name(datatypeLibrary, type));
      } catch (DatatypeException e) {
        String detail = e.getMessage();
        if (detail != null)
          error("unsupported_datatype_detail", datatypeLibrary, type, detail, loc);
        else
          error("unrecognized_datatype", datatypeLibrary, type, loc);
      }
    }
    return new DummyDataPatternBuilder();
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeValue(String datatypeLibrary, String type, String value, Context context, String ns,
                                                                      SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null) {
      error("unrecognized_datatype_library", datatypeLibrary, loc);
    } else {
      try {
        DatatypeBuilder dtb = dl.createDatatypeBuilder(type);
        try {
          Datatype dt = dtb.createDatatype();
          Object obj = dt.createValue(value, new ValidationContextImpl(context, ns));
          if (obj != null) {
            return finishPattern(pb.makeValue(dt, new Name(datatypeLibrary, type), obj, value), loc, anno);
          }
          error("invalid_value", value, loc);
        } catch (DatatypeException e) {
          String detail = e.getMessage();
          if (detail != null)
            error("datatype_requires_param_detail", detail, loc);
          else
            error("datatype_requires_param", loc);
        }
      } catch (DatatypeException e) {
        error("unrecognized_datatype", datatypeLibrary, type, loc);
      }
    }
    return pb.makeError();
  }

  static class GrammarImpl implements
      Grammar<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl>,
      Div<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl>,
      IncludedGrammar<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> {
    private final SchemaBuilderImpl sb;
    private final Map<String, com.thaiopensource.suggest.relaxng.pattern.RefPattern> defines;
    private final com.thaiopensource.suggest.relaxng.pattern.RefPattern startRef;
    private final Scope<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> parent;

    private GrammarImpl(SchemaBuilderImpl sb, Scope<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> parent) {
      this.sb = sb;
      this.parent = parent;
      this.defines = new HashMap<String, com.thaiopensource.suggest.relaxng.pattern.RefPattern>();
      this.startRef = new com.thaiopensource.suggest.relaxng.pattern.RefPattern(null);
    }

    protected GrammarImpl(SchemaBuilderImpl sb, GrammarImpl g) {
      this.sb = sb;
      parent = g.parent;
      startRef = g.startRef;
      defines = g.defines;
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern endGrammar(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      for (String name : defines.keySet()) {
        com.thaiopensource.suggest.relaxng.pattern.RefPattern rp = defines.get(name);
        if (rp.getPattern() == null) {
          sb.error("reference_to_undefined", name, rp.getRefLocator());
          rp.setPattern(sb.pb.makeError());
        }
      }
      com.thaiopensource.suggest.relaxng.pattern.Pattern start = startRef.getPattern();
      if (start == null) {
        sb.error("missing_start_element", loc);
        start = sb.pb.makeError();
      }
      return start;
    }

    public void endDiv(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      // nothing to do
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern endIncludedGrammar(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      return null;
    }

    public void define(String name, Combine combine, com.thaiopensource.suggest.relaxng.pattern.Pattern pattern, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
      define(lookup(name), combine, pattern, loc);
    }

    private void define(com.thaiopensource.suggest.relaxng.pattern.RefPattern rp, Combine combine, com.thaiopensource.suggest.relaxng.pattern.Pattern pattern, SourceLocation loc)
        throws BuildException {
      switch (rp.getReplacementStatus()) {
        case com.thaiopensource.suggest.relaxng.pattern.RefPattern.REPLACEMENT_KEEP:
          if (combine == null) {
            if (rp.isCombineImplicit()) {
              if (rp.getName() == null)
                sb.error("duplicate_start", loc);
              else
                sb.error("duplicate_define", rp.getName(), loc);
            } else
              rp.setCombineImplicit();
          } else {
            byte combineType = (combine == COMBINE_CHOICE ? com.thaiopensource.suggest.relaxng.pattern.RefPattern.COMBINE_CHOICE : com.thaiopensource.suggest.relaxng.pattern.RefPattern.COMBINE_INTERLEAVE);
            if (rp.getCombineType() != com.thaiopensource.suggest.relaxng.pattern.RefPattern.COMBINE_NONE
                && rp.getCombineType() != combineType) {
              if (rp.getName() == null)
                sb.error("conflict_combine_start", loc);
              else
                sb.error("conflict_combine_define", rp.getName(), loc);
            }
            rp.setCombineType(combineType);
          }
          if (rp.getPattern() == null)
            rp.setPattern(pattern);
          else if (rp.getCombineType() == com.thaiopensource.suggest.relaxng.pattern.RefPattern.COMBINE_INTERLEAVE)
            rp.setPattern(sb.pb.makeInterleave(rp.getPattern(), pattern));
          else
            rp.setPattern(sb.pb.makeChoice(rp.getPattern(), pattern));
          break;
        case com.thaiopensource.suggest.relaxng.pattern.RefPattern.REPLACEMENT_REQUIRE:
          rp.setReplacementStatus(com.thaiopensource.suggest.relaxng.pattern.RefPattern.REPLACEMENT_IGNORE);
          break;
        case com.thaiopensource.suggest.relaxng.pattern.RefPattern.REPLACEMENT_IGNORE:
          break;
      }
    }

    public void topLevelAnnotation(ElementAnnotationBuilderImpl ea) throws BuildException {
    }

    public void topLevelComment(com.thaiopensource.suggest.relaxng.pattern.CommentListImpl comments) throws BuildException {
    }

    private com.thaiopensource.suggest.relaxng.pattern.RefPattern lookup(String name) {
      if (name == START)
        return startRef;
      return lookup1(name);
    }

    private com.thaiopensource.suggest.relaxng.pattern.RefPattern lookup1(String name) {
      com.thaiopensource.suggest.relaxng.pattern.RefPattern p = defines.get(name);
      if (p == null) {
        p = new com.thaiopensource.suggest.relaxng.pattern.RefPattern(name);
        defines.put(name, p);
      }
      return p;
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern makeRef(String name, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      com.thaiopensource.suggest.relaxng.pattern.RefPattern p = lookup1(name);
      if (p.getRefLocator() == null && loc != null)
        p.setRefLocator(loc);
      return p;
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern makeParentRef(String name, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      if (parent == null) {
        sb.error("parent_ref_outside_grammar", loc);
        return sb.makeErrorPattern();
      }
      return finishPattern(parent.makeRef(name, loc, anno), loc, anno);
    }

    public Div<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> makeDiv() {
      return this;
    }

    public Include<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> makeInclude() {
      return new IncludeImpl(sb, this);
    }

  }

  static class RootScope implements Scope<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> {
    private final SchemaBuilderImpl sb;

    RootScope(SchemaBuilderImpl sb) {
      this.sb = sb;
    }

    public com.thaiopensource.suggest.relaxng.pattern.Pattern makeParentRef(String name, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      sb.error("parent_ref_outside_grammar", loc);
      return sb.makeErrorPattern();
    }
    public com.thaiopensource.suggest.relaxng.pattern.Pattern makeRef(String name, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      sb.error("ref_outside_grammar", loc);
      return sb.makeErrorPattern();
    }

  }

  static class Override {
    Override(com.thaiopensource.suggest.relaxng.pattern.RefPattern prp, Override next) {
      this.prp = prp;
      this.next = next;
    }

    final com.thaiopensource.suggest.relaxng.pattern.RefPattern prp;
    final Override next;
    byte replacementStatus;
  }

  private static class IncludeImpl implements
      Include<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl>,
      Div<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> {
    private final SchemaBuilderImpl sb;
    private Override overrides;
    private final GrammarImpl grammar;

    private IncludeImpl(SchemaBuilderImpl sb, GrammarImpl grammar) {
      this.sb = sb;
      this.grammar = grammar;
    }

    public void define(String name, Combine combine, com.thaiopensource.suggest.relaxng.pattern.Pattern pattern, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
        throws BuildException {
      com.thaiopensource.suggest.relaxng.pattern.RefPattern rp = grammar.lookup(name);
      overrides = new Override(rp, overrides);
      grammar.define(rp, combine, pattern, loc);
    }

    public void endDiv(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      // nothing to do
    }

    public void topLevelAnnotation(ElementAnnotationBuilderImpl ea) throws BuildException {
      // nothing to do
    }

    public void topLevelComment(com.thaiopensource.suggest.relaxng.pattern.CommentListImpl comments) throws BuildException {
    }

    public Div<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> makeDiv() {
      return this;
    }

    public void endInclude(String href, String base, String ns,
                           SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
      SubParseable<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.NameClass, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> subParseable
          = sb.subParser.createSubParseable(href, base);
      String uri = subParseable.getUri();
      for (OpenIncludes inc = sb.openIncludes;
           inc != null;
           inc = inc.parent) {
        if (inc.uri.equals(uri)) {
          sb.error("recursive_include", uri, loc);
          return;
        }
      }

      for (Override o = overrides; o != null; o = o.next) {
        o.replacementStatus = o.prp.getReplacementStatus();
        o.prp.setReplacementStatus(com.thaiopensource.suggest.relaxng.pattern.RefPattern.REPLACEMENT_REQUIRE);
      }
      try {
        SchemaBuilderImpl isb = new SchemaBuilderImpl(ns, uri, sb);
        subParseable.parseAsInclude(isb, new GrammarImpl(isb, grammar));
        for (Override o = overrides; o != null; o = o.next) {
          if (o.prp.getReplacementStatus() == RefPattern.REPLACEMENT_REQUIRE) {
            if (o.prp.getName() == null)
              sb.error("missing_start_replacement", loc);
            else
              sb.error("missing_define_replacement", o.prp.getName(), loc);
          }
        }
      } catch (IllegalSchemaException e) {
        sb.noteError();
      } finally {
        for (Override o = overrides; o != null; o = o.next)
          o.prp.setReplacementStatus(o.replacementStatus);
      }
    }

    public Include<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> makeInclude() {
      return null;
    }
  }

  public Grammar<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl>
  makeGrammar(Scope<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> parent) {
    return new GrammarImpl(this, parent);
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern makeExternalRef(String href, String base, String ns, Scope<com.thaiopensource.suggest.relaxng.pattern.Pattern, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> scope,
                                                                            SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno)
      throws BuildException {
    SubParseable<com.thaiopensource.suggest.relaxng.pattern.Pattern, com.thaiopensource.suggest.relaxng.pattern.NameClass, SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl> subParseable
        = subParser.createSubParseable(href, base);
    String uri = subParseable.getUri();
    for (OpenIncludes inc = openIncludes;
         inc != null;
         inc = inc.parent) {
      if (inc.uri.equals(uri)) {
        error("recursive_include", uri, loc);
        return pb.makeError();
      }
    }
    try {
      return subParseable.parse(new SchemaBuilderImpl(ns, uri, this), scope);
    } catch (IllegalSchemaException e) {
      noteError();
      return pb.makeError();
    }
  }

  static private com.thaiopensource.suggest.relaxng.pattern.Pattern finishPattern(com.thaiopensource.suggest.relaxng.pattern.Pattern p, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    finishAnnotated(p, loc, anno);
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass makeNameClassChoice(List<com.thaiopensource.suggest.relaxng.pattern.NameClass> nameClasses, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    int nNameClasses = nameClasses.size();
    if (nNameClasses <= 0)
      throw new IllegalArgumentException();
    com.thaiopensource.suggest.relaxng.pattern.NameClass result = nameClasses.get(0);
    for (int i = 1; i < nNameClasses; i++)
      result = new ChoiceNameClass(result, nameClasses.get(i));
    return finishNameClass(result, loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass makeName(String ns, String localName, String prefix, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishNameClass(new SimpleNameClass(new Name(resolveInherit(ns), localName)), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass makeNsName(String ns, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishNameClass(new NsNameClass(resolveInherit(ns)), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass makeNsName(String ns, com.thaiopensource.suggest.relaxng.pattern.NameClass except, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishNameClass(new NsNameExceptNameClass(resolveInherit(ns), except), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass makeAnyName(SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishNameClass(new AnyNameClass(), loc, anno);
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass makeAnyName(com.thaiopensource.suggest.relaxng.pattern.NameClass except, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    return finishNameClass(new AnyNameExceptNameClass(except), loc, anno);
  }


  private static com.thaiopensource.suggest.relaxng.pattern.NameClass finishNameClass(com.thaiopensource.suggest.relaxng.pattern.NameClass nc, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    finishAnnotated(nc, loc, anno);
    return nc;
  }

  private static void finishAnnotated(Annotated a, SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) {
    a.setSourceLocation(loc);
    if (anno != null)
      anno.apply(a);
  }

  public com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl makeAnnotations(com.thaiopensource.suggest.relaxng.pattern.CommentListImpl comments, Context context) {
    return new com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl(context);
  }

  public ElementAnnotationBuilder<SourceLocation, ElementAnnotationBuilderImpl, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl>
  makeElementAnnotationBuilder(String ns, String localName, String prefix,
                               SourceLocation loc, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl comments, Context context) {
    ElementAnnotation element = new ElementAnnotation(ns, localName);
    element.setPrefix(prefix);
    element.setSourceLocation(loc);
    element.setContext(new NamespaceContextImpl(context));
    return new ElementAnnotationBuilderImpl(element);
  }

  public com.thaiopensource.suggest.relaxng.pattern.CommentListImpl makeCommentList() {
    return this;
  }

  public boolean usesComments() {
    return true;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern annotatePattern(com.thaiopensource.suggest.relaxng.pattern.Pattern p, com.thaiopensource.suggest.relaxng.pattern.AnnotationsImpl anno) throws BuildException {
    if (anno != null) anno.apply(p);
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass annotateNameClass(com.thaiopensource.suggest.relaxng.pattern.NameClass nc, AnnotationsImpl anno) throws BuildException {
    if (anno != null) anno.apply(nc);
    return nc;
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern annotateAfterPattern(com.thaiopensource.suggest.relaxng.pattern.Pattern p, ElementAnnotationBuilderImpl e) throws BuildException {
    addAfterAnnotation(p, e);
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass annotateAfterNameClass(com.thaiopensource.suggest.relaxng.pattern.NameClass nc, ElementAnnotationBuilderImpl e) throws BuildException {
    addAfterAnnotation(nc, e);
    return nc;
  }

  static private void addAfterAnnotation(Annotated a, ElementAnnotationBuilderImpl e) {
    e.addDocumentationTo(a.getFollowingElementAnnotations());
  }

  public com.thaiopensource.suggest.relaxng.pattern.Pattern commentAfterPattern(Pattern p, com.thaiopensource.suggest.relaxng.pattern.CommentListImpl comments) throws BuildException {
    return p;
  }

  public com.thaiopensource.suggest.relaxng.pattern.NameClass commentAfterNameClass(NameClass nc, CommentListImpl comments) throws BuildException {
    return nc;
  }

  private String resolveInherit(String ns) {
    if (ns == INHERIT_NS)
      return inheritNs;
    return ns;
  }

  private static class LocatorImpl extends SourceLocation implements Locator {

    private LocatorImpl(String systemId, int lineNumber, int columnNumber) {
      super(systemId, lineNumber, columnNumber);
    }

    public String getPublicId() {
      return null;
    }

    public String getSystemId() {
      return this.getUri();
    }
  }

  public SourceLocation makeLocation(String systemId, int lineNumber, int columnNumber) {
    return new LocatorImpl(systemId, lineNumber, columnNumber);
  }

  public static Locator makeLocation(SourceLocation location) {
    return new LocatorImpl(location.getUri(), location.getLineNumber(), location.getColumnNumber());
  }


  private void error(SAXParseException message) throws BuildException {
    noteError();
    try {
      if (eh != null)
        eh.error(message);
    } catch (SAXException e) {
      throw new BuildException(e);
    }
  }

  private void error(String key, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key), makeLocation(loc)));
  }

  private void error(String key, String arg, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg), makeLocation(loc)));
  }

  private void error(String key, String arg1, String arg2, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg1, arg2), makeLocation(loc)));
  }

  private void error(String key, String arg1, String arg2, String arg3, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, new Object[]{arg1, arg2, arg3}), makeLocation(loc)));
  }

  private void noteError() {
    if (!hadError && parent != null)
      parent.noteError();
    hadError = true;
  }
}
