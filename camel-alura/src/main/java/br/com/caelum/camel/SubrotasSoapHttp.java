package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class SubrotasSoapHttp {

    /**
     * @param args
     * @throws Exception O objetivo do multicast é configurar para que cada sub-rota receba o body original.
     *                   <p>
     *                   Esse Java doc é para explicar outra possibilidade, ao invés do multicast, o Staged event-driven architecture ou simplesmente SEDA:
     *                   A ideia do SEDA é que cada rota (e sub-rota) possua uma fila dedicada de entrada
     *                   e as rotas enviam mensagens para essas filas para se comunicar.
     *                   Dentro dessa arquitetura, as mensagens são chamadas de eventos.
     *                   A rota fica então consumindo as mensagens/eventos da fila, tudo funcionando em paralelo.
     *                   <p>
     *                   Para usar SEDA basta substituir a palavra direct por seda, com isso, o multicast se tornará desnecessário:
     *                   from("file:pedidos?delay=5s&noop=true").
     *                   routeId("rota-pedidos").
     *                   to("seda:soap").
     *                   to("seda:http");
     *                   <p>
     *                   from("seda:soap").
     *                   routeId("rota-soap").
     *                   log("chamando servico soap ${body}").
     *                   to("mock:soap");
     */

    /*
    Este trecho poderia ser utilizado no caso de ter algum tratamento
    para excecoes específicas, como dito no site do Alura:

    O errorHandler permite definir um tratamento de error bastante detalhado,
        no entanto não podemos tratar exceções específicas.
        Imagine que queremos um tratamento específico para uma IOException e
        outro para uma ParsingException. Usando apenas o errorHandler é impossível.

    onException(SAXParseException.class).
    handled(true).
        maximumRedeliveries(3).
            redeliveryDelay(4000).
        onRedelivery(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                    int counter = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER);
                    int max = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER);
                    System.out.println("Redelivery - " + counter + "/" + max );;
            }
    });

        O método handled(..) realmente tira a mensagem venenosa da rota
            (depois das tentativas). Sem esse método a mensagem continuaria
            na rota original e consequentemente geraria problemas.
     */
    public static void main(String[] args) throws Exception {

        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                from("file:pedidos?delay=5s&noop=true").
                        routeId("rota-principal").
                        errorHandler(
                                // É comum utilizarmos uma fila (Dead Letter Queue) para este destino
                                // deadLetterChannel("activemq:queue:pedidos.DLQ").
                                deadLetterChannel("file:erro").
                                        useOriginalMessage().
                                        logExhaustedMessageHistory(true).
                                        maximumRedeliveries(3).
                                        redeliveryDelay(5000).
                                        onRedelivery((exchange) -> {
                                            int counter = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER);
                                            int max = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER);
                                            System.out.println("Redelivery - " + counter + "/" + max);
                                        })).
                        to("validator:pedido.xsd").
                        multicast().
                        // parallelProcessing(): configuracao do multicast para chamar cada sub-rota em uma Thread separada de forma paralela.
                        // parallelProcessing().
                        // to("direct:http").
                                to("direct:soap");

                from("direct:soap").
                        routeId("rota-soap").
                        // to("xslt:file://C:\xslt\pedidos-para-soap.xslt").
                        // to("xslt:br/com/caelum/xslt/pedidos-para-soap.xslt").
                                to("xslt:pedido-para-soap.xslt").
                        // log("Resultado do Template: ${body}").
                                to("mock:soap");
						/*
							setHeader(Exchange.CONTENT_TYPE,constant("text/xml")).
							to("http4://localhost:8080/webservices/financeiro");
						 */

                from("direct:http").
                        routeId("rota-http").
                        setProperty("pedidoId", xpath("/pedido/id/text()")).
                        setProperty("email", xpath("/pedido/pagamento/email-titular/text()")).
                        split().
                        xpath("/pedido/itens/item").
                        filter().
                        xpath("/item/formato[text()='EBOOK']").
                        setProperty("ebookId", xpath("/item/livro/codigo/text()")).
                        setHeader(Exchange.HTTP_QUERY,
                                simple("clienteId=${property.email}&pedidoId=${property.pedidoId}&ebookId=${property.ebookId}")).
                        to("http4://localhost:8080/webservices/ebook/item");
            }
        });

        context.start();
        Thread.sleep(20000);
        context.stop();
    }
}
