package br.com.zup.edu.livraria.livros;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class NovoLivroControllerIntegrationTest {

    @Autowired
    private LivroRepository repository;

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("deve cadastrar novo livro")
    public void t1() throws Exception {
        // cenário
        NovoLivroRequest request = new NovoLivroRequest(
                "978-0-4703-2225-3",
                "Design e Arquitetura Java",
                LocalDate.now().minusMonths(6)
        );

        // ação
        String json = toJson(request);
        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // validação
        assertEquals(1, repository.count());
    }

    @Test
    @DisplayName("não deve cadastrar novo livro quando livro já existente")
    public void t2() throws Exception {
        // cenário
        NovoLivroRequest request = new NovoLivroRequest(
                "978-0-4703-2225-3",
                "Design e Arquitetura Java",
                LocalDate.now().minusMonths(6)
        );

        // grava livro no banco
        repository.save(request.toModel());

        // ação
        String json = toJson(request);
        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity());

        // validação
        assertEquals(1, repository.count());
    }

    @Test
    @DisplayName("não deve cadastrar novo livro quando parametros invalidos")
    public void t3() throws Exception {
        // cenário
        NovoLivroRequest request = new NovoLivroRequest(
                "978-0-INVALID-3",
                "",
                LocalDate.now().plusDays(1)
        );

        // ação
        String json = toJson(request);
        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "pt_BR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations", containsInAnyOrder(
                            violation("isbn", "invalid ISBN"),
                            violation("titulo", "must not be blank"),
                            violation("publicadoEm", "must be a past date")
                        )
                ));

        // validação
        assertEquals(0, repository.count());
    }

    @Test
    @DisplayName("não deve cadastrar novo livro repetido quando em alta-concorrência")
    public void t4() throws Exception {
        // cenário
        NovoLivroRequest request = new NovoLivroRequest(
                "978-0-4703-2225-3",
                "Design e Arquitetura Java",
                LocalDate.now().minusMonths(6)
        );

        // ação
        doSyncAndConcurrently(10, s -> {
            try {
                String json = toJson(request);
                mockMvc.perform(post("/api/livros")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // validação
        assertEquals(1, repository.count());
    }

    private String toJson(Object payload) throws JsonProcessingException {
        return mapper.writeValueAsString(payload);
    }

    private Map<String, Object> violation(String field, String message) {
        return Map.of(
                "field", field,
                "message", message
        );
    }

    protected static final Logger LOGGER = LoggerFactory.getLogger(NovoLivroControllerIntegrationTest.class);

    protected void doSyncAndConcurrently(int threadCount, Consumer<String> operation) throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            String threadName = "Thread-" + i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    operation.accept(threadName);
                } catch (Exception e) {
                    LOGGER.error("error while executing operation {}: {}", threadName, e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await();
    }

}
