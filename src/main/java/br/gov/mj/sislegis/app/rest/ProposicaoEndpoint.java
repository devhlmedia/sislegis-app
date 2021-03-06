package br.gov.mj.sislegis.app.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.annotations.cache.Cache;

import br.gov.mj.sislegis.app.enumerated.Origem;
import br.gov.mj.sislegis.app.model.AreaDeMeritoRevisao;
import br.gov.mj.sislegis.app.model.Comissao;
import br.gov.mj.sislegis.app.model.EncaminhamentoProposicao;
import br.gov.mj.sislegis.app.model.PosicionamentoProposicao;
import br.gov.mj.sislegis.app.model.ProcessoSei;
import br.gov.mj.sislegis.app.model.Proposicao;
import br.gov.mj.sislegis.app.model.Reuniao;
import br.gov.mj.sislegis.app.model.TipoEncaminhamento;
import br.gov.mj.sislegis.app.model.Usuario;
import br.gov.mj.sislegis.app.model.Votacao;
import br.gov.mj.sislegis.app.model.documentos.Briefing;
import br.gov.mj.sislegis.app.model.documentos.Emenda;
import br.gov.mj.sislegis.app.model.documentos.NotaTecnica;
import br.gov.mj.sislegis.app.model.pautacomissao.PautaReuniaoComissao;
import br.gov.mj.sislegis.app.model.pautacomissao.ProposicaoPautaComissao;
import br.gov.mj.sislegis.app.parser.TipoProposicao;
import br.gov.mj.sislegis.app.rest.authentication.UsuarioAutenticadoBean;
import br.gov.mj.sislegis.app.service.AreaDeMeritoService;
import br.gov.mj.sislegis.app.service.AutoUpdateProposicaoService;
import br.gov.mj.sislegis.app.service.ComissaoService;
import br.gov.mj.sislegis.app.service.DocumentoService;
import br.gov.mj.sislegis.app.service.EncaminhamentoProposicaoService;
import br.gov.mj.sislegis.app.service.ProposicaoService;
import br.gov.mj.sislegis.app.service.ReuniaoService;
import br.gov.mj.sislegis.app.service.TipoEncaminhamentoService;
import br.gov.mj.sislegis.app.util.SislegisUtil;

/**
 * 
 */

@Path("/proposicaos")
public class ProposicaoEndpoint {

	@Inject
	private ProposicaoService proposicaoService;

	@Inject
	private AreaDeMeritoService areaMeritoRevisao;
	@Inject
	private DocumentoService docService;

	@Inject
	private EncaminhamentoProposicaoService encaminhamentoProposicaoService;
	@Inject
	private UsuarioAutenticadoBean controleUsuarioAutenticado;

	@GET
	@Path("/proposicoesPautaCamara")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<PautaReuniaoComissao> buscarProposicoesPautaCamara(@QueryParam("idComissao") Long idComissao, @QueryParam("data") Date data) throws Exception {

		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("idComissao", idComissao);
		parametros.put("data", data);

		Set<PautaReuniaoComissao> lista = proposicaoService.buscarProposicoesPautaCamaraWS(parametros);

		return lista;
	}

	@GET
	@Path("/proposicoesPautaSenado")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<PautaReuniaoComissao> buscarProposicoesPautaSenado(@QueryParam("siglaComissao") String siglaComissao, @QueryParam("data") Date data) throws Exception {

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("siglaComissao", siglaComissao);
		parametros.put("data", data);

		return proposicaoService.buscarProposicoesPautaSenadoWS(parametros);

	}

	@GET
	@Path("/detalharProposicaoCamaraWS")
	@Produces(MediaType.APPLICATION_JSON)
	public Proposicao detalharProposicaoCamaraWS(@QueryParam("id") Long id) throws Exception {
		return proposicaoService.detalharProposicaoCamaraWS(id);
	}

	@GET
	@Path("/detalharProposicaoSenadoWS")
	@Produces(MediaType.APPLICATION_JSON)
	public Proposicao detalharProposicaoSenadoWS(@QueryParam("id") Long id) throws Exception {
		return proposicaoService.detalharProposicaoSenadoWS(id);
	}

	@POST
	@Path("/salvarProposicoes")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response salvarProposicoes(List<Proposicao> listaProposicoesSelecionados) {
		// nao é usada mais
		return Response.status(Status.SERVICE_UNAVAILABLE).build();
	}

