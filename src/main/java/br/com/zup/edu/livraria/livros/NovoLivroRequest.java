package br.com.zup.edu.livraria.livros;

import org.hibernate.validator.constraints.ISBN;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;

public class NovoLivroRequest {

    @ISBN
    @NotBlank
    private String isbn;

    @NotBlank
    private String titulo;

    @Past
    @NotNull
    private LocalDate publicadoEm;

    public NovoLivroRequest(String isbn, String titulo, LocalDate publicadoEm) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.publicadoEm = publicadoEm;
    }

    public String getIsbn() {
        return isbn;
    }
    public String getTitulo() {
        return titulo;
    }
    public LocalDate getPublicadoEm() {
        return publicadoEm;
    }

    public Livro toModel() {
        return new Livro(isbn, titulo, publicadoEm);
    }
}
