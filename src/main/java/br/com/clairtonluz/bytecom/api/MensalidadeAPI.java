package br.com.clairtonluz.bytecom.api;

import br.com.clairtonluz.bytecom.model.jpa.entity.financeiro.Mensalidade;
import br.com.clairtonluz.bytecom.model.service.financeiro.MensalidadeService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by clairtonluz on 19/12/15.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("mensalidades")
public class MensalidadeAPI {

    @Inject
    private MensalidadeService mensalidadeService;

    @GET
    @Path("atrasada")
    public List<Mensalidade> getAtrasadas() {
        return mensalidadeService.buscarMensalidadesAtrasada();
    }

}