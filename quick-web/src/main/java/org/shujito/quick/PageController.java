package org.shujito.quick;

import org.shujito.quick.models.Quick;
import org.shujito.quick.models.Session;
import org.shujito.quick.models.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import spark.HaltException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.utils.IOUtils;

/**
 * @author shujito
 */
public class PageController {
	public static final String TAG = PageController.class.getSimpleName();
	static final Map<String, Object> EMPTY = new HashMap<>();
	static final QuickService QUICK_SERVICE;

	static {
		QUICK_SERVICE = new QuickService(SQLiteDatabase.getConnection());
	}

	/**
	 * Check if it isn't logged in
	 *
	 * @param request
	 * @param response
	 */
	public static void beforeMe(Request request, Response response) {
		String b64Session = request.cookie("session");
		byte[] sessionBytes = Crypto.base64decode(b64Session);
		try {
			User user = QUICK_SERVICE.getUserFromSession(sessionBytes);
			if (user == null) {
				response.redirect("/login");
				Spark.halt();
			}
		} catch (Exception e) {
			if (!(e instanceof HaltException)) {
				e.printStackTrace();
			}
			response.redirect("/login");
		}
	}

	/**
	 * Check if it is logged in
	 *
	 * @param request
	 * @param response
	 */
	public static void beforeLoginSignin(Request request, Response response) {
		String b64Session = request.cookie("session");
		byte[] sessionBytes = Crypto.base64decode(b64Session);
		try {
			User user = QUICK_SERVICE.getUserFromSession(sessionBytes);
			if (user != null) {
				response.redirect("/me");
				Spark.halt();
			}
		} catch (Exception e) {
			if (!(e instanceof HaltException)) {
				e.printStackTrace();
			}
			response.redirect("/me");
		}
	}

	/**
	 * Corresponding route: /
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public static ModelAndView index(Request request, Response response) {
		Map<String, Object> model = new HashMap<>();
		Date date = new Date();
		model.put("milliseconds", date.getTime());
		return new ModelAndView(model, "index");
	}

	/**
	 * Corresponding route: /login
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public static ModelAndView login(Request request, Response response) {
		String email = request.queryParams("email");
		String password = request.queryParams("password");
		Map<String, Object> model = new HashMap<>();
		if ("POST".equals(request.requestMethod())) {
			try {
				Session session = QUICK_SERVICE.logInUser(email, password, request.userAgent());
				String accessToken = Crypto.base64encode(session.getAccessToken());
				Date now = new Date();
				System.out.println("now:" + now + " millis:" + now.getTime());
				long expiration = session.getExpiresAt().getTime() - now.getTime();
				response.cookie("session", accessToken, (int) (expiration / 1000));
				response.redirect("/me");
			} catch (Exception ex) {
				model.put("error", ex.getMessage());
			}
		}
		model.put("email", email);
		return new ModelAndView(model, "login");
	}

	/**
	 * Corresponding route: /signin
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public static ModelAndView signin(Request request, Response response) {
		if ("POST".equals(request.requestMethod())) {
			String username = request.queryParams("username");
			String email = request.queryParams("email");
			String password = request.queryParams("password");
			String confirm = request.queryParams("confirm");
			try {
				User user = QUICK_SERVICE.signInUser(username, email, password, confirm);
				String emailEncoded = URLEncoder.encode(user.getEmail(), "utf-8");
				response.redirect("/login?email=" + emailEncoded);
			} catch (Exception ex) {
				ex.printStackTrace();
				Map<String, Object> model = new HashMap<>();
				model.put("username", username);
				model.put("email", email);
				model.put("error", ex.getMessage());
				return new ModelAndView(model, "signin");
			}
		}
		return new ModelAndView(EMPTY, "signin");
	}

	/**
	 * Corresponding route: /me
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public static ModelAndView me(Request request, Response response) {
		String b64Session = request.cookie("session");
		byte[] sessionBytes = Crypto.base64decode(b64Session);
		try {
			User user = QUICK_SERVICE.getUserFromSession(sessionBytes);
			Map<String, Object> model = new HashMap<>();
			model.put("username", user.getUsername());
			model.put("display_name", user.getDisplayName());
			model.put("email", user.getEmail());
			return new ModelAndView(model, "me");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ModelAndView(EMPTY, "me");
	}

	/**
	 * Corresponding route: /quick
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public static ModelAndView quick(Request request, Response response) {
		if ("POST".equals(request.requestMethod())) {
			long maxFileSize = 1024 * 1024 * 50;
			long maxRequestSize = 1024 * 1024 * 50;
			int fileSizeThreshold = 1024;
			MultipartConfigElement mce = new MultipartConfigElement("/tmp/stomp", maxFileSize, maxRequestSize, fileSizeThreshold);
			request.raw().setAttribute("org.eclipse.jetty.multipartConfig", mce);
			try {
				Part contents = request.raw().getPart("contents");
				String contentType = contents.getHeader("content-type");
				String name = request.queryParams("name");
				String description = request.queryParams("description");
				try (final InputStream is = contents.getInputStream()) {
					try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
						IOUtils.copy(is, baos);
						byte[] bytes = baos.toByteArray();
						System.out.println("file is " + bytes.length + " bytes long");
						Quick quick = QUICK_SERVICE.uploadQuick(bytes, contentType, name, description);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return new ModelAndView(EMPTY, "quick");
	}
}
