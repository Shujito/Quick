package org.shujito.quick;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import org.apache.commons.io.FileUtils;
import org.shujito.quick.models.Quick;
import org.shujito.quick.models.Session;
import org.shujito.quick.models.User;
import org.shujito.quick.utils.Crypto;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

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
	public static final String ATTRIBUTE_USER = "user";
	public static final String ATTRIBUTE_ACCESS_TOKEN = "access_token";
	private final Map<String, Object> EMPTY = new HashMap<>();
	private final QuickService quickService;

	public PageController() {
		this.quickService = new QuickService(SQLiteDatabase.getConnection());
	}

	public void before(Request request, Response response) throws Exception {
		String b64Session = request.session().attribute(ATTRIBUTE_ACCESS_TOKEN);
		if (b64Session == null) {
			return;
		}
		byte[] sessionBytes = Crypto.base64decode(b64Session);
		User user = this.quickService.getUserFromSession(sessionBytes);
		request.session().attribute(ATTRIBUTE_USER, user);
	}

	/**
	 * Corresponding route: /
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView index(Request request, Response response) {
		Map<String, Object> model = new HashMap<>();
		try {
			List<Quick> quicks = this.quickService.getActiveQuicks();
			List<Map<String, Object>> quicksMapList = new ArrayList<>();
			for (Quick quick : quicks) {
				Map<String, Object> quickMap = new HashMap<>();
				quickMap.put("id", quick.getId());
				quickMap.put("name", quick.getName());
				quickMap.put("description", quick.getDescription());
				quickMap.put("content_size", FileUtils.byteCountToDisplaySize(quick.getContentSize()));
				quickMap.put("content_hash", HexBin.encode(quick.getContentHash()).toLowerCase());
				quicksMapList.add(quickMap);
			}
			model.put("quicks", quicksMapList);
		} catch (Exception e) {
			e.printStackTrace();
			model.put("error", e.toString());
		}
		//Date date = new Date();
		//model.put("milliseconds", date.getTime());
		return new ModelAndView(model, "index");
	}

	/**
	 * Corresponding route: /login
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView login(Request request, Response response) {
		if (request.session().attribute(ATTRIBUTE_USER) != null) {
			response.redirect("/me");
			Spark.halt();
		}
		String email = request.queryParams("email");
		String password = request.queryParams("password");
		Map<String, Object> model = new HashMap<>();
		if ("POST".equals(request.requestMethod())) {
			try {
				Session session = this.quickService.logInUser(email, password, request.userAgent());
				String accessToken = Crypto.base64encode(session.getAccessToken());
				request.session().attribute(ATTRIBUTE_ACCESS_TOKEN, accessToken);
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
	public ModelAndView signin(Request request, Response response) {
		if (request.session().attribute(ATTRIBUTE_USER) != null) {
			response.redirect("/me");
			Spark.halt();
		}
		if ("POST".equals(request.requestMethod())) {
			String username = request.queryParams("username");
			String email = request.queryParams("email");
			String password = request.queryParams("password");
			String confirm = request.queryParams("confirm");
			try {
				User user = this.quickService.signInUser(username, email, password, confirm);
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
	 * Corresponding route: /logout
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView logout(Request request, Response response) {
		request.session().removeAttribute(ATTRIBUTE_USER);
		response.redirect("/login");
		return null;
	}

	/**
	 * Corresponding route: /me
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView me(Request request, Response response) {
		User user = request.session().attribute(ATTRIBUTE_USER);
		if (user == null) {
			response.redirect("/login");
			Spark.halt();
		} else {
			Map<String, Object> model = new HashMap<>();
			model.put("username", user.getUsername());
			model.put("display_name", user.getDisplayName());
			model.put("email", user.getEmail());
			return new ModelAndView(model, "me");
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
	public ModelAndView quick(Request request, Response response) {
		User user = request.session().attribute(ATTRIBUTE_USER);
		if ("POST".equals(request.requestMethod())) {
			long maxFileSize = 1024 * 1024 * 50;
			long maxRequestSize = 1024 * 1024 * 50;
			int fileSizeThreshold = 1024;
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp/quick", maxFileSize, maxRequestSize, fileSizeThreshold);
			request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
			try {
				Part contentsPart = request.raw().getPart("contents");
				String contentType = contentsPart.getHeader("content-type");
				String name = request.queryParams("name");
				String description = request.queryParams("description");
				try (final InputStream is = contentsPart.getInputStream()) {
					try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
						IOUtils.copy(is, baos);
						byte[] contentBytes = baos.toByteArray();
						System.out.println("file is " + contentBytes.length + " bytes long");
						//Quick quick = this.quickService.uploadQuick(user, contentBytes, contentType, name, description);
						//response.redirect("/s/:id");
						this.quickService.uploadQuick(user, contentBytes, contentType, name, description);
						response.redirect("/");
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return new ModelAndView(EMPTY, "quick");
	}
}
