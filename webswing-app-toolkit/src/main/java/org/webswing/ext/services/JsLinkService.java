package org.webswing.ext.services;

import java.lang.reflect.Method;

import org.webswing.model.appframe.in.JavaEvalRequestMsgIn;
import org.webswing.model.appframe.in.JsParamMsgIn;
import org.webswing.model.appframe.out.JsParamMsgOut;
import org.webswing.model.appframe.out.JsResultMsgOut;

import netscape.javascript.JSException;

public interface JsLinkService {

  public JsParamMsgOut generateParam(Object arg) throws Exception;

  public JsResultMsgOut generateJavaResult(String correlationId, Object result) throws Exception;

  public JsResultMsgOut generateJavaErrorResult(String correlationId, Throwable result);

  public Object parseValue(JsParamMsgIn value) throws JSException;

  public Object[] getCompatibleParams(JavaEvalRequestMsgIn javaReq, Method m) throws Exception;

}
