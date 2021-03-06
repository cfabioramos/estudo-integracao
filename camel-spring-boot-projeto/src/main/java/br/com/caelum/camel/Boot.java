package br.com.caelum.camel;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Boot {

    @Autowired
    private CamelContext context;

    /**
     * Configuração para adicionar o activemq no CamelContext.
     *
     * @throws Exception
     */
    @PostConstruct
    public void init() throws Exception {
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61618"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Boot.class, args);
    }

	/*
	Uma alternativa caso fosse necessário fazer com que a classe ProdutoService
	herdasse de outra classe.
	*/
    /*
	@Bean
    public RoutesBuilder rota() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:pedidos").
                to("activemq:queue:pedidos");
            }
        };
    }
	 */

}
