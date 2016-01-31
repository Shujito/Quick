package org.shujito.quick;

import org.shujito.quick.models.Session;
import org.shujito.quick.models.User;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spark.HaltException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

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
			User user = QUICK_SERVICE.validateSession(sessionBytes);
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
			User user = QUICK_SERVICE.validateSession(sessionBytes);
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
		//System.out.println("method:" + request.requestMethod());
		if ("POST".equals(request.requestMethod())) {
			String username = request.queryParams("username");
			String email = request.queryParams("email");
			String password = request.queryParams("password");
			String confirm = request.queryParams("confirm");
			try {
				QUICK_SERVICE.signInUser(username, email, password, confirm);
				String emailEncoded = URLEncoder.encode(email, "utf-8");
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
			User user = QUICK_SERVICE.validateSession(sessionBytes);
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
		return new ModelAndView(EMPTY, "quick");
	}
}
