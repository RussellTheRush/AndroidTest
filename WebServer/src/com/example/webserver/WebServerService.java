package com.example.webserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class WebServerService extends Service {

	private WebServer mServer = null;

	@Override
	public void onCreate() {
		Log.i("RRR", "Creating and starting httpService");
		super.onCreate();

		mServer = new WebServer(WebServerService.this);
		mServer.startServer();

	}

	@Override
	public void onDestroy() {
		Log.i("RRR", "Destroying httpService");
		mServer.stopServer();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}

