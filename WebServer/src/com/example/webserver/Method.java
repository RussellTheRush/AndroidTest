package com.example.webserver;

public enum Method {
	GET, PUT, POST, DELETE, HEAD, OPTIONS;

	static Method lookup(String	method) {
		for (Method m : Method.values()) {
			if (m.toString().equalsIgnoreCase(method)) {
				return m;
			}
		}

		return null;
	}
}
