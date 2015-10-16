package br.gov.mj.sislegis.app.parser.senado.xstream;

import br.gov.mj.sislegis.app.enumerated.Origem;
import br.gov.mj.sislegis.app.model.Proposicao;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Materia")
public class Materia {
	@XStreamAlias("IdentificacaoMateria")
	IdentificacaoMateria IdentificacaoMateria;
	@XStreamAlias("DadosBasicosMateria")
	DadosBasicosMateria DadosBasicosMateria;

	@XStreamAlias("AutoresPrincipais")
	AutoresPrincipais AutoresPrincipais;

	@XStreamAlias("SituacaoAtual")
	SituacaoAtual situacaoAtual;

	@Override
	public String toString() {

		return IdentificacaoMateria.toString();
	}

	public Proposicao toProposicao() {
		Proposicao p = new Proposicao();
		p.setAno(IdentificacaoMateria.AnoMateria);
		if (AutoresPrincipais != null && !AutoresPrincipais.autores.isEmpty()) {
			p.setAutor(AutoresPrincipais.autores.get(0).NomeAutor);
		}
		p.setOrigem(Origem.SENADO);
		if (!situacaoAtual.autuacoes.autuacoes.isEmpty()) {

			p.setComissao(situacaoAtual.autuacoes.autuacoes.get(0).Local.SiglaLocal);
		}

		p.setEmenta(DadosBasicosMateria.EmentaMateria);
		p.setIdProposicao(IdentificacaoMateria.CodigoMateria);
		p.setSigla(IdentificacaoMateria.SiglaSubtipoMateria);

		return p;

	}

	public static void configXstream(XStream xstream) {
		xstream.processAnnotations(Materia.class);
		xstream.processAnnotations(IdentificacaoMateria.class);
		xstream.processAnnotations(DadosBasicosMateria.class);
		xstream.processAnnotations(AutoresPrincipais.class);
		xstream.processAnnotations(AutorPrincipal.class);
		SituacaoAtual.configXstream(xstream);

	}
}