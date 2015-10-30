package br.gov.mj.sislegis.app.rest;

import br.gov.mj.sislegis.app.model.Resultado;
import br.gov.mj.sislegis.app.model.Usuario;
import br.gov.mj.sislegis.app.rest.authentication.UsuarioAutenticadoBean;
import br.gov.mj.sislegis.app.service.ResultadoService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.List;

@Path("/resultados")
public class ResultadoEndpoint {

    @Inject
    private ResultadoService resultadoService;

    @Inject
    private UsuarioAutenticadoBean controleUsuarioAutenticado;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(Resultado resultado, @HeaderParam("Authorization") String authorization) {
        try {
            Usuario usuario = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
            resultado.setUsuario(usuario);
            resultadoService.save(resultado);

        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.created(UriBuilder.fromResource(ResultadoEndpoint.class).path(String.valueOf(resultado.getId())).build()).build();
    }

    @GET
    @Path("/proposicao/{idProposicao:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Resultado> findByIdProposicao(@PathParam("idProposicao") Long idProposicao) {
        List<Resultado> resultados = resultadoService.findByIdProposicao(idProposicao);
        return resultados;
    }

}