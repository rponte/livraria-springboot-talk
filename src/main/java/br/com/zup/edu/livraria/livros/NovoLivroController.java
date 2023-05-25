package br.com.zup.edu.livraria.livros;

import org.hibernate.exception.ConstraintViolationException;
import static org.springframework.http.HttpStatus.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class NovoLivroController {

    private final LivroRepository repository;

    public NovoLivroController(LivroRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @PostMapping("/api/livros")
    public ResponseEntity<?> cadastra(@RequestBody @Valid NovoLivroRequest request) {

        if (repository.existsByIsbn(request.getIsbn())) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "livro já existente no sistema");
        }

        Livro livro = request.toModel();
        repository.save(livro);

        return ResponseEntity
                .status(CREATED).build();
    }

    /**
     * Controller Advice Local
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleUniqueConstraintErrors(ConstraintViolationException e, WebRequest request) {

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "message", "livro já existente no sistema"
        );

        return ResponseEntity
                .unprocessableEntity().body(body); // http 422
    }
}
