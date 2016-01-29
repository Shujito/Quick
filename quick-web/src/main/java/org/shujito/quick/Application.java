package org.shujito.quick;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

public class Application {
	public static void main(String[] args) {
		//SQLiteDatabase.getConnection();
		String what = URLConnection.guessContentTypeFromName("music.png");
		Spark.port(9999);
		Spark.staticFileLocation("public");
		Spark.externalStaticFileLocation("public");
		JadeTemplateEngine jadeTemplateEngine = new JadeTemplateEngine() {
			@Override
			public String render(ModelAndView modelAndView) {
				String rendered = super.render(modelAndView);
				Map<String, Object> model = new HashMap<>();
				model.put("template", rendered);
				ModelAndView newModelAndView = new ModelAndView(model, "_layout");
				return super.render(newModelAndView);
			}
		};
		Spark.exception(Exception.class, (exception, request, response) -> {
			response.status(500);
			response.body("this happened: '" + exception + "'");
		});
		Spark.before("/*", (request, response) -> {
			String uri = request.uri();
			//System.out.println("path:'" + uri + "'");
			if (uri.length() > 1 && uri.endsWith("/")) {
				response.redirect(uri.substring(0, uri.length() - 1));
			}
		});
		Spark.before("/me", PageController::beforeMe);
		Spark.get("/", PageController::index, jadeTemplateEngine);
		Spark.get("/login", PageController::login, jadeTemplateEngine);
		Spark.post("/login", PageController::login, jadeTemplateEngine);
		Spark.get("/signin", PageController::signin, jadeTemplateEngine);
		Spark.post("/signin", PageController::signin, jadeTemplateEngine);
		Spark.get("/me", PageController::me, jadeTemplateEngine);
		Spark.get("/quick", PageController::quick, jadeTemplateEngine);
		//Spark.get("/s/:id", PageController::quick, jadeTemplateEngine);
	}
}
