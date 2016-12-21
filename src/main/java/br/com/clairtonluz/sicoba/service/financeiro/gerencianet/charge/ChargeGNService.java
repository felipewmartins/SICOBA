package br.com.clairtonluz.sicoba.service.financeiro.gerencianet.charge;

import br.com.clairtonluz.sicoba.exception.ConflitException;
import br.com.clairtonluz.sicoba.model.entity.comercial.Contrato;
import br.com.clairtonluz.sicoba.model.entity.financeiro.gerencianet.Credentials;
import br.com.clairtonluz.sicoba.model.entity.financeiro.gerencianet.charge.Charge;
import br.com.clairtonluz.sicoba.service.financeiro.gerencianet.GNService;
import br.com.clairtonluz.sicoba.util.DateUtil;
import br.com.clairtonluz.sicoba.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by clairton on 09/11/16.
 */
@Service
class ChargeGNService {

    private static final String CREATE_CHARGE = "createCharge";
    private static final String DETAIL_CHARGE = "detailCharge";
    private static final String UPDATE_CHARGE_METADATA = "updateChargeMetadata";
    private static final String UPDATE_BILLET = "updateBillet";
    private static final String PAY_CHARGE = "payCharge";
    private static final String CANCEL_CHARGE = "cancelCharge";
    private static final String RESEND_BILLET = "resendBillet";


    JSONObject createCharge(Contrato contrato, Charge charge) {
        String item = String.format("Internet Banda Larga %s", contrato.getPlano().getNome());
        JSONArray items = new JSONArray().put(GNService.createItem(item, charge.getValue()));

        JSONObject body = new JSONObject();
        body.put("items", items);
        body.put("metadata", GNService.createMetadata(Credentials.getInstance().getNotificationUrl(), charge.getId()));

        return GNService.call(CREATE_CHARGE, body);
    }

    JSONObject setPaymentToBankingBilletGN(Charge charge) {
        JSONObject customer = GNService.createConsumer(charge.getCliente(), false);
        JSONArray instructions = GNService.createInstructions(charge.getDiscount());

        JSONObject bankingBillet = new JSONObject();
        bankingBillet.put("expire_at", DateUtil.formatISO(charge.getExpireAt()));
        bankingBillet.put("customer", customer);

        JSONObject payment = new JSONObject();
        payment.put("banking_billet", bankingBillet);

        JSONObject body = new JSONObject();
        body.put("payment", payment);
        body.put("instructions", instructions); // opcional

        return GNService.call(PAY_CHARGE, body);
    }

    JSONObject cancelCharge(Charge charge) {
        Map<String, String> params = new HashMap<>();
        params.put("id", charge.getChargeId().toString());

        return GNService.call(CANCEL_CHARGE, params);
    }

    JSONObject updateBilletExpireAt(Charge charge) {
        Map<String, String> params = new HashMap<>();
        params.put("id", charge.getChargeId().toString());

        JSONObject body = new JSONObject();
        body.put("expire_at", DateUtil.formatISO(charge.getExpireAt()));

        return GNService.call(UPDATE_BILLET, params, body);
    }

    JSONObject updateChargeMetadata(Charge charge) {
        Map<String, String> params = new HashMap<>();
        params.put("id", charge.getChargeId().toString());

        JSONObject body = GNService.createMetadata(Credentials.getInstance().getNotificationUrl(), charge.getId());

        return GNService.call(UPDATE_CHARGE_METADATA, params, body);
    }

    JSONObject detailCharge(Charge charge) {
        Map<String, String> params = new HashMap<>();
        params.put("id", charge.getChargeId().toString());
        return GNService.call(DETAIL_CHARGE, params);
    }

    JSONObject resendBillet(Charge charge) {
        if (StringUtil.isEmpty(charge.getCliente().getEmail())) {
            throw new ConflitException("Cliente não possui email");
        }

        Map<String, String> params = new HashMap<>();
        params.put("id", charge.getChargeId().toString());

        JSONObject body = new JSONObject();
        body.put("email", charge.getCliente().getEmail());

        return GNService.call(RESEND_BILLET, params, body);
    }

    JSONObject linkCharge(Charge charge) {
        Map<String, String> params = new HashMap<>();
        params.put("id", charge.getChargeId().toString());

        JSONObject body = new JSONObject();
        if (charge.getDiscount() != null && charge.getDiscount() > 0) {
            body.put("billet_discount", charge.getDiscount() * 100);
        }

        if (StringUtil.isEmpty(charge.getMessage())) {
            body.put("message", charge.getMessage());
        }

        body.put("expire_at", DateUtil.formatISO(charge.getExpireAt()));
        body.put("request_delivery_address", false);
        body.put("payment_method", "all");

        return GNService.call(RESEND_BILLET, params, body);
    }
}
