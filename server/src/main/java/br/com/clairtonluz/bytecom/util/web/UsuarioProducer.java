package br.com.clairtonluz.bytecom.util.web;

import br.com.clairtonluz.bytecom.model.jpa.entity.administrador.UserAccount;
import br.com.clairtonluz.bytecom.util.qualifier.UsuarioLogado;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * @author Clairton Luz
 */
public class UsuarioProducer {

    @Produces
    @Named
    @UsuarioLogado
    public UserAccount getUserAccount() {
        Subject currentUser = SecurityUtils.getSubject();
        return (UserAccount) currentUser.getSession().getAttribute("currentUser");
    }

}