package org.webswing.javafx.toolkit;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.sun.glass.ui.Pixels;

public class WebPixels8 extends Pixels {
	public WebPixels8(int width, int height, ByteBuffer data) {
		super(width, height, data);
	}

	public WebPixels8(int width, int height, IntBuffer data) {
		super(width, height, data);
	}

	public WebPixels8(int width, int height, IntBuffer data, float scale) {
		super(width, height, data, scale);
	}

	@Override
	protected void _fillDirectByteBuffer(ByteBuffer bb) {
	}

	@Override
	protected void _attachInt(long ptr, int w, int h, IntBuffer ints, int[] array, int offset) {
	}

	@Override
	protected void _attachByte(long ptr, int w, int h, ByteBuffer bytes, byte[] array, int offset) {
	}
}