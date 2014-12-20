package org.webswing.directdraw.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.ImageProto;

import com.google.protobuf.ByteString;

public class ImageConst extends DrawConstant {

	private ImageProto.Builder model = ImageProto.newBuilder();
	private long hash = System.identityHashCode(this);
	//map of subImage hash and its start position 
	private Map<Long,Point> subImageHashMap;

	public ImageConst(DirectDraw context, BufferedImage img, Rectangle2D crop, Map<Long,Point> subImageHashMap) {
		super(context);
		this.subImageHashMap=subImageHashMap;
		if (crop != null) {
			model.setOffsetX((int) crop.getX());
			model.setOffsetY((int) crop.getY());
		}
		crop = crop != null ? crop : new Rectangle(img.getWidth(), img.getHeight());
		byte[] imgData = context.getServices().getPngImage(img.getSubimage((int) crop.getX(), (int) crop.getY(), (int) crop.getWidth(), (int) crop.getHeight()));
		try {
			model.setData(ByteString.readFrom(new ByteArrayInputStream(imgData)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Map<Long, Point> getSubImageHashMap() {
		return subImageHashMap;
	}

	@Override
	protected long getHash() {
		return hash ;
	}
	
	@Override
	public Object extractMessage(DirectDraw dd) {
		model.setHash(getAddress());
		this.message=model.build();
		model=null;
		return super.extractMessage(dd);
	}

	@Override
	public String getFieldName() {
		return "image";
	}

}
