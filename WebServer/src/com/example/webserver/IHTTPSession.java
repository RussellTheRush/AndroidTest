package com.example.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface IHTTPSession {
	public void execute() throws IOException;
	public Map<String, String> getParams();
	public Map<String, String> getHeaders();
	public String getUri();
	public Method getMethod();
	public InputStream getInputStream();
	public void parseBody(Map<String, String> files) throws IOException, ResponseException;
}
