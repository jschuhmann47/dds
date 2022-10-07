package server;

import models.controllers.*;
import models.entities.organizaciones.entidades.AgenteSectorial;
import models.entities.organizaciones.entidades.Trabajador;
import models.entities.seguridad.cuentas.Permiso;
import models.entities.seguridad.cuentas.Rol;
import models.helpers.PermisoHelper;
import models.middlewares.AuthMiddleware;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import spark.utils.BooleanHelper;
import spark.utils.HandlebarsTemplateEngineBuilder;

public class Router {
    private static HandlebarsTemplateEngine engine;

    private static void initEngine() {
        Router.engine = HandlebarsTemplateEngineBuilder
                .create()
                .withDefaultHelpers()
                .withHelper("isTrue", BooleanHelper.isTrue)
                .build();
    }

    public static void init() {
        Router.initEngine();
        Spark.staticFileLocation("/public");
        Router.configure();
    }

    private static void configure(){

        OrganizacionController organizacionController = new OrganizacionController();
        LoginController loginController = new LoginController();
        MenuController menuController = new MenuController();
        TrabajadorController trabajadorController = new TrabajadorController();
        AdministradorController administradorController = new AdministradorController();
        ErrorHandlerController errorController = new ErrorHandlerController();
        AgenteSectorialController agenteController = new AgenteSectorialController();


        Spark.path("/login", () -> {
            Spark.get("",loginController::inicio, Router.engine);
            Spark.post("",loginController::login);

        });

        Spark.path("/menu",() -> {
            Spark.before("", AuthMiddleware::verificarSesion);
            Spark.before("/*", AuthMiddleware::verificarSesion);
            Spark.get("/logout",loginController::logout); //post o get?
            Spark.get("",menuController::inicio,Router.engine);

            Spark.path("/organizacion", () -> {
                Spark.before("", (request, response) -> {
                   if(!PermisoHelper.usuarioTienePermisos(request, Permiso.VER_ORGANIZACION)){ //todo o el id de ese tipo es null??
                       response.redirect("/prohibido");
                       Spark.halt("Recurso prohibido");
                   }
                });
                Spark.before("/*", (request, response) -> {
                    if(!PermisoHelper.usuarioTienePermisos(request, Permiso.VER_ORGANIZACION)){
                        response.redirect("/prohibido");
                        Spark.halt("Recurso prohibido");
                    }
                });
                Spark.get("",organizacionController::mostrar, Router.engine);
                Spark.get("/vinculaciones",organizacionController::mostrarVinculaciones, Router.engine);
                Spark.get("/medicion",organizacionController::mostrarMedicion, Router.engine);
                Spark.get("/reportes",organizacionController::mostrarReportes, Router.engine);
                Spark.get("/calculadora",organizacionController::mostrarCalculadoraHC, Router.engine);
                Spark.get("/recomendaciones",organizacionController::mostrarRecomendaciones, Router.engine);
            });

            Spark.path("/trabajador", () -> {
                Spark.before("", (request, response) -> {
                    if(!PermisoHelper.usuarioTienePermisos(request, Permiso.VER_TRABAJADOR)){ //todo no funca?
                        response.redirect("/prohibido");
                        Spark.halt("Recurso prohibido");
                    }
                });
                Spark.before("/*", (request, response) -> {
                    if(!PermisoHelper.usuarioTienePermisos(request, Permiso.VER_TRABAJADOR)){
                        response.redirect("/prohibido");
                        Spark.halt("Recurso prohibido");
                    }
                });

                Spark.get("",trabajadorController::mostrar, Router.engine);
//                Spark.get("/calculadora",trabajadorController::mostrarCalculadora, Router.engine);
//                Spark.get("/reportes",trabajadorController::mostrarReportes, Router.engine);
//                Spark.get("/vinculacion",trabajadorController::mostrarVinculacion, Router.engine);
//                Spark.get("/trayectos",trabajadorController::mostrarReportes, Router.engine);
            });

            Spark.path("/agente", () -> {
                Spark.before("", (request, response) -> {
                    if(!PermisoHelper.usuarioTienePermisos(request, Permiso.VER_AGENTESECTORIAL)){ //todo no funca?
                        response.redirect("/prohibido");
                        Spark.halt("Recurso prohibido");
                    }
                });
                Spark.before("/*", (request, response) -> {
                    if(!PermisoHelper.usuarioTienePermisos(request, Permiso.VER_AGENTESECTORIAL)){
                        response.redirect("/prohibido");
                        Spark.halt("Recurso prohibido");
                    }
                });
                Spark.get("",agenteController::mostrar,Router.engine);
            });

            Spark.path("/administrador", () -> {
                Spark.before("", (request, response) -> {
                    if(!PermisoHelper.usuarioTieneRol(request, Rol.ADMINISTRADOR)){
                        response.redirect("/prohibido");
                        Spark.halt("Recurso prohibido");
                    }
                });
                Spark.before("/*", (request, response) -> {
                    if(!PermisoHelper.usuarioTieneRol(request, Rol.ADMINISTRADOR)){
                        response.redirect("/prohibido");
                        Spark.halt("Recurso prohibido");
                    }
                });
                Spark.get("",administradorController::mostrar, Router.engine);
            });
        });
        Spark.get("/prohibido",errorController::prohibido,Router.engine);
        Spark.get("/error",errorController::error,Router.engine);
    }
}