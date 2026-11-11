package com.ProjSoft.ProvaFinal.base;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class BaseServiceTest {
  final Instant now = Instant.parse("2026-11-11T12:00:00Z");
  final JdbcTemplate jdbc = mock(JdbcTemplate.class);
  final BaseService service = new BaseService(jdbc, Clock.fixed(now, ZoneOffset.UTC));

  @Test
  void confirmaConexaoComBanco() {
    when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
    var response = service.status();
    assertEquals("ok", response.get("status"));
    assertEquals(1, response.get("banco"));
    assertEquals(now, response.get("instante"));
    verify(jdbc).queryForObject("SELECT 1", Integer.class);
  }

  @Test
  void validaSemPersistirDados() {
    var response = service.validar(new BaseRequest(" teste "));
    assertEquals("teste", response.valor());
    assertEquals(now, response.instante());
    verifyNoInteractions(jdbc);
  }
}
