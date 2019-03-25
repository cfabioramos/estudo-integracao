package br.com.caelum.leilao.teste;

import br.com.caelum.leilao.modelo.Usuario;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.xml.XmlPath;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class UsuariosWSTest {

    private Usuario usuarioEsperado1;
    private Usuario usuarioEsperado2;

    @Before
    public void setUp() {
        usuarioEsperado1 = new Usuario(1L, "Mauricio Aniche", "mauricio.aniche@caelum.com.br");
        usuarioEsperado2 = new Usuario(2L, "Guilherme Silveira", "guilherme.silveira@caelum.com.br");
    }

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

        assertEquals(usuarioEsperado1, usuario1);
        assertEquals(usuarioEsperado2, usuario2);

    }

    @Test
    public void deveRetornarUsuarioPeloId() {

        JsonPath path = given().header("Accept", "application/json")
                .parameter("usuario.id", 1)
                .get("/usuarios/show?usuario.id=1")
                .andReturn().jsonPath();

        Usuario usuario = path.getObject("usuario", Usuario.class);

        System.out.println(path.getString("usuario.nome"));

        assertEquals(usuarioEsperado1, usuario);

    }

}
