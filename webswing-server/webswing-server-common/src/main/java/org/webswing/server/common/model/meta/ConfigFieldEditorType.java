package org.webswing.server.common.model.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
@Inherited
public @interface ConfigFieldEditorType {
  public enum EditorType {
    String, Number, Boolean, Object, StringList, StringMap, ObjectList, ObjectListAsTable, ObjectMap, Generic;
  }

  EditorType editor() default EditorType.Object;

  String className() default "";

}
