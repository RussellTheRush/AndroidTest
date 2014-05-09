package com.example.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class WebServer {
	public static boolean RUNNING = false;
	public static int serverPort = 6369;
	public static final int SOCKET_READ_TIMEOUT = 5000;

	private Context mContext;

	private String GET_INFO_PATTERN = "/getpackageinfo*";

	private ServerSocket mServerSocket;
	
	private Thread mServerThread;
	private AsyncRunner mAsync;
	
	private Set<Socket> mSocketPool = new HashSet<Socket>();
	
	public interface AsyncRunner {
		public void exec(Runnable runnable);
	}
	
	public static class DefaultAsyncRunner implements AsyncRunner {
		private long execCount;
		@Override
		public void exec(Runnable runnable) {
			execCount++;
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			t.setName("PP Web Service Async Thread #" + execCount);
			t.run();
		}
		
	}

	public WebServer(Context context) {
		this.setContext(context);
		setAsyncRunner(new DefaultAsyncRunner());
	}

	public void setAsyncRunner(DefaultAsyncRunner defaultAsyncRunner) {
		mAsync = defaultAsyncRunner;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void runServer() {
		try {
			mServerSocket = new ServerSocket(serverPort);
			mServerSocket.setReuseAddress(true);
			Log.w("RRR", "Server started!");
			mServerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (RUNNING) {
						try {
							Log.w("RRR", "loop");
							final Socket socket = mServerSocket.accept();
							registerSocket(socket);
							socket.setSoTimeout(SOCKET_READ_TIMEOUT);
							final InputStream is = socket.getInputStream();
							mAsync.exec(new Runnable() {
								@Override
								public void run() {
									
								}
							});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			mServerThread.setDaemon(true);
			mServerThread.setName("PP Web Service Thread");
			mServerThread.start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		RUNNING = false;
	}

	public synchronized void startServer() {
		RUNNING = true;
		runServer();
	}
	
	private synchronized void registerSocket(Socket s) {
		mSocketPool.add(s);
	}

	public synchronized void stopServer() {
		RUNNING = false;
		Log.w("RRR", "stopServer");
		if (mServerSocket != null) {
			try {
				mServerSocket.close();
				mServerSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public Context getContext() {
		return mContext;
	}
}