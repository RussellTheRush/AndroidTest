package com.pp.lib.jpeglib;

import android.graphics.Bitmap;

public class JpegUtil {
	static {
		System.loadLibrary("ppjpeg");
	};
	public static native void  decompressNative(byte[] buf, int size, Bitmap bitmap);
	public static native void decompressToFileNative(String inFile, String outFile);
	public static Bitmap decompress(byte []buf, int size, int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//bitmap.compress(format, quality, stream)
		decompressNative(buf, size, bitmap);
		return bitmap;
	}

}
