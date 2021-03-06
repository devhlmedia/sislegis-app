package br.gov.mj.sislegis.app.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import br.gov.mj.sislegis.app.model.AreaDeMerito;
import br.gov.mj.sislegis.app.model.AreaDeMeritoRevisao;
import br.gov.mj.sislegis.app.service.AreaDeMeritoService;

@Path("/areamerito")
public class AreaMeritoEndpoint {
	@Inject
	private AreaDeMeritoService service;

	@POST
	@Path("/{id:[0-9]*}")	
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(AreaDeMerito entity) {
		service.saveAreaDeMerito(entity);
		return Response.created(
				UriBuilder.fromResource(AreaMeritoEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
	}
	@POST	
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(AreaDeMerito entity) {
		service.saveAreaDeMerito(entity);
		return Response.created(
				UriBuilder.fromResource(AreaMeritoEndpoint.class).path(String.valueOf(entity.getId())).build()).header("Access-Control-Expose-Headers", "Location").build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id) {
		service.deleteById(id);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findById(@PathParam("id") Long id) {
		return Response.ok(service.findById(id)).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<AreaDeMerito> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult) {
		return service.listAll();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/revisao")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AreaDeMeritoRevisao> listAllRevisao(@PathParam("id") Long id,
			@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult) {
		return service.listRevisoes(id, false);
	}

}