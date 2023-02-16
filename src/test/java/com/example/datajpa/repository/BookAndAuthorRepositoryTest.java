package com.example.datajpa.repository;

import com.example.datajpa.entity.Author;
import com.example.datajpa.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@SpringBootTest
@Transactional
public class BookAndAuthorRepositoryTest {
  @Autowired EntityManagerFactory entityManagerFactory;
  @Autowired AuthorRepository authorRepository;
  @Autowired BookRepository bookRepository;
  @Autowired EntityManager em;

  @Test
  void findAllBooks() {
    Author author1 = new Author("오제웅");
    Author author2 = new Author("정희두");
    Author author3 = new Author("서양훈");
    Author author4 = new Author("이동건");
    authorRepository.save(author1);
    authorRepository.save(author2);
    authorRepository.save(author3);
    authorRepository.save(author4);

    Book book1 = new Book("삶은 왜 힘들까?");
    Book book2 = new Book("고난과 역경");
    Book book3 = new Book("아프니까 청춘이다");
    Book book4 = new Book("아프면 환자지 개XXX");
    bookRepository.save(book1);
    bookRepository.save(book2);
    bookRepository.save(book3);
    bookRepository.save(book4);

    book1.setAuthor(author1);
    book2.setAuthor(author2);
    book3.setAuthor(author3);
    book4.setAuthor(author4);

    em.flush();
    em.clear();

    EntityGraph<Book> entityGraph = em.createEntityGraph(Book.class);
    entityGraph.addAttributeNodes("author");

    List<Book> books =
        em.createQuery("SELECT b FROM Book b", Book.class)
            .setHint("javax.persistence.fetchgraph", entityGraph)
            .getResultList();

    for(Book data : books) {
      System.out.println("data.getTitle() = " + data.getTitle());
      System.out.println("data.getAuthor().getClass() = " + data.getAuthor().getClass());
      System.out.println("data.setAuthor() = " + data.getAuthor());
    }
  }
}
