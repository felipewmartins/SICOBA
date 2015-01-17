package net.servehttp.bytecom.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.servehttp.bytecom.persistence.entity.cadastro.Cliente;
import net.servehttp.bytecom.persistence.entity.cadastro.Mensalidade;

import org.jrimum.bopepo.BancosSuportados;
import org.jrimum.bopepo.Boleto;
import org.jrimum.bopepo.view.BoletoViewer;
import org.jrimum.domkee.comum.pessoa.endereco.CEP;
import org.jrimum.domkee.comum.pessoa.endereco.Endereco;
import org.jrimum.domkee.comum.pessoa.endereco.UnidadeFederativa;
import org.jrimum.domkee.financeiro.banco.ParametrosBancariosMap;
import org.jrimum.domkee.financeiro.banco.febraban.Agencia;
import org.jrimum.domkee.financeiro.banco.febraban.Carteira;
import org.jrimum.domkee.financeiro.banco.febraban.Cedente;
import org.jrimum.domkee.financeiro.banco.febraban.ContaBancaria;
import org.jrimum.domkee.financeiro.banco.febraban.NumeroDaConta;
import org.jrimum.domkee.financeiro.banco.febraban.Sacado;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeCobranca;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeTitulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo.Aceite;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.servehttp.bytecom.commons.DateUtil;
import com.servehttp.bytecom.commons.StringUtil;

public class GerarBoleto implements Serializable {

  private static final double TAXA_JUROS_AO_DIA = 0.006677;
  private static final double TAXA_MULTA = 0.05;
  private static final int EMITENTE_BENEFICIARIO = 4;
  private static final long serialVersionUID = 2996334986455478857L;

  public static byte[] criarCarneCaixa(List<Mensalidade> mensalidades) {
    if (mensalidades != null && !mensalidades.isEmpty()) {
      List<Boleto> boletos = new ArrayList<>();

      mensalidades.forEach(m -> {
        Cedente cedente = getCedente();

        Sacado sacado = getSacado(m.getCliente());

        Titulo titulo = getTitulo(m, cedente, sacado);

        Boleto boleto = getBoleto(m, titulo);

        boletos.add(boleto);
      });

      File templatePersonalizado =
          new File(GerarBoleto.class.getResource("BoletoCarne3PorPagina.pdf").getFile());

      byte[] boletosPorPagina = groupInPages(boletos, "Carne3PorPagina.pdf", templatePersonalizado);

      return boletosPorPagina;
    }
    return null;

  }

  private static Boleto getBoleto(Mensalidade m, Titulo titulo) {
    /*
     * INFORMANDO OS DADOS SOBRE O BOLETO.
     */
    Boleto boleto = new Boleto(titulo);

    boleto.setLocalPagamento("PREFERENCIALMENTE NAS CASAS LOTÉRICAS ATÉ O VALOR LIMITE");
    boleto.setInstrucao1(String.format("MULTA DE R$: %f APÓS : %s",
        StringUtil.formatCurrence(m.getValor() * TAXA_MULTA),
        DateUtil.format(m.getDataVencimento())));
    boleto.setInstrucao2(String.format("JUROS DE R$: %f AO DIA",
        StringUtil.formatCurrence(m.getValor() * TAXA_JUROS_AO_DIA)));
    boleto.setInstrucao4("NÃO RECEBER APOS 30 DIAS DO VENCIMENTO");
    if (m.getParcela() != null && !m.getParcela().isEmpty()) {
      boleto.setInstrucao6("PARCELA " + m.getParcela());
    }
    String nossoNumeroPadraoCEF =
        String.format("%d%d/%s-%s", titulo.getContaBancaria().getCarteira().getCodigo(),
            EMITENTE_BENEFICIARIO, titulo.getNossoNumero(), titulo.getDigitoDoNossoNumero());
    boleto.addTextosExtras("txtFcNossoNumero", nossoNumeroPadraoCEF);
    boleto.addTextosExtras("txtRsNossoNumero", nossoNumeroPadraoCEF);
    return boleto;
  }

  private static Titulo getTitulo(Mensalidade m, Cedente cedente, Sacado sacado) {
    /*
     * INFORMANDO OS DADOS SOBRE O TÍTULO.
     */

    // Informando dados sobre a conta bancária do título.
    ContaBancaria contaBancaria =
        new ContaBancaria(BancosSuportados.CAIXA_ECONOMICA_FEDERAL.create());
    contaBancaria.setNumeroDaConta(new NumeroDaConta(484924, "8"));
    contaBancaria.setCarteira(new Carteira(2, TipoDeCobranca.SEM_REGISTRO));
    contaBancaria.setAgencia(new Agencia(1089, "1"));

    ParametrosBancariosMap map = new ParametrosBancariosMap();
    map.adicione("CodigoOperacao", 1);

    Titulo titulo = new Titulo(contaBancaria, sacado, cedente, map);

    titulo.setNumeroDoDocumento(String.format("%d/%d", m.getCliente().getId(), m.getId()));
    // titulo.setNossoNumero(String.format("%015d", m.getId()));
    // titulo.setDigitoDoNossoNumero(calcularDigitoDoNossoNumero(contaBancaria.getCarteira()

    // titulo.setNumeroDoDocumento("42/011");
    titulo.setNossoNumero("000000000000312");

    titulo.setDigitoDoNossoNumero(calcularDigitoDoNossoNumero(contaBancaria.getCarteira()
        .getCodigo(), titulo.getNossoNumero()));
    titulo.setValor(BigDecimal.valueOf(m.getValor()));
    titulo.setDataDoDocumento(new Date());
    titulo.setDataDoVencimento(m.getDataVencimento());
    titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
    titulo.setAceite(Aceite.N);
    return titulo;
  }

