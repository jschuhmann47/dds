package models.controllers;

import db.EntityManagerHelper;
import models.entities.organizaciones.entidades.PosibleTipoDocumento;
import models.entities.organizaciones.entidades.Trabajador;
import models.entities.seguridad.ValidadorContrasenia;
import models.entities.seguridad.chequeos.Chequeo;
import models.entities.seguridad.cuentas.Permiso;
import models.entities.seguridad.cuentas.Rol;
import models.entities.seguridad.cuentas.TipoRecurso;
import models.entities.seguridad.cuentas.Usuario;
import models.helpers.HashingHelper;
import models.helpers.PersistenciaHelper;
import models.helpers.SessionHelper;
import models.repositories.RepositorioDeTipoDocumento;
import models.repositories.RepositorioDeUsuarios;
import models.repositories.factories.FactoryRepositorioDeTipoDocumento;
import models.repositories.factories.FactoryRepositorioDeUsuarios;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.*;

public class LoginController {

    RepositorioDeTipoDocumento repoTipoDoc = FactoryRepositorioDeTipoDocumento.get();
    RepositorioDeUsuarios repoUsuarios = FactoryRepositorioDeUsuarios.get();

    public ModelAndView inicio(Request request, Response response){
        Map<String, Object> parametros = new HashMap<>();
        return new ModelAndView(parametros,"login.hbs");
    }

    public Response login(Request request, Response response){
        try{
            RepositorioDeUsuarios usuarios = FactoryRepositorioDeUsuarios.get();

            String nombreDeUsuario = request.queryParams("nombreDeUsuario");
            String contrasenia = HashingHelper.hashear(request.queryParams("contrasenia"));


            if(usuarios.existe(nombreDeUsuario,contrasenia)){
                Usuario usuario = usuarios.buscar(nombreDeUsuario,contrasenia);

                request.session(true);
                request.session().attribute("id", usuario.getId());
                request.session().attribute("resource_type",usuario.getTipoRecurso().toString());
                request.session().attribute("resource_id",usuario.obtenerIdRecurso());
                request.session().attribute("permissions",usuario.getPermisos());

                response.redirect("/" + usuario.getTipoCuenta());
            }
            else{
                response.redirect("/login");

            }
        }
        catch (Exception e){
            response.redirect("/login");

        }
        finally {
            return response;
        }
    }

    public Response logout(Request request, Response response){
        request.session().invalidate();
        response.redirect("/login");
        return response;
    }

    public ModelAndView mostrarNuevoUsuario(Request request, Response response){
        HashMap<String,Object> parametros = new HashMap<>();
        parametros.put("tiposDocumento",this.repoTipoDoc.buscarTodos());
        return new ModelAndView(parametros,"nuevo-usuario.hbs");
    }

    public Response crearNuevoUsuario(Request request, Response response) throws IOException {
        if(SessionHelper.atributosNoSonNull(request,"contrasenia","contraseniaChequeo","nombreUsuario","tipoDocumentoId","nroDocumento","nombre","apellido")){
            String contrasenia = request.queryParams("contrasenia");
            String contraseniaVerificacion = request.queryParams("contraseniaChequeo");
            if(!Objects.equals(contrasenia, contraseniaVerificacion)){
                response.redirect("/errorNoCoincidenPasswords");
                return response;
            }
            if(this.repoUsuarios.existeUsuario(request.queryParams("nombreUsuario"))){
                response.redirect("/errorUsuarioExistente");
                return response;
            }
            ValidadorContrasenia.inicializarChequeos();
            ValidadorContrasenia.setearPeoresContrasenias("src/main/java/models/entities/" +
                    "seguridad/chequeos/peoresContrasenias.txt");
            if(ValidadorContrasenia.esContraseniaValida(contrasenia)){

                Trabajador nuevoTrabajador = new Trabajador
                        (request.queryParams("apellido").toString(),request.queryParams("nombre").toString(),
                                this.repoTipoDoc.buscar(new Integer(request.queryParams("tipoDocumentoId"))),new Integer(request.queryParams("nroDocumento")));
                PersistenciaHelper.persistir(nuevoTrabajador);

                Usuario nuevoUser = new Usuario(request.queryParams("nombreUsuario"),
                        contrasenia,
                        Rol.BASICO,
                        nuevoTrabajador.getId(),
                        TipoRecurso.TRABAJADOR,
                        Permiso.VER_TRABAJADOR);

                PersistenciaHelper.persistir(nuevoUser);
            } else{
                response.redirect("/errorPasswordNoSegura");
                return response;
            }
        } else{
            response.redirect("/errorNoArgumentos");
            return response;
        }
        response.redirect("/login");
        return response;
    }

}
