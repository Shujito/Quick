package org.shujito.quick;

import java.util.HashMap;
import java.util.Map;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.FileTemplateLoader;
import spark.ModelAndView;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

public class Application {
	public static void main(String[] args) {
		Spark.port(9999);
		Spark.staticFileLocation("public");
		Spark.externalStaticFileLocation("public");
		JadeConfiguration jcon = new JadeConfiguration();
		jcon.setCaching(false);
		jcon.setTemplateLoader(new FileTemplateLoader("./src/main/resources/templates/", "UTF-8"));
		JadeTemplateEngine jadeTemplateEngine = new JadeTemplateEngine(jcon) {
			@Override
			public String render(ModelAndView modelAndView) {
				String rendered = super.render(modelAndView);
				Map<String, Object> model = new HashMap<>();
				model.put("template", rendered);
				model.put("model", modelAndView.getModel());
				ModelAndView newModelAndView = new ModelAndView(model, "_layout");
				return super.render(newModelAndView);
			}
		};
		Spark.exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
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
		Spark.before("/login", PageController::beforeLoginSignin);
		Spark.before("/signin", PageController::beforeLoginSignin);
		Spark.get("/", PageController::index, jadeTemplateEngine);
		Spark.get("/login", PageController::login, jadeTemplateEngine);
		Spark.post("/login", PageController::login, jadeTemplateEngine);
		Spark.get("/signin", PageController::signin, jadeTemplateEngine);
		Spark.post("/signin", PageController::signin, jadeTemplateEngine);
		Spark.get("/me", PageController::me, jadeTemplateEngine);
		Spark.post("/me", PageController::me, jadeTemplateEngine);
		Spark.get("/quick", PageController::quick, jadeTemplateEngine);
		Spark.post("/quick", "multipart/form-data", PageController::quick, jadeTemplateEngine);
		Spark.get("/s/:id", PageController::quick, jadeTemplateEngine);
	}
}
