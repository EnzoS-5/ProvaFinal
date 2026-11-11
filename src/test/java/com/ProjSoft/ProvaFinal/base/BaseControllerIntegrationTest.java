package com.ProjSoft.ProvaFinal.base;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BaseControllerIntegrationTest {
  @Autowired MockMvc mvc;
  @MockBean JwtDecoder decoder;

  @BeforeEach
  void tokens() {
    for (String role : List.of("ADMIN", "USER", "GUEST")) {
      when(decoder.decode(role))
          .thenReturn(
              Jwt.withTokenValue(role)
                  .header("alg", "RS256")
                  .subject("auth0|" + role)
                  .claim("https://social-insper.com/roles", List.of(role))
                  .build());
    }
  }

  @Test
  void semTokenRetorna401() throws Exception {
    mvc.perform(get("/registros/_status")).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/registros/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":\"teste\"}"))
        .andExpect(status().isUnauthorized());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ADMIN", "USER"})
  void consultaInfraestruturaComRole(String role) throws Exception {
    mvc.perform(get("/registros/_status").header("Authorization", "Bearer " + role))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.banco").value(1));
  }

  @Test
  void adminValidaSemCadastrar() throws Exception {
    mvc.perform(
            post("/registros/validar")
                .header("Authorization", "Bearer ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":\" teste \"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valor").value("teste"))
        .andExpect(jsonPath("$.instante").isNotEmpty());
  }

  @Test
  void userNaoPodePost() throws Exception {
    mvc.perform(
            post("/registros/validar")
                .header("Authorization", "Bearer USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":\"teste\"}"))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{}",
        "{\"valor\":\" \"}",
        "{\"valor\":null}",
        "{\"valor\":\"a\",\"id\":1}",
        "{invalido"
      })
  void entradaInvalidaRetorna400(String body) throws Exception {
    mvc.perform(
            post("/registros/validar")
                .header("Authorization", "Bearer ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void guestSemPermissao() throws Exception {
    mvc.perform(get("/registros/_status").header("Authorization", "Bearer GUEST"))
        .andExpect(status().isForbidden());
  }

  @Test
  void naoPossuiCrud() throws Exception {
    mvc.perform(get("/registros").header("Authorization", "Bearer ADMIN"))
        .andExpect(status().isNotFound());
  }
}
