package net.servehttp.bytecom.comercial.jpa.entity;

import java.io.Serializable;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.servehttp.bytecom.extra.jpa.entity.EntityGeneric;

import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "pais")
public class Pais extends EntityGeneric implements Serializable {

  private static final long serialVersionUID = -8042147881454631916L;

  private String nome;

  @XmlTransient
  @OneToMany(mappedBy = "pais", fetch = FetchType.EAGER)
  private List<Estado> estados;

  public Pais() {}

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getNome() {
    return this.nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public List<Estado> getEstados() {
    return this.estados;
  }

  public void setEstados(List<Estado> estados) {
    this.estados = estados;
  }

  public Estado addEstado(Estado estado) {
    getEstados().add(estado);
    estado.setPais(this);

    return estado;
  }

  public Estado removeEstado(Estado estado) {
    getEstados().remove(estado);
    estado.setPais(null);

    return estado;
  }

}