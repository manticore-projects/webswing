package org.webswing.directdraw.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.model.DrawConstant;
import org.webswing.directdraw.model.FontFaceConst;
import org.webswing.directdraw.model.ImageConst;
import org.webswing.directdraw.proto.Directdraw.DrawConstantProto;

public class DrawConstantPool {

  private static final int IMG_CACHE_SIZE = Integer.getInteger("webswing.ddImageCacheSize", 128);
  private static final int CONSTANT_CACHE_SIZE =
      Integer.getInteger("webswing.ddConstCacheSize", 8192);
  public static final int CONSTANT_CACHE_SIZE_MAX =
      Integer.getInteger("webswing.ddMaxConstCacheSize", 8192 * 32);

  private LRUDrawConstantPoolCache pool;
  private LRUDrawConstantPoolCache imgPool;
  private Set<String> registeredFonts = new HashSet<String>();
  private Map<String, FontFaceConst> requestedFonts = new HashMap<String, FontFaceConst>();
  private int poolOverflowCounter = 0;
  private int imgPoolOverflowCounter = 0;

  public DrawConstantPool() {
    pool = new LRUDrawConstantPoolCache(CONSTANT_CACHE_SIZE, 0, CONSTANT_CACHE_SIZE_MAX);
    imgPool = new LRUDrawConstantPoolCache(IMG_CACHE_SIZE, CONSTANT_CACHE_SIZE_MAX,
        CONSTANT_CACHE_SIZE_MAX);
  }

  public void resetCacheOverflowCounters() {
    poolOverflowCounter = 0;
    imgPoolOverflowCounter = 0;
  }

  public int addToCache(List<DrawConstantProto> protos, DrawConstant<?> cons) {
    if (cons instanceof ImageConst) {
      return addToCache(imgPool, protos, cons, imgPoolOverflowCounter++);
    } else {
      return addToCache(pool, protos, cons, poolOverflowCounter++);
    }
  }

  private int addToCache(LRUDrawConstantPoolCache cache, List<DrawConstantProto> protos,
      DrawConstant<?> cons, int overflowCounter) {
    if (overflowCounter >= cache.getCapacity()) {
      cache.increaseCapacity();
    }
    int thisId;
    if (!cache.contains(cons)) {
      DrawConstant<?> cacheEntry = cons.toCacheEntry();
      cache.getOrAdd(cacheEntry);
      DrawConstantProto.Builder proto = DrawConstantProto.newBuilder();
      if (cons.getFieldName() != null) {
        proto.setField(
            DrawConstantProto.Builder.getDescriptor().findFieldByName(cons.getFieldName()),
            cons.toMessage());
      }
      proto.setId(cacheEntry.getId());
      protos.add(proto.build());
      thisId = cacheEntry.getId();
    } else {
      thisId = cache.getOrAdd(cons).getId();
    }
    return thisId;
  }

  public synchronized boolean isFontRegistered(String file) {
    if (requestedFonts.containsKey(file) || registeredFonts.contains(file)
        || Boolean.getBoolean(DirectDraw.FONTS_PROVIDED)) {
      return true;
    }
    return false;
  }

  public synchronized void requestFont(String file, FontFaceConst fontFaceConst) {
    requestedFonts.put(file, fontFaceConst);
  }

  public synchronized Collection<FontFaceConst> registerRequestedFonts() {
    if (requestedFonts.size() > 0) {
      Collection<FontFaceConst> result = new ArrayList<FontFaceConst>(requestedFonts.values());
      registeredFonts.addAll(requestedFonts.keySet());
      requestedFonts.clear();
      return result;
    }
    return Collections.emptyList();
  }

}
