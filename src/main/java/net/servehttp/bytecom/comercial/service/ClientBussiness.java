package net.servehttp.bytecom.comercial.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import net.servehttp.bytecom.comercial.jpa.ClienteJPA;
import net.servehttp.bytecom.comercial.jpa.entity.Cliente;
import net.servehttp.bytecom.comercial.jpa.entity.StatusCliente;

public class ClientBussiness implements Serializable {

  private static final long serialVersionUID = -8296012997453708684L;

  @Inject
  private ClienteJPA clienteJPA;

  public List<Cliente> buscaUltimosClientesAlterados() {
    return clienteJPA.buscaUltimosClientesAlterados();
  }

  public Cliente buscarPorId(int id) {
    return clienteJPA.buscarPorId(Cliente.class, id);
  }

  public List<Cliente> buscarTodosClientePorNomeIp(String nome, String ip, StatusCliente status) {
    return clienteJPA.buscarTodosClientePorNomeIp(nome, ip, status);
  }

  public boolean rgAvaliable(Cliente c) {
    List<Cliente> clientes = clienteJPA.buscarTodos("rg", c.getRg(), Cliente.class);
    return clientes.isEmpty() || clientes.get(0).getId() == c.getId();
  }

  public boolean cpfCnpjAvaliable(Cliente c) {
    List<Cliente> clientes = clienteJPA.buscarTodos("cpfCnpj", c.getCpfCnpj(), Cliente.class);
    return clientes.isEmpty() || clientes.get(0).getId() == c.getId();
  }

  public boolean emailAvaliable(Cliente c) {
    List<Cliente> clientes = clienteJPA.buscarTodos("email", c.getEmail(), Cliente.class);
    return clientes.isEmpty() || clientes.get(0).getId() == c.getId();
  }

  public <T> T salvar(T t) {
    return clienteJPA.salvar(t);
  }

  public <T> T atualizar(T t) {
    return clienteJPA.atualizar(t);
  }

  public void remover(Cliente cliente) {
    clienteJPA.remover(cliente);
  }

}