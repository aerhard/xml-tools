package com.thaiopensource.suggest.relaxng.impl;

// slightly extended version of com.thaiopensource.validate.CombineSchema

import com.thaiopensource.suggest.Suggester;
import com.thaiopensource.suggest.SuggesterSchema;
import com.thaiopensource.suggest.relaxng.impl.IdTypeMapSchema;
import com.thaiopensource.suggest.relaxng.impl.SuggesterImpl;
import com.thaiopensource.suggest.relaxng.pattern.IdTypeMap;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.CombineValidator;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.Validator;

public class CombineSchema extends AbstractSchema implements SuggesterSchema {
  private final PatternSchema schema1;
  private final IdTypeMapSchema schema2;

  public CombineSchema(PatternSchema schema1, IdTypeMapSchema schema2, PropertyMap properties) {
    super(properties);
    this.schema1 = schema1;
    this.schema2 = schema2;
  }

  public Suggester createSuggester(PropertyMap properties) {
    return schema1.createSuggester(properties, schema2.getIdTypeMap());
  }

  public Validator createValidator(PropertyMap properties) {
    return new CombineValidator(schema1.createValidator(properties),
        schema2.createValidator(properties));
  }

}
