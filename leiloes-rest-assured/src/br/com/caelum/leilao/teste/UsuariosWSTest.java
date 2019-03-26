package br.com.caelum.leilao.teste;

import br.com.caelum.leilao.modelo.Leilao;
import br.com.caelum.leilao.modelo.Usuario;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.xml.XmlPath;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class UsuariosWSTest {

    private Usuario usuarioEsperado1;
    private Usuario usuarioEsperado2;
    private Leilao leilaoEsperado1;

    @Before
    public void setUp() {
        usuarioEsperado1 = new Usuario(1L, "Mauricio Aniche", "mauricio.aniche@caelum.com.br");
        usuarioEsperado2 = new Usuario(2L, "Guilherme Silveira", "guilherme.silveira@caelum.com.br");
        leilaoEsperado1 = new Leilao(1L, "Geladeira", 800.0, usuarioEsperado1, false);
    }

    @Test
    public void deveRetornarListaDeUsuariosComXml() {

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
    public void deveRetornarListaDeUsuariosComJson() {
        JsonPath path = given()
                //.header("Accept", "application/json")
                .parameter("_format", "json")
                .get("/usuarios")
                .andReturn().jsonPath();

        // List<HashMap> usuarios = path.getList("list", HashMap.class);

        // assertEquals( usuarioEsperado1.getNome(), usuarios.get(0).get("nome").toString() );

        assertEquals( usuarioEsperado1.getNome(), path.getString("list[0].nome") );
        assertEquals( usuarioEsperado2.getNome(), path.getString("list[1].nome") );

    }

    @Test
    public void deveRetornarUsuarioPeloId() {

        JsonPath path = given().header("Accept", "application/json")
                .queryParam("usuario.id", 1)
                // ou
                //.parameter("usuario.id", 1)
                .get("/usuarios/show")
                .andReturn().jsonPath();

        Usuario usuario = path.getObject("usuario", Usuario.class);

        assertEquals(usuarioEsperado1, usuario);
        assertEquals("Mauricio Aniche", path.getString("usuario.nome"));

    }

    @Test
    public void deveRetornarLeilaoPeloId() {

        JsonPath path = given().header("Accept", "application/json")
                .parameter("leilao.id", 1)
                .get("/leiloes/show")
                .andReturn().jsonPath();

        Leilao leilao = path.getObject("leilao", Leilao.class);

        assertEquals(leilaoEsperado1, leilao);
    }

    /*
    @Test
    public void deveRetornarQuantidadeDeLeiloes() {
        XmlPath path = given()
                .header("Accept", "application/xml")
                .get("/leiloes/total")
                .andReturn().xmlPath();

        int total = path.getInt("int");

        assertEquals(2, total);
    }
     */

}
