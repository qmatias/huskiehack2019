/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.matiaskotlik.huskiehack2019;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class App extends NanoHTTPD {
	private Template loginTmp;
	private Template btnTmp;

	private SessionList sessionList;

	private Response response404;

	public App(int port) {
		super(port);

		sessionList = new SessionList();

		loginTmp = getTemplate("/index.html");
		btnTmp = getTemplate("/buttonpage.html");

		response404 = Response.newFixedLengthResponse("Error 404 File Not Found");
	}

	public Session getSession(IHTTPSession ihttpSession) {
		return sessionList.getSession(getSessionId(ihttpSession));
	}

	public String getSessionId(IHTTPSession ihttpSession) {
		String id = ihttpSession.getCookies().read("session");
		id = id == null ? UUID.randomUUID().toString() : id;
		setSessionId(ihttpSession, id);
		return id;
	}

	public void setSessionId(IHTTPSession ihttpSession, String id) {
		ihttpSession.getCookies().set("session", id, Integer.MAX_VALUE);
	}

	public Map<String, String> getSessionDataMap(Session session) {
		Map<String, String> map = new HashMap<>();
		if (session.getName() != null) {
			map.put("name", session.getName());
		}
		return map;
	}

	@Override
	public Response serve(IHTTPSession ihttpSession) {
		Method method = ihttpSession.getMethod();
		String uri = ihttpSession.getUri();
		try {
			ihttpSession.parseBody(new HashMap<>());
		} catch (IOException e) {
			System.err.println("Could not parse body");
		} catch (ResponseException e) {
			System.err.println("ResponseException when trying to parse body");
		}

		System.out.println(getSessionDataMap(getSession(ihttpSession)));
		System.out.println(sessionList);

		if (uri.equals("/")) {
			Session session = getSession(ihttpSession);
			if (session.getName() != null) {
				return ss(btnTmp.render(getSessionDataMap(session)));
			} else {
				return ss(loginTmp.render(getSessionDataMap(session)));
			}
		} else if (uri.equals("/signin")) {
//			System.out.println(ihttpSession.getParms());
			Session session = getSession(ihttpSession);
			session.setName(ihttpSession.getParms().get("name"));
			return redirect("/");
		}
		return response404;
	}

	public Response redirect(String uri) {
		return Response.newFixedLengthResponse("<script>window.location.replace(\"" + uri +"\");</script>");
	}

	public Response ss(String msg) {
		return Response.newFixedLengthResponse(msg);
	}

	public static void main(String[] args) {
		int port = 0;
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		App app = new App(port);
		System.out.println("Starting server...");
		try {
			app.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e) {
			System.err.println("Couldn't start server.");
			e.printStackTrace();
		}
	}

	public Template getTemplate(String path) {
		try {
			return new Template(getResource(path));
		} catch (IOException e) {
			System.err.println("Could not open resource at path " + path);
		}
		return new Template("Could not open resource");
	}

	public String getResource(String path) throws IOException {
		InputStream in = getClass().getResourceAsStream(path);
		return inputStreamToString(in);
	}

	public String inputStreamToString(InputStream inputStream) throws IOException {
		try(ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			return result.toString("UTF-8");
		}
	}
}
