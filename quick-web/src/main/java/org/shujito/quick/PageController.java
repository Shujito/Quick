package org.shujito.quick;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

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
		Map<String, Object> model = new HashMap<>();
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
