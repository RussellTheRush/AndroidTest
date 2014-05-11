package com.example.webserver;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class HTTPSession implements IHTTPSession {
	private final static int BUF_SIZE = 8192;
	private PushbackInputStream mIn;
	private OutputStream mOut;
	private Map<String, String> mHeaders;
	private Map<String, String> mParams;
	private int mRlen;
	private Method mMethod;
	private String mUri;
	private boolean mIsClosed;

	public HTTPSession(InputStream in, OutputStream out) {
		mIn = new PushbackInputStream(in, BUF_SIZE);
		mOut = out;
		mHeaders = new HashMap<String, String>();
		mIsClosed = false;
	}

	public void execute() throws IOException  {
		try {
			byte [] buf = new byte[BUF_SIZE];
			int read;
			try {
				read = mIn.read(buf, 0, BUF_SIZE);
			} catch(Exception e) {
				throw new SocketException("socket shutdown");
			}

			if (read == -1) {
				mIsClosed = true;
				return;
			}
			int hlen = 0;
			while (read > 0) {
				mRlen += read;
				hlen = findHeaderEnd(buf, mRlen);
				if (hlen > 0) {
					break;
				}
				read = mIn.read(buf, mRlen, BUF_SIZE - mRlen);
			}

			if (hlen < mRlen) {
				mIn.unread(buf, hlen, mRlen - hlen);
			}

			BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, mRlen)));
			
			Map<String, String> pre = new HashMap<String, String>();
			decodeHeaders(hin, pre, mParams, mHeaders);
			mMethod = Method.lookup(pre.get("method"));
			if (mMethod == null) {
				throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error.");
			}
			Log.w("RRR", "begin serve");
			Response r = serve(this);
			if (r == null) {
				throw new ResponseException(Response.Status.INTERNEL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
			} else {                                       
				r.setRequestMethod(mMethod);
				r.send(mOut);
			}

		} catch (SocketException e) {
			throw e;
		} catch (SocketTimeoutException ste) {
			throw ste;
		} catch (IOException ioe) {
			Response r = new Response(Response.Status.INTERNEL_ERROR, WebServer.MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			r.send(mOut);
			mIsClosed = true;
		} catch (ResponseException re) {
			Response r = new Response(re.getStatus(), WebServer.MIME_PLAINTEXT, re.getMessage());
			r.send(mOut);
			mIsClosed = true;
		}
	}

	public Response serve(IHTTPSession session) {
		String msg = "<html><body><h1>Hello world!</h1><p>HHHHH</p></body></html>";
		return new Response(msg);
	}

	private void decodeHeaders(BufferedReader hin, Map<String, String> pre, Map<String, String> params, Map<String, String>headers) throws ResponseException {
		try {
			String line = hin.readLine();
			if (line == null) {
				return;
			}
			StringTokenizer st = new StringTokenizer(line);
			if (!st.hasMoreTokens()) {
				throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
			}

			pre.put("method", st.nextToken());

			if (!st.hasMoreTokens()) {
				throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Missing URI, useage: GET /example/file.html");
			}

			String uri = st.nextToken();

			int pi = uri.indexOf('?');
			if (pi >= 0) {
				decodeParams(uri.substring(pi+1), params);
				uri = decodePercent(uri.substring(0, pi));
			} else {
				uri = decodePercent(uri);
			}

			if (st.hasMoreTokens()) {
				String l = hin.readLine();
				while (l != null && l.trim().length() > 0) {
					int p = l.indexOf(':');
					if (p >= 0) {
						headers.put(l.substring(0, p).trim().toLowerCase(Locale.US), l.substring(p+1).trim());
					}
					l = hin.readLine();
				}
			}

			pre.put("uri", uri);
			mUri = uri;
		} catch(IOException e) {
			throw new ResponseException(Response.Status.INTERNEL_ERROR, "SERVER INTERNAL ERROR: IOException: " + e.getMessage(), e);
		}

	}

	private void decodeParams(String params, Map<String, String> p) {
		if (params == null) {
			return ;
		}

		StringTokenizer st = new StringTokenizer(params, "&");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf("=");
			if (sep >= 0) {
				p.put(decodePercent(e.substring(0, sep)).trim(),
						decodePercent(e.substring(sep+1)));
			} else {
				p.put(decodePercent(e).trim(), "");
			}
		}
	}

	protected String decodePercent(String str) {
		String decoded = null;
		try {
			decoded = URLDecoder.decode(str, "UTF8");
		} catch(UnsupportedEncodingException e) {}

		return decoded;
	}

	private int findHeaderEnd(byte buf[], int len) {
		int i = 0;
		for (i=0; i + 3<len; i++) {
			if (buf[i] == '\r' && buf[i+1] == '\n'
				&& buf[i+2] == '\r' && buf[i+3] == '\n') {
				return i+4;
			}
		}
		return -1;
	}

    @Override
    public Map<String, String> getParams() {
        return mParams;
    }

    @Override
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    @Override
    public String getUri() {
        return mUri;
    }

    @Override
    public Method getMethod() {
        return mMethod;
    }

    @Override
    public InputStream getInputStream() {
        return mIn;
    }

    @Override
    public void parseBody(Map<String, String> files) throws IOException, ResponseException {
        
    }

    public boolean isClosed() {
        
        return mIsClosed;
    }
}
