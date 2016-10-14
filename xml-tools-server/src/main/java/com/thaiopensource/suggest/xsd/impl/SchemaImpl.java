package com.thaiopensource.suggest.xsd.impl;

import com.thaiopensource.suggest.Suggester;
import com.thaiopensource.suggest.SuggesterSchema;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.Validator;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xs.XSModel;

class SchemaImpl extends AbstractSchema implements SuggesterSchema {
  private final SymbolTable symbolTable;
  private final XMLGrammarPool grammarPool;
  private XSModel model = null;

  SchemaImpl(SymbolTable symbolTable,
             XMLGrammarPool grammarPool,
             PropertyMap properties,
             PropertyId<?>[] supportedPropertyIds) {
    super(properties, supportedPropertyIds);
    this.symbolTable = symbolTable;
    this.grammarPool = grammarPool;

  }

  public Suggester createSuggester(PropertyMap properties) {
    if (model == null) {
      Grammar[] grammars = grammarPool.retrieveInitialGrammarSet(XMLGrammarDescription.XML_SCHEMA);

      if (grammars.length > 0) {
        SchemaGrammar[] schemaGrammars = new SchemaGrammar[grammars.length];
        for (int i = 0; i  < schemaGrammars.length; i++) {
          schemaGrammars[i] = (SchemaGrammar) grammars[i];
        }
        model = (schemaGrammars[0]).toXSModel(schemaGrammars);
      }
    }

    return new SuggesterImpl(symbolTable, grammarPool, model, properties);
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(symbolTable, grammarPool, properties);
  }
}
