package br.com.zup.edu.livraria.livros;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn;
    private String titulo;
    private LocalDate publicadoEm;

    @Deprecated
    public Livro(){}

    public Livro( String isbn, String titulo, LocalDate publicadoEm) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.publicadoEm = publicadoEm;
    }

    public Long getId() {
        return id;
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
}
