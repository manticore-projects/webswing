package org.webswing.toolkit.jslink;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import org.webswing.Constants;
import org.webswing.model.SyncObjectResponse;
import org.webswing.model.app.out.AppToServerFrameMsgOut;
import org.webswing.model.appframe.in.AppFrameMsgIn;
import org.webswing.model.appframe.in.JSObjectMsgIn;
import org.webswing.model.appframe.in.JavaEvalRequestMsgIn;
import org.webswing.model.appframe.out.AppFrameMsgOut;
import org.webswing.toolkit.util.JsLinkUtil;
import org.webswing.toolkit.util.Services;
import org.webswing.toolkit.util.WeakValueHashMap;
import org.webswing.util.NamedThreadFactory;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class WebJSObject extends JSObject {

  private static final Map<String, WeakReference<JSObjectMsgIn>> jsGarbageCollectionMap =
      new HashMap<String, WeakReference<JSObjectMsgIn>>();
  private static final WeakValueHashMap<String, Object> javaReferences =
      new WeakValueHashMap<String, Object>();
  private static boolean jsLinkAllowed =
      Boolean.getBoolean(Constants.SWING_START_SYS_PROP_ALLOW_JSLINK);
  private static String jsLinkWhitelistProp =
      System.getProperty(Constants.SWING_START_SYS_PROP_JSLINK_WHITELIST, "");
  private static List<String> jsLinkWhitelist;
  private static ScheduledExecutorService javaEvalThread =
      Executors.newSingleThreadScheduledExecutor(
          NamedThreadFactory.getInstance("Webswing JsLink Processor"));
  private JSObjectMsgIn jsThis;

  static {
    jsLinkWhitelist = new ArrayList<>();
    jsLinkWhitelist = Arrays.asList(jsLinkWhitelistProp.split(","));
  }

  public WebJSObject(JSObjectMsgIn jsThis) {
    this.jsThis = jsThis;
    if (jsThis != null) {
      synchronized (jsGarbageCollectionMap) {
        jsGarbageCollectionMap.put(jsThis.getId() + "", new WeakReference<JSObjectMsgIn>(jsThis));
      }
    }
  }

  @Override
  public Object call(String methodName, Object[] args) throws JSException {
    AppFrameMsgOut msg = JsLinkUtil.generateCallRequest(jsThis, methodName, args);
    return sendJsRequest(msg);
  }

  @Override
  public Object eval(String s) throws JSException {
    AppFrameMsgOut msg = JsLinkUtil.generateEvalRequest(jsThis, s);
    return sendJsRequest(msg);
  }

  @Override
  public Object getMember(String name) throws JSException {
    AppFrameMsgOut msg = JsLinkUtil.generateGetMemberRequest(jsThis, name);
    return sendJsRequest(msg);
  }

  @Override
  public void setMember(String name, Object value) throws JSException {
    AppFrameMsgOut msg = JsLinkUtil.generateSetMemberRequest(jsThis, name, value);
    sendJsRequest(msg);
  }

  @Override
  public void removeMember(String name) throws JSException {
    AppFrameMsgOut msg = JsLinkUtil.generateRemoveMemberRequest(jsThis, name);
    sendJsRequest(msg);
  }

  @Override
  public Object getSlot(int index) throws JSException {
    AppFrameMsgOut msg = JsLinkUtil.generateGetSlotRequest(jsThis, index);
    return sendJsRequest(msg);
  }

  @Override
  public void setSlot(int index, Object value) throws JSException {
    AppFrameMsgOut msg = JsLinkUtil.generateSetSlotRequest(jsThis, index, value);
    sendJsRequest(msg);
  }

  private static Object sendJsRequest(AppFrameMsgOut frame) {
    try {
      AppToServerFrameMsgOut msgOut = new AppToServerFrameMsgOut();
      SyncObjectResponse result = Services.getConnectionService().sendObjectSync(msgOut, frame,
          frame.getJsRequest().getCorrelationId());
      if (result.getFrame() != null) {
        AppFrameMsgIn frameIn = result.getFrame();
        return JsLinkUtil.parseResponse(frameIn);
      }
      return null;
    } catch (TimeoutException e) {
      throw new JSException(e.getMessage());
    } catch (Exception e) {
      throw new JSException(e.getMessage());
    }
  }

  public static List<String> getGarbage() {
    ArrayList<String> result = new ArrayList<String>();
    synchronized (jsGarbageCollectionMap) {
      for (Iterator<String> i = jsGarbageCollectionMap.keySet().iterator(); i.hasNext();) {
        String key = i.next();
        if (jsGarbageCollectionMap.get(key).isEnqueued()) {
          result.add(key);
          i.remove();
        }
      }
    }
    return result;
  }

  public JSObjectMsgIn getThisId() {
    return jsThis;
  }

  public static String createJavaReference(Object arg) {
    return createJavaReference(arg, UUID.randomUUID().toString());
  }

  public static String createJavaReference(Object arg, String newId) {
    if (javaReferences.containsValue(arg)) {
      String id = null;
      for (String key : javaReferences.keySet()) {
        if (javaReferences.get(key) == arg) {
          id = key;
        }
      }
      return id;
    } else {
      String id = newId;
      javaReferences.put(id, arg);
      return id;
    }
  }

  public static Object getJavaReference(String id) {
    Object o = javaReferences.get(id);
    return o;
  }

  public static Future<?> evaluateJava(final JavaEvalRequestMsgIn javaReq) {
    return javaEvalThread.submit(new Runnable() {
      @Override
      public void run() {
        AppToServerFrameMsgOut msgOut = new AppToServerFrameMsgOut();
        AppFrameMsgOut frame;

        if (jsLinkAllowed) {
          Object javaRef = javaReferences.get(javaReq.getObjectId());
          frame = JsLinkUtil.callMatchingMethod(javaReq, javaRef, jsLinkWhitelist);
        } else {
          frame = JsLinkUtil.getErrorResponse(javaReq.getCorrelationId(),
              "JsLink is not allowed for this application. Set the 'allowJsLink' to true in webswing.config to enable it.");
        }

        Services.getConnectionService().sendObject(msgOut, frame);
      }
    });
  }

}