  private static Sacado getSacado(Cliente cliente) {
    /*
     * INFORMANDO DADOS SOBRE O SACADO.
     */
    Sacado sacado = new Sacado(cliente.getNome().toUpperCase(), cliente.getCpfCnpj());

    // Informando o endereço do sacado.
    Endereco enderecoSac = new Endereco();
    enderecoSac.setUF(UnidadeFederativa.CE);
    enderecoSac
        .setLocalidade(cliente.getEndereco().getBairro().getCidade().getNome().toUpperCase());
    enderecoSac.setCep(new CEP(cliente.getEndereco().getCep()));
    enderecoSac.setBairro(cliente.getEndereco().getBairro().getNome().toUpperCase());
    enderecoSac.setLogradouro(cliente.getEndereco().getLogradouro().toUpperCase());
    enderecoSac.setNumero(cliente.getEndereco().getNumero());
    sacado.addEndereco(enderecoSac);
    return sacado;
  }

  private static Cedente getCedente() {
    /*
     * INFORMANDO DADOS SOBRE O CEDENTE.
     */
    Cedente cedente = new Cedente("CLAUDIO CARNEIRO LUZ", "052.749.623-51");
    return cedente;
  }

  private static byte[] groupInPages(List<Boleto> boletos, String filePath,
      File templatePersonalizado) {

    byte[] arq = null;
    BoletoViewer boletoViewer = new BoletoViewer(boletos.get(0));
    boletoViewer.setTemplate(templatePersonalizado);

    List<byte[]> boletosEmBytes = new ArrayList<byte[]>(boletos.size());

    // Adicionando os PDF, em forma de array de bytes, na lista.
    for (Boleto b : boletos) {
      boletosEmBytes.add(boletoViewer.setBoleto(b).getPdfAsByteArray());
    }
    try {
      // Criando o arquivo com os boletos da lista
      arq = mergeFilesInPages(boletosEmBytes);

    } catch (Exception e) {
      throw new IllegalStateException("Erro durante geração do PDF! Causado por "
          + e.getLocalizedMessage(), e);
    }

    return arq;
  }

  public static byte[] mergeFilesInPages(List<byte[]> pdfFilesAsByteArray)
      throws DocumentException, IOException {

    Document document = new Document();
    ByteArrayOutputStream byteOS = new ByteArrayOutputStream();

    PdfWriter writer = PdfWriter.getInstance(document, byteOS);

    document.open();

    PdfContentByte cb = writer.getDirectContent();
    float positionAnterior = 0;

    // Para cada arquivo da lista, cria-se um PdfReader, responsável por ler o arquivo PDF e
    // recuperar informações dele.
    for (byte[] pdfFile : pdfFilesAsByteArray) {

      PdfReader reader = new PdfReader(pdfFile);

      // Faz o processo de mesclagem por página do arquivo, começando pela de número 1.
      for (int i = 1; i <= reader.getNumberOfPages(); i++) {

        float documentHeight = cb.getPdfDocument().getPageSize().getHeight();

        // Importa a página do PDF de origem
        PdfImportedPage page = writer.getImportedPage(reader, i);
        float pagePosition = positionAnterior;

        /*
         * Se a altura restante no documento de destino form menor que a altura do documento,
         * cria-se uma nova página no documento de destino.
         */
        if ((documentHeight - positionAnterior) <= page.getHeight()) {
          document.newPage();
          pagePosition = 0;
          positionAnterior = 0;
        }

        // Adiciona a página ao PDF destino
        cb.addTemplate(page, 0, pagePosition);

        positionAnterior += page.getHeight();
      }
    }

    byteOS.flush();
    document.close();

    byte[] arquivoEmBytes = byteOS.toByteArray();
    byteOS.close();

    return arquivoEmBytes;
  }

  private static String calcularDigitoDoNossoNumero(int carteira, String nossoNumero) {
    StringBuilder sb = new StringBuilder();
    sb.append(carteira).append(EMITENTE_BENEFICIARIO).append(nossoNumero);
    String numero = sb.reverse().toString();

    int multiplicador = 2;
    int soma = 0;

    for (int i = 0; i < numero.length(); i++) {
      int x = Integer.valueOf(Character.toString(numero.charAt(i)));

      // PASSO 1
      int valor = x * multiplicador++;

      if (multiplicador > 9) {
        multiplicador = 2;
      }
      // PASSO 2
      soma += valor;
    }

    // PASSO 3
    int resto = soma % 11;

    // PASSO 4
    int dv = 11 - resto;

    return Integer.toString(dv);
  }

}
