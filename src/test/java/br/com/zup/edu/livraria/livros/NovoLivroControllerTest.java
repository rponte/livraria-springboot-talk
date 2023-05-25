package br.com.zup.edu.livraria.livros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
class NovoLivroControllerTest {

    @Mock
    private LivroRepository repository;

    @Test
    @DisplayName("deve cadastrar novo livro")
    public void t1() {
        // cenário
        NovoLivroRequest request = new NovoLivroRequest(
                "978-0-4703-2225-3",
                "Arquitetura Java",
                LocalDate.now());

        when(repository.existsByIsbn("978-0-4703-2225-3")).thenReturn(false);

        // ação
        NovoLivroController controller = new NovoLivroController(repository);
        ResponseEntity<?> response = controller.cadastra(request);

        // validação
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("não deve cadastrar novo livro quando livro já existente")
    public void t2() {
        // cenário
        NovoLivroRequest request = new NovoLivroRequest(
                "978-0-4703-2225-3",
                "Outro livro de Arquitetura Java",
                LocalDate.now());

        when(repository.existsByIsbn("978-0-4703-2225-3")).thenReturn(true);

        // ação
        NovoLivroController controller = new NovoLivroController(repository);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.cadastra(request);
        });

        // validação
        assertEquals("livro já existente no sistema", exception.getReason());
    }
}
