package com.example.webserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Response {
    
	private IStatus mStatus;

	private String mMimeType;

	private InputStream mData;

	private Map<String, String> mHeaders = new HashMap<String, String>();
	
	private boolean mChunkedTransfer;
	
	private Method mRequestMethod;
	
	public Response(String msg) {
		this(Status.OK, WebServer.MIME_HTML, msg);
	}
	
	public Response(IStatus status, String mimeType, InputStream data) {
		mStatus = status;
		mMimeType = mimeType;
		mData = data;
	}

	public Response(IStatus status, String mimeType, String text) {
		mStatus = status;
		mMimeType = mimeType;
		try {
			mData = text != null ? new ByteArrayInputStream(text.getBytes("UTF-8")) : null;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
	}
	
	public enum Status implements IStatus {
	    SWITCH_PROTOCOL(101, "Switching Protocols"),
	    OK(200, "OK"),
	    CREATED(201, "Created"),
	    ACCEPTED(202, "Accepted"),
	    NO_CONTENT(204, "No Content"),
	    PARTIAL_CONTENT(204, "Partial Content"),
	    REDIRECT(301, "Moved Permanently"),
	    NOT_MODIFIED(304, "Not Modified"),
	    BAD_REQUEST(400, "Bad Request"),
	    UNAUTHORIZED(401, "Unauthorized"),
	    FORBIDDEN(403, "Forbidden"),
	    NOT_FOUND(404, "Not Found"),
	    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	    RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
	    INTERNEL_ERROR(500, "Internal Server Error");
	    private Status(int requestStatus, String desc) {
	        mRequestStatus = requestStatus;
	        mDescription = desc;
	    }
	    private int mRequestStatus;
	    private String mDescription;
        @Override
        public int getRequestStatus() {
            return mRequestStatus;
        }
        public String getDescription() {
            return "" + this.mRequestStatus + " " + mDescription;
        }
	}

	public void addHeaders(String name, String value) {
		mHeaders.put(name, value);
	}

	public String getHeader(String name) {
		return mHeaders.get(name);
	}
	
	
	private void genOutput(OutputStream out) {
	 // String mime = mMimeType;
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            if (mStatus == null) {
                throw new Error("sendResponse(): Status can not be null.");
            }
//          ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(out);
            pw.print("HTTP/1.1 " + mStatus.getDescription() + "\r\n");
            if (mMimeType != null) {
                pw.print("Content-Type: " + mMimeType + "\r\n");
            }
            
            if (mHeaders == null || mHeaders.get("Date") == null) {
                pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
            }
            
            if (mHeaders != null) {
                for (String key : mHeaders.keySet()) {
                    String value = mHeaders.get(key);
                    pw.print(key  + ": " + value + "\r\n");
                }
            }
            
            sendConnectionHeaderIfNotAlreadyPresent(pw, mHeaders);
            if (mRequestMethod != Method.HEAD && mChunkedTransfer) {
                sendAsChunked(out, pw);
            } else {
                int pendding = mData != null ? mData.available() : 0;
                sendContentLengthHeaderIfNotAlreadyPresent(pw, mHeaders, pendding);
                pw.print("\r\n");
                pw.flush();
                
                sendAsFixedLength(out, pendding);
            }
            out.flush();
        } catch(IOException ioe) {
        }
	}
	protected void send(OutputStream out) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    genOutput(baos);
	    byte buf[] = baos.toByteArray();
	    try {
            out.write(buf, 0, buf.length);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	    
	}

	private void sendAsChunked(OutputStream out, PrintWriter pw) throws IOException {
		pw.print("Transfer-Encoding: chunked\r\n");
		pw.print("\r\n");
		pw.flush();
		int BUF_SIZE = 16 * 1024;
		byte[] CRLF = "\r\n".getBytes();
		byte[] buf = new byte[BUF_SIZE];
		int read;

		while ((read = mData.read(buf)) > 0) {
			out.write(String.format("%x\r\n", read).getBytes());
			out.write(buf, 0, read);
			out.write(CRLF);
		}

		out.write(String.format("0\r\n\r\n").getBytes());
	}

	private void sendAsFixedLength(OutputStream out, int pendding) throws IOException {
		if (mRequestMethod != Method.HEAD && mData != null) {
			int BUF_SIZE = 16 * 1024;
			byte[] buf = new byte[BUF_SIZE];
			while (pendding > 0) {
				int read = mData.read(buf, 0, ((pendding > BUF_SIZE) ? BUF_SIZE : pendding));
				if (read <= 0) {
					break;
				}
				out.write(buf, 0, read);
				pendding -= read;
			}
		}
	}

	private void sendConnectionHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> headers) {
		if (!headerAlreadySent(headers, "Connection")) {
			pw.print("Connection: keep-alive\r\n");	
		}
	}

	private boolean headerAlreadySent(Map<String, String> headers, String name) {
		boolean res = false;
		for (String key : headers.keySet()) {
			if (key.equalsIgnoreCase(name)) {
				res = true;
				break;
			}
		}

		return res;
	}

	private void sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> headers, int size) {
		if (!headerAlreadySent(headers, "Content-Length")) {
			pw.print("Content-Length: " + size + "\r\n");
		}
	}

    public void setRequestMethod(Method mMethod) {
        mRequestMethod = mMethod;
    }
	
}
