package br.com.caelum.leilao.teste;

import br.com.caelum.leilao.modelo.Usuario;
import com.jayway.restassured.path.xml.XmlPath;
import org.junit.Test;

import java.util.List;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class UsuariosWSTest {

    @Test
    public void deveRetornarListaDeUsuarios() {

        XmlPath path = given()
                .header("Accept", "application/xml")
                .get("/usuarios")
                .andReturn().xmlPath();

        // Ou
        // XmlPath path = get("/usuarios?_format=xml").andReturn().xmlPath();

        Usuario usuario1 = path.getObject("list.usuario[0]", Usuario.class);
        Usuario usuario2 = path.getObject("list.usuario[1]", Usuario.class);

        // Se fosse o caso de pegar a lista
        // List<Usuario> usuarios = path.getList("list.usuario", Usuario.class);

        Usuario esperado1 = new Usuario(1L, "Mauricio Aniche", "mauricio.aniche@caelum.com.br");
        Usuario esperado2 = new Usuario(2L, "Guilherme Silveira", "guilherme.silveira@caelum.com.br");

        assertEquals(esperado1, usuario1);
        assertEquals(esperado2, usuario2);

    }

}
