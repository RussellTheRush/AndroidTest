package com.example.webserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

//import org.apache.http.ConnectionReuseStrategy;
//import org.apache.http.Header;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpException;
//import org.apache.http.HttpRequest;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpResponseFactory;
//import org.apache.http.HttpServerConnection;
//import org.apache.http.entity.ContentProducer;
//import org.apache.http.entity.EntityTemplate;
//import org.apache.http.impl.DefaultConnectionReuseStrategy;
//import org.apache.http.impl.DefaultHttpResponseFactory;
//import org.apache.http.impl.DefaultHttpServerConnection;
//import org.apache.http.params.BasicHttpParams;
//import org.apache.http.protocol.BasicHttpContext;
//import org.apache.http.protocol.BasicHttpProcessor;
//import org.apache.http.protocol.HttpContext;
//import org.apache.http.protocol.HttpProcessor;
//import org.apache.http.protocol.HttpRequestHandler;
//import org.apache.http.protocol.HttpRequestHandlerRegistry;
//import org.apache.http.protocol.HttpService;
//import org.apache.http.protocol.ResponseConnControl;
//import org.apache.http.protocol.ResponseContent;
//import org.apache.http.protocol.ResponseDate;
//import org.apache.http.protocol.ResponseServer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class WebServer {
	public static int serverPort = 6369;
	public static final int SOCKET_READ_TIMEOUT = 5000;
	
    public static final String MIME_HTML = "text/html";
    public static final String MIME_PLAINTEXT = "text/plain";

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
					while (!mServerSocket.isClosed()) {
						try {
							Log.w("RRR", "loop");
							final Socket socket = mServerSocket.accept();
							Log.w("RRR", "accept a socket");
							registerSocket(socket);
							socket.setSoTimeout(SOCKET_READ_TIMEOUT);
							final InputStream in = socket.getInputStream();
							mAsync.exec(new Runnable() {
								@Override
								public void run() {
									OutputStream out = null;
									try {
										out = socket.getOutputStream();
										HTTPSession session = new HTTPSession(in, out);
										while (!session.isClosed() && !socket.isClosed()) {
											session.execute();
										}
									} catch (Exception e) {
										e.printStackTrace();
									} finally {
										safeClose(out);
										safeClose(in);
										safeClose(socket);
										unRegisterSocket(socket);
									}
									
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
	}

	public synchronized void startServer() {
		runServer();
	}

	private final static void safeClose(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private final static void safeClose(Socket socket) {
	    if (socket != null) {
	        try {
	            socket.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	private synchronized void registerSocket(Socket s) {
		mSocketPool.add(s);
	}

	private synchronized void unRegisterSocket(Socket s) {
		mSocketPool.remove(s);
	}

	public synchronized void stopServer() {
		Log.w("RRR", "stopServer");
		if (mServerSocket != null) {
			try {
				mServerSocket.close();
				mServerSocket = null;
				closeSocketPool();
				if (mServerThread != null) {
				    mServerThread.join();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void closeSocketPool() {
	    for (Socket s : mSocketPool) {
	        safeClose(s);
	    }
	}
	public Context getContext() {
		return mContext;
	}
}
