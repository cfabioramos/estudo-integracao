package br.com.caelum.camel;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.jms.MessageListener;

public class SubrotasSoapHttpFromActiveMQ {

    public static void main(String[] args) throws Exception {

        CamelContext context = new DefaultCamelContext();
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61618"));
        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                //Outra possibilidade de posicionamento do errorHandler
                errorHandler(
                        deadLetterChannel("activemq:queue:pedidos.DLQ").
                                useOriginalMessage().
                                logExhaustedMessageHistory(true).
                                maximumRedeliveries(3).
                                redeliveryDelay(5000).
                                onRedelivery((exchange) -> {
                                    int counter = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER);
                                    int max = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER);
                                    System.out.println("Redelivery - " + counter + "/" + max);
                                }));

                /*
                    Com o Camel sendo o consumidor das mensagens,
                    não é tão necessário usar esse MessageListener,
                    no entanto, nada impede cadastrá-lo.
                 */
                /*
                from("activemq:queue:pedidos.req").
                        bean(TratadorMensagemJms.class).
                        log("Pattern: ${exchange.pattern}").
                        setBody(constant("novo conteudo da mensagem")).
                        setHeader(Exchange.FILE_NAME, constant("teste.txt")).
                        to("file:saida");
                        */

                from("activemq:queue:pedidos").
                        routeId("rota-principal").
                        to("validator:pedido.xsd").
                        multicast().
                        to("direct:soap");
                        //to("direct:http");

                from("direct:soap").
                        routeId("rota-soap").
                        to("xslt:pedido-para-soap.xslt").
                        log("Resultado do Template: ${body}").
                        to("mock:soap");

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

    class TratadorMensagemJms implements MessageListener { //import javax.jmx
        @Override
        public void onMessage(javax.jms.Message message) {

        }
    }
}