	@Inject
	private ReuniaoService serviceReuniao;

	@POST
	@Path("/salvarProposicoesDePauta")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response adicionaProposicoesDePautaEmReuniao(AddProposicaoPautaWrapper proposicaoPautaWrapper) {
		try {
			Date reuniaoDate = new Date(proposicaoPautaWrapper.getReuniaoDate());
			Reuniao reuniao = null;
			try {
				reuniao = serviceReuniao.buscaReuniaoPorData(reuniaoDate);
			} catch (Exception e) {
				System.err.println("E " + e.getMessage());

			}
			if (reuniao == null) {
				reuniao = new Reuniao();
				reuniao.setData(reuniaoDate);
			}
			proposicaoService.adicionaProposicoesReuniao(proposicaoPautaWrapper.getPautaReunioes(), reuniao);

			return Response.noContent().build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.SERVICE_UNAVAILABLE).build();
		}

	}

	@POST
	@Path("/salvarProposicoesGenericas")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response salvarProposicoesGenericas(ProposicaoPautadaPautaWrapper propPautadas) {
		try {
			List<Proposicao> responses = new ArrayList<>();
			Iterator<PautaReuniaoComissao> prs = propPautadas.getPautas().iterator();
			for (Iterator iterator = propPautadas.getProposicoes().iterator(); iterator.hasNext();) {
				Proposicao proposicao = (Proposicao) iterator.next();
				PautaReuniaoComissao prc = prs.next();
				responses.add(proposicaoService.persistProposicaoAndPauta(proposicao, prc));

			}
			return Response.ok(responses, MediaType.APPLICATION_JSON).build();
		} catch (EJBTransactionRolledbackException e) {
			return Response.status(Response.Status.CONFLICT).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@POST
	@Path("/salvarProposicoesExtras")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response salvarProposicoesExtras(List<Proposicao> proposicoes) {
		try {
			Map<Integer, Proposicao> responses = new HashMap<>();
			for (Iterator iterator = proposicoes.iterator(); iterator.hasNext();) {
				Proposicao proposicao = (Proposicao) iterator.next();

				int result = proposicaoService.salvarProposicaoIndependente(proposicao);
				responses.put(result, proposicao);
			}
			return Response.ok(responses, MediaType.APPLICATION_JSON).build();
		} catch (EJBTransactionRolledbackException e) {
			return Response.status(Response.Status.CONFLICT).build();
		}

	}

	@POST
	@Path("/salvarProposicaoExtra")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response salvarProposicaoExtra(Proposicao proposicao) {
		try {
			int result = proposicaoService.salvarProposicaoIndependente(proposicao);
			switch (result) {
			case 0:
				return Response.status(Response.Status.OK).build();
			case 1:
				return Response.status(Response.Status.CREATED).build();
			case -1:
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

		} catch (EJBTransactionRolledbackException e) {
			return Response.status(Response.Status.CONFLICT).build();
		}
		return Response.noContent().build();

	}

	@Inject
	private TipoEncaminhamentoService tipoSvc;

	@POST
	@Path("/{id:[0-9][0-9]+}/desmarcaAtencaoEspecial")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response desmarcarAtencaoEspecial(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization) throws IOException {
		Proposicao p = proposicaoService.findById(id);
		if (p.getComAtencaoEspecial() != null) {
			p.desmarcarAtencaoEspecial();
			proposicaoService.save(p);
			Set<EncaminhamentoProposicao> encs = new HashSet<EncaminhamentoProposicao>(encaminhamentoProposicaoService.findByProposicao(id));
			if (encs.size() > 0) {
				TipoEncaminhamento marcadoComAtencao = tipoSvc.buscarTipoEncaminhamentoDespachoMinisterial();
				for (Iterator iterator = encs.iterator(); iterator.hasNext();) {
					EncaminhamentoProposicao encaminhamentoProposicao = (EncaminhamentoProposicao) iterator.next();
					if (marcadoComAtencao.equals(encaminhamentoProposicao.getTipoEncaminhamento())) {
						if (!encaminhamentoProposicao.isFinalizado()) {
							Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
							encaminhamentoProposicaoService.finalizar(encaminhamentoProposicao.getId(), "Proposição foi removida do status de atenção especial", user);
						}
					}
				}
			}

			return Response.ok().build();

		} else {
			return Response.notModified().build();
		}

	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Proposicao entity, @HeaderParam("Authorization") String authorization) {

		try {
			Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
			proposicaoService.save(entity, user);
			return Response.ok(entity).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(Proposicao entity, @HeaderParam("Authorization") String authorization) {
		try {
			Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
			proposicaoService.save(entity, user);
			return Response.created(UriBuilder.fromResource(ProposicaoEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id) {
		proposicaoService.deleteById(id);
		return Response.noContent().build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/{reuniaoId:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id, @PathParam("reuniaoId") Long reuniaoId) {
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findById(@PathParam("id") Integer id, @QueryParam("fetchAll") Boolean fetchAll) {
		return Response.ok(proposicaoService.buscarPorId(id, (fetchAll != null && fetchAll))).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Proposicao> listAll() {
		return proposicaoService.listarTodos();
	}

	@GET
	@Path("/autores")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Autor> listAutores(@QueryParam("nome") String nome) {
		List<Autor> au = new ArrayList<ProposicaoEndpoint.Autor>();
		if (nome != null && !nome.trim().isEmpty()) {
			List<String> l = proposicaoService.listarTodosAutores(nome);

			for (Iterator iterator = l.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				Autor autor = new Autor();
				autor.nome = string;

				autor.id = au.size();
				au.add(autor);
			}
		}
		return au;

	}

	@GET
	@Path("/relatores")
	@Produces(MediaType.APPLICATION_JSON)
	// da pra usar o obj autor
	public List<Autor> searchRelator(@QueryParam("nome") String nome) {
		List<Autor> au = new ArrayList<ProposicaoEndpoint.Autor>();
		if (nome != null && !nome.trim().isEmpty()) {
			List<String> l = proposicaoService.procurarRelatores(nome);

			for (Iterator iterator = l.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				Autor autor = new Autor();
				autor.nome = string;

				autor.id = au.size();
				au.add(autor);
			}
		}
		return au;

	}

	class Autor {
		long id;
		String nome;

		public long getId() {
			return id;
		}

		public String getNome() {
			return nome;
		}
	}

	@GET
	@Path("/consultar")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Proposicao> consultar(@QueryParam("relator") String relator, @QueryParam("comissao") String comissao, @QueryParam("ementa") String ementa, @QueryParam("autor") String autor, @QueryParam("sigla") String sigla, @QueryParam("origem") String origem, @QueryParam("estado") String estado, @QueryParam("isFavorita") String isFavorita, @QueryParam("idResponsavel") Long idResponsavel, @QueryParam("idPosicionamento") Long idPosicionamento, @QueryParam("idEquipe") Long idEquipe,
			@QueryParam("limit") Integer limit, @QueryParam("inseridaApos") String inseridaApos, @QueryParam("foiDespachadaApos") String foiDespachadaApos, @QueryParam("foiDespachadaAte") String foiDespachadaAte, @QueryParam("macrotema") String macrotema, @QueryParam("comAtencaoEspecial") Boolean comAtencaoEspecial, @QueryParam("somentePautadas") Boolean pautadas, @QueryParam("comNotaTecnica") Boolean comNotaTecnica, @QueryParam("offset") Integer offset) {

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("sigla", sigla);
		m.put("ementa", ementa);
		m.put("relator", relator);
		m.put("autor", autor);
		m.put("origem", origem);
		m.put("isFavorita", isFavorita);
		m.put("estado", estado);
		m.put("macrotema", macrotema);
		m.put("idEquipe", idEquipe);
		m.put("idPosicionamento", idPosicionamento);
		m.put("idResponsavel", idResponsavel);
		m.put("somentePautadas", pautadas);
		m.put("comNotaTecnica", comNotaTecnica);

		m.put("comAtencaoEspecial", comAtencaoEspecial);

		m.put("comissao", comissao);
		m.put("inseridaApos", inseridaApos);
		m.put("foiDespachadaApos", foiDespachadaApos);
		m.put("foiDespachadaAte", foiDespachadaAte);

		List<Proposicao> results = proposicaoService.consultar(m, offset, limit);
		return results;
	}

	@GET
	@Path("/buscarPorSufixo")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Proposicao> buscarPorSufixo(@QueryParam("sufixo") String sufixo) {
		return proposicaoService.buscarPorSufixo(sufixo);
	}

	@GET
	@Path("/buscaIndependente/{origem:[A-Z]*}/{tipo:[A-Z\\.]*}/{ano:[0-9]{4}}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Proposicao> buscaIndependente(@PathParam("origem") String origem, @PathParam("tipo") String tipo, @QueryParam("numero") String numero, @PathParam("ano") Integer ano) throws Exception {

		return proposicaoService.buscaProposicaoIndependentePor(Origem.valueOf(origem), tipo, numero, ano);

	}

	@GET
	@Path("/listTipos/CAMARA")
	@Cache(maxAge = 24, noStore = false, isPrivate = false, sMaxAge = 24)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<TipoProposicao> listTiposCamara() throws Exception {

		return proposicaoService.listTipos(Origem.CAMARA);

	}

	@GET
	@Path("/listTipos/SENADO")
	@Cache(maxAge = 24, noStore = false, isPrivate = false, sMaxAge = 24)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<TipoProposicao> listTiposSenado() throws Exception {

		return proposicaoService.listTipos(Origem.SENADO);

	}

	
	@POST
	@Path("/follow/{id:[0-9]+}")
	public Response follow(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization) {
		try {
			Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
			proposicaoService.followProposicao(user, id);

			return Response.noContent().build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

	}

	@POST
	@Path("/check4updates/{id:[0-9]+}")
	public Response syncpauta(@PathParam("id") Long id) {
		try {

			if (proposicaoService.syncDadosPautaProposicao(id) || proposicaoService.syncDadosProposicao(id)) {
				return Response.status(Status.ACCEPTED).entity(proposicaoService.findById(id)).build();
			} else {
				return Response.status(Status.NOT_MODIFIED).build();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

	}

	@DELETE
	@Path("/follow/{id:[0-9]+}")
	public Response unfollow(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization) {
		try {
			Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
			proposicaoService.unfollowProposicao(user, id);
			return Response.noContent().build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

	}

	@POST
	@Path("/alterarPosicionamento")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response alterarPosicionamento(PosicionamentoProposicaoWrapper posicionamentoProposicaoWrapper, @HeaderParam("Authorization") String authorization) {
		try {
			Usuario usuarioLogado = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
			PosicionamentoProposicao pp = proposicaoService.alterarPosicionamento(posicionamentoProposicaoWrapper.getId(), posicionamentoProposicaoWrapper.getIdPosicionamento(), posicionamentoProposicaoWrapper.preliminar, usuarioLogado);

			return Response.ok(pp).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/historicoPosicionamentos/{id:[0-9]+}")
	public List<PosicionamentoProposicao> historicoPosicionamentos(@PathParam("id") Long id) {
		return proposicaoService.listarHistoricoPosicionamentos(id);
	}

	@DELETE
	@Path("/{id:[0-9]+}/revisaoMerito/{idRevisao:[0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response apagaRevisao(@PathParam("id") Long id, @PathParam("idRevisao") Long idRevisao) throws Exception {
		// AreaDeMeritoRevisao rev = areaMeritoRevisao.findRevisao(idRevisao);

		areaMeritoRevisao.deleteRevisao(idRevisao);

		return Response.ok().build();
	}

	@DELETE
	@Path("/{id:[0-9]+}/revisaoMerito/{idRevisao:[0-9]+}/anexo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAnexoRevisao(@PathParam("id") Long id, @PathParam("idRevisao") Long idRevisao) throws Exception {
		AreaDeMeritoRevisao rev = areaMeritoRevisao.findRevisao(idRevisao);
		if (rev.getDocumento() != null) {
			Long idDoc = rev.getDocumento().getId();
			rev.setDocumento(null);
			areaMeritoRevisao.saveRevisao(rev);
			docService.deleteById(idDoc);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/{id:[0-9]+}/revisaoMerito")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AreaDeMeritoRevisao> listRevisoes(@PathParam("id") Long id) throws Exception {
		return areaMeritoRevisao.listRevisoesProposicao(id);
	}

	@POST
	@Path("/{id:[0-9]+}/revisaoMerito")
	@Produces(MediaType.APPLICATION_JSON)
	public AreaDeMeritoRevisao saveRevisao(@PathParam("id") Long id, AreaDeMeritoRevisao entity) throws Exception {
		entity.setProposicao(proposicaoService.findById(id));

		return areaMeritoRevisao.saveRevisao(entity);
	}

	@GET
	@Path("/{id:[0-9]+}/notatecnica")
	@Produces(MediaType.APPLICATION_JSON)
	public List<NotaTecnica> listNotaTecnicas(@PathParam("id") Long id) throws Exception {
		return proposicaoService.getNotaTecnicas(id);
	}

	@DELETE
	@Path("/{id:[0-9]+}/docrelated/{type:[0-9]+}/{docId:[0-9]+}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeDocRelated(@PathParam("id") Long id, @PathParam("docId") Long docId, @PathParam("type") Integer type, @HeaderParam("Authorization") String authorization) {
		try {
			Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);

			switch (type) {
			case 1:
				proposicaoService.deleteDocRelated(docId, NotaTecnica.class);
				break;
			case 2:
				proposicaoService.deleteDocRelated(docId, Briefing.class);
				break;
			case 3:
				proposicaoService.deleteDocRelated(docId, Emenda.class);
				break;
			default:
				throw new IllegalArgumentException("tipo invalido");
			}
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/{id:[0-9]+}/docrelated/{type:[0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public List listDocRelated(@PathParam("id") Long id, @PathParam("type") Integer type) throws Exception {
		switch (type) {
		case 1:
			return proposicaoService.getNotaTecnicas(id);
		case 2:
			return proposicaoService.getBriefings(id);
		case 3:
			return proposicaoService.getEmendas(id);

		default:
			break;
		}
		return proposicaoService.getNotaTecnicas(id);
	}

	@POST
	@Path("/{id:[0-9]+}/notatecnica")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createNotaTecnica(@PathParam("id") Long id, NotaTecnica nt, @HeaderParam("Authorization") String authorization) {
		try {
			Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
			nt.setUsuario(user);
			nt.setProposicao(proposicaoService.findById(id));

			proposicaoService.saveNotaTecnica(nt);
			return Response.ok(nt).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@DELETE
	@Path("/{id:[0-9]+}/notatecnica/{idNota:[0-9]+}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeNotaTecnica(@PathParam("id") Long id, @PathParam("idNota") Long idNota, @HeaderParam("Authorization") String authorization) {
		try {
			Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
			proposicaoService.deleteDocRelated(idNota, NotaTecnica.class);
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/{id:[0-9]+}/pautas")
	@Cache(maxAge = 24, noStore = false, isPrivate = false, sMaxAge = 24)
	@Produces(MediaType.APPLICATION_JSON)
	public Set<ProposicaoPautaComissao> listPautasProposicao(@PathParam("id") Long id) throws Exception {
		return proposicaoService.findById(id).getPautasComissoes();
	}

	@POST
	@Path("/setRoadmapComissoes")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setRoadmapComissoes(RoadmapComissoesWrapper roadmapComissoesWrapper) {
		try {
			proposicaoService.setRoadmapComissoes(roadmapComissoesWrapper.getIdProposicao(), roadmapComissoesWrapper.getComissoes());
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path("/vincularProcessoSei")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ProcessoSei inserirProcessoSei(ProcessoSeiWrapper processoSeiWrapper) {
		try {
			ProcessoSei processoSei = proposicaoService.vincularProcessoSei(processoSeiWrapper.getId(), processoSeiWrapper.getProtocolo());
			return processoSei;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@DELETE
	@Path("/excluirProcessoSei/{idProcesso:[0-9]+}")
	public Response excluirProcessoSei(@PathParam("idProcesso") Long idProcesso) {
		try {
			proposicaoService.excluirProcessoSei(idProcesso);
			return Response.noContent().build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("/listarVotacoes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Votacao> listarVotacoes(@QueryParam("idProposicao") String idProposicao, @QueryParam("tipo") String tipo, @QueryParam("numero") String numero, @QueryParam("ano") String ano, @QueryParam("origem") String origem) {

		try {
			Integer idProp = (idProposicao == null || "".equals(idProposicao)) ? null : Integer.valueOf(idProposicao);
			List<Votacao> votacoes = proposicaoService.listarVotacoes(idProp, tipo, numero, ano, Origem.valueOf(origem));
			return votacoes;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Inject
	AutoUpdateProposicaoService auto;

	@Inject
	ComissaoService comissaoService;

	@GET
	@Path("/auto")
	public void autoupdates(@QueryParam("s") String s, @QueryParam("o") String origem, @QueryParam("c") String comissaoParam, @HeaderParam("Authorization") String authorization) {
		if (s != null) {

			List<Comissao> ls;
			try {
				Usuario user = controleUsuarioAutenticado.carregaUsuarioAutenticado(authorization);
				Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).warning(user.getEmail() + " executando auto updates");
				Origem o = Origem.CAMARA;
				if ("s".equals(origem)) {
					o = Origem.SENADO;
					ls = comissaoService.listarComissoesSenado();
				} else {
					ls = comissaoService.listarComissoesCamara();
				}

				if ("TRAMITACAO".equals(comissaoParam)) {
					Logger.getLogger(SislegisUtil.SISLEGIS_LOGGER).fine("Atualizando tramitacoes");
					Map<String, Object> filtros = new HashMap<String, Object>();
					filtros.put("origem", o.name());
					// filtros.put("somentePautadas", true);
					List<Proposicao> props = proposicaoService.consultar(filtros, 0, null);
					for (Iterator iterator = props.iterator(); iterator.hasNext();) {
						Proposicao proposicao = (Proposicao) iterator.next();
						proposicaoService.syncDadosProposicao(proposicao.getId());
					}

					return;
				}

				SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");

				Calendar dataInicial = Calendar.getInstance();
				dataInicial.setTimeInMillis(sdf.parse(s).getTime());
				Calendar dataFinal = (Calendar) dataInicial.clone();
				dataFinal.add(Calendar.WEEK_OF_YEAR, 1);
				for (Iterator<Comissao> iterator = ls.iterator(); iterator.hasNext();) {
					Comissao comissao = (Comissao) iterator.next();
					if (comissaoParam != null) {
						if (comissaoParam.equals(comissao.getSigla().trim())) {
							proposicaoService.syncPautaAtualComissao(o, comissao, dataInicial, dataFinal);
						}
					} else {
						proposicaoService.syncPautaAtualComissao(o, comissao, dataInicial, dataFinal);
					}

				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}

class ProposicaoPautadaPautaWrapper {
	List<Proposicao> proposicoes;
	List<PautaReuniaoComissao> pautas;

	public List<Proposicao> getProposicoes() {
		return proposicoes;
	}

	public void setProposicoes(List<Proposicao> proposicoes) {
		this.proposicoes = proposicoes;
	}

	public List<PautaReuniaoComissao> getPautas() {
		return pautas;
	}

	public void setPautas(List<PautaReuniaoComissao> pautas) {
		this.pautas = pautas;
	}

}

class AddProposicaoPautaWrapper {
	Set<PautaReuniaoComissao> pautaReunioes;
	long reuniaoDate;

	public Set<PautaReuniaoComissao> getPautaReunioes() {
		return pautaReunioes;
	}

	public void setPautaReunioes(Set<PautaReuniaoComissao> pautaReunioes) {
		this.pautaReunioes = pautaReunioes;
	}

	public long getReuniaoDate() {
		return reuniaoDate;
	}

	public void setReuniaoDate(long reuniaoDate) {
		this.reuniaoDate = reuniaoDate;
	}

}

class PosicionamentoProposicaoWrapper {
	Long id;
	Long idPosicionamento;
	boolean preliminar;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdPosicionamento() {
		return idPosicionamento;
	}

	public void setIdPosicionamento(Long idPosicionamento) {
		this.idPosicionamento = idPosicionamento;
	}

	public boolean isPreliminar() {
		return preliminar;
	}

	public void setPreliminar(boolean preliminar) {
		this.preliminar = preliminar;
	}
}

class RoadmapComissoesWrapper {
	private Long idProposicao;
	private List<String> comissoes;

	public Long getIdProposicao() {
		return idProposicao;
	}

	public void setIdProposicao(Long idProposicao) {
		this.idProposicao = idProposicao;
	}

	public List<String> getComissoes() {
		return comissoes;
	}

	public void setComissoes(List<String> comissoes) {
		this.comissoes = comissoes;
	}
}

class ProcessoSeiWrapper {
	private Long id;
	private String protocolo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProtocolo() {
		return protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}
}
