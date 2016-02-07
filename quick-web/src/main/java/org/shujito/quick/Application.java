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
		PageController pageController = new PageController();
		Spark.before("/me", pageController::before);
		Spark.before("/login", pageController::before);
		Spark.before("/signin", pageController::before);
		Spark.get("/", pageController::index, jadeTemplateEngine);
		Spark.get("/login", pageController::login, jadeTemplateEngine);
		Spark.post("/login", pageController::login, jadeTemplateEngine);
		Spark.get("/signin", pageController::signin, jadeTemplateEngine);
		Spark.post("/signin", pageController::signin, jadeTemplateEngine);
		Spark.get("/logout", pageController::logout, jadeTemplateEngine);
		Spark.get("/me", pageController::me, jadeTemplateEngine);
		Spark.post("/me", pageController::me, jadeTemplateEngine);
		Spark.get("/quick", pageController::quick, jadeTemplateEngine);
		Spark.post("/quick", "multipart/form-data", pageController::quick, jadeTemplateEngine);
		//Spark.get("/q/:id", pageController::quick, jadeTemplateEngine);
	}
}
