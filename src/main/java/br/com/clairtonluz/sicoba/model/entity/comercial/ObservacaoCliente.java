package br.com.clairtonluz.sicoba.model.entity.comercial;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.com.clairtonluz.sicoba.model.entity.extra.BaseEntity;

/**
 * @author felipe
 */

@Entity
@Table(name = "observacao_cliente")
public class ObservacaoCliente extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "observacao_cliente_id_seq")
	@SequenceGenerator(name = "observacao_cliente_id_seq", sequenceName = "observacao_cliente_id_seq")
	private Integer id;

	@JoinColumn(name = "cliente_id", referencedColumnName = "id")
	@OneToOne
	@NotNull(message = "cliente é obrigatório")
	private Cliente cliente;

	@NotNull(message = "Observação é obrigatória")
	private String observacao;

	@Temporal(TemporalType.TIMESTAMP)
	private Date dataInclusao;

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Override
	public Integer getId() {
		return id;
	}

}
