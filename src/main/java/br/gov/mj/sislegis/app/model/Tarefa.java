package br.gov.mj.sislegis.app.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import br.gov.mj.sislegis.app.enumerated.TipoTarefa;

@Entity
@Table(name = "tarefa")
@NamedQueries({ 
	@NamedQuery(name = "getAllTarefa4Usuario", query = "select c from Tarefa c where c.usuario.id=:userId"),
	@NamedQuery(name = "getTarefa4Comentario", query = "select c from Tarefa c where c.comentarioFinalizacao.id=:comentarioId"),
	@NamedQuery(name = "getAllTarefa", query = "select c from Tarefa c where c.usuario=:user and c.encaminhamentoProposicao=:enc and c.tipoTarefa=:tipo")

	

})
@XmlRootElement
public class Tarefa extends AbstractEntity {
	public static Tarefa createTarefaEncaminhamento(Usuario usuario, EncaminhamentoProposicao encaminhamento) {
		Tarefa tarefa = new Tarefa(TipoTarefa.ENCAMINHAMENTO, usuario);
		tarefa.encaminhamentoProposicao = encaminhamento;
		return tarefa;
	}

	private static final long serialVersionUID = -806063711060116952L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "data")
	@Temporal(TemporalType.TIMESTAMP)
	private Date data;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private TipoTarefa tipoTarefa;

	@Column
	private boolean isFinalizada;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	private EncaminhamentoProposicao encaminhamentoProposicao;

	@ManyToOne(fetch = FetchType.EAGER)
	private Usuario usuario;

	@Transient
	private Proposicao proposicao;

	Tarefa() {
	}

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Comentario comentarioFinalizacao;

	Tarefa(TipoTarefa tipo, Usuario user) {
		this.tipoTarefa = tipo;
		this.usuario = user;
		this.data = new Date();
	}

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public TipoTarefa getTipoTarefa() {
		return tipoTarefa;
	}

	public void setTipoTarefa(TipoTarefa tipoTarefa) {
		this.tipoTarefa = tipoTarefa;
	}

	public boolean isFinalizada() {
		return isFinalizada;
	}

	public void setFinalizada(boolean isFinalizada) {
		this.isFinalizada = isFinalizada;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Proposicao getProposicao() {
		return proposicao;
	}

	public void setProposicao(Proposicao proposicao) {
		this.proposicao = proposicao;
	}

	public EncaminhamentoProposicao getEncaminhamentoProposicao() {
		if (!TipoTarefa.ENCAMINHAMENTO.equals(tipoTarefa)) {
			throw new IllegalArgumentException("Esta tarefa nao foi criada a partir de um encaminhamento");
		}
		return encaminhamentoProposicao;
	}

	public void setEncaminhamentoProposicao(EncaminhamentoProposicao ent) {

		encaminhamentoProposicao = ent;
	}

	public Comentario getComentarioFinalizacao() {
		return comentarioFinalizacao;
	}

	public void setComentarioFinalizacao(Comentario comentarioFinalizacao) {
		this.comentarioFinalizacao = comentarioFinalizacao;
	}
}