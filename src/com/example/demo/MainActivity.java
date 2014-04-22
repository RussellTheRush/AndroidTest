package com.example.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Random;

import com.pp.lib.jpeglib.JpegUtil;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	

	private TextView tv_info;
	private WifiManager mWifiManager;
	private List<ScanResult> mWifiList;  
	private List<WifiConfiguration> mWifiConfiguration;
	private ImageView mImageView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mImageView = (ImageView)findViewById(R.id.imageView1);
		tv_info = (TextView)findViewById(R.id.textView2);
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
		mWifiManager.startScan();  
		mWifiList = mWifiManager.getScanResults();  
		mWifiConfiguration = mWifiManager.getConfiguredNetworks(); 
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (WifiConfiguration s : mWifiConfiguration) {
			sb.append("ScanResult[" + i++ +"]: " + s + "\r\n");
		}
		d = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/ddd/abc/");
		nd = new File(this.getCacheDir().getPath());
		Log.w("RRR", "d:" + d.getAbsolutePath() + "\nnd:" + nd.getAbsolutePath());
		tv_info.setText(sb.toString());
		//append a line
        //append a line by russell.
		//resolve the conflict by xiongzhiwei  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		//append a line by xiongzhiwei.
	}
	
	private Random sr = new Random();
	File d;
	File nd;
	public void onClickBtn(View view) {
		JpegUtil.decompressToFileNative("/storage/sdcard0/2014-04-15_11-17-10.jpg", "/storage/sdcard0/new2014.bmp");
		//BitmapFactory.Options
		//Bitmap bitmap = BitmapFactory.decodeFile("/storage/sdcard0/new2014.bmp", );
	//	mImageView.setImageBitmap(bitmap);
//		if (!d.exists()) {
//			boolean b = d.mkdirs();
//			Log.w("RRR", "d mkdirs return:" + b);
//		}
//		
//		File f = new File(d, sr.nextInt() + ".txt");
//		FileOutputStream fos;
//		try {
//			fos = new FileOutputStream(f);
//			Log.w("RRR", "write to file:" + f.getAbsolutePath());
//			fos.write("HHHHHH".getBytes());
//			Thread.sleep(60*1000);
//			fos.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		if (!nd.exists()) {
//			boolean b = nd.mkdirs();
//			Log.w("RRR", "nd mkdirs return:" + b);
//		}
//		File nf = new File(nd, sr.nextInt() + ".txt");
//		FileOutputStream nfos;
//		try {
//			nfos = new FileOutputStream(nf);
//			Log.w("RRR", "write to file:" + nf.getAbsolutePath());
//			nfos.write("HHHHHH".getBytes());
//			nfos.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//new Thread(r).start();
//		Bitmap bitmap = null;
//		FileInputStream in = null;
//		try {
//			//in = new FileInputStream("/storage/sdcard0/abc.jpg");
//			in = new FileInputStream("/storage/sdcard0/111.jpg");
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			byte []buf = new byte[1024];
//			int len;
//			while ((len = in.read(buf, 0, 1024)) != -1) {
//				baos.write(buf, 0, len);
//				baos.flush();
//			}
//			byte mapbits[];
//			mapbits = baos.toByteArray();
//			Log.w("RRR", "Begin decompress.");
//			bitmap = JpegUtil.decompress(mapbits, mapbits.length, 300, 200);
//			mImageView.setImageBitmap(bitmap);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
//		try {
//			in = new FileInputStream("/storage/sdcard0/abc.jpg");
//			//in = new FileInputStream("/storage/sdcard0/111.jpg");
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			byte []buf = new byte[1024];
//			int len;
//			while ((len = in.read(buf, 0, 1024)) != -1) {
//				baos.write(buf, 0, len);
//				baos.flush();
//			}
//			byte mapbits[];
//			mapbits = baos.toByteArray();
//			Log.i("RRR", "byte array length: " + mapbits.length);
//			Log.w("RRR", "Begin decompress.");
//			bitmap = BitmapFactory.decodeByteArray(mapbits, 0, mapbits.length);
//			Log.w("RRR", "end decompress.");
//			in.close();
//			mImageView.setImageBitmap(bitmap);
//		} catch (OutOfMemoryError e) {
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		if (bitmap == null) {
//			Log.e("RRR", "Decode error.");
//		} else {
//			Log.i("RRR", "Decode success.");
//		}
	}
	
	private Runnable r = new Runnable() {
		
		public void run() {
			String strUrl = "http://passport-i.25pp.com:8080/i";
			//String strUrl = "http://122.13.176.126:8080/i";
			HttpURLConnection mHttpUrlConnection = null;
			URL mUrl = null;
			Log.w("RRR", "onClick");
			try {
				mUrl = new URL(strUrl);
				if (mUrl == null)
					return;
				mHttpUrlConnection = (HttpURLConnection) mUrl.openConnection();
				if (mHttpUrlConnection == null)
					return;
				Log.w("RRR", "Got connection");
				mHttpUrlConnection.setRequestMethod("POST");
				mHttpUrlConnection.setConnectTimeout(100 * 1000);
				mHttpUrlConnection.setReadTimeout(100 * 1000);
				mHttpUrlConnection.setDoOutput(true);
				mHttpUrlConnection.setDoInput(true);
				mHttpUrlConnection.setUseCaches(false);
				mHttpUrlConnection.setRequestProperty("Charset", "UTF-8");
				
				OutputStream out = mHttpUrlConnection.getOutputStream();
				ByteBuffer buffer = ByteBuffer.allocate(8+ 8);
		        buffer.order(ByteOrder.LITTLE_ENDIAN);
		        buffer.putInt(0); 				
		        buffer.putInt(0xAA000013);
		        buffer.putInt(0x11223344);
		        buffer.flip();
		        buffer.putInt(buffer.limit());
		        buffer.rewind();
				out.write(buffer.array(), 0, buffer.limit());
				out.flush();
				out.close();
				if (mHttpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					Log.w("RRR", "Hook on");
				}
			} catch (Exception e) {
				//ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
				//PrintWriter w = new PrintWriter(b);
				e.printStackTrace();
				Log.w("RRR", "Exception: " + e);
				return;
			} finally {
				if (mHttpUrlConnection != null) mHttpUrlConnection.disconnect();
			}
		}
	};
	
}
