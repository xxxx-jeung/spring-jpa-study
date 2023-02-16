package com.example.datajpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Setter
@Getter
@ToString
public class Author {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @OneToMany(mappedBy = "author")
  @ToString.Exclude
  private List<Book> books;

  public Author() {
  }

  public Author(String name) {
    this.name = name;
  }

  public Author(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
