package br.com.zup.edu.livraria.livros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    public boolean existsByIsbn(String isbn);
}
