package com.example.datajpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @ManyToOne(fetch = FetchType.LAZY)
  @ToString.Exclude
  private Author author;

  public Book() {
  }

  public Book(String title) {
    this.title = title;
  }

  public Book(Long id, String title) {
    this.id = id;
    this.title = title;
  }
}
