package com.example.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

/*
    @EnableJpaRepositories
    SpringBootApplication 아래에 두면 알아서 레파지토리 경로부터 자바 파일까지 읽어들인다.
    만약 레파지토리 경로가 다른위치에 있다면 @EnableJpaRepositories(basePackages = "xxx.xxx")
    경로를 설정하면 레파지토리를 읽어들인다.
*/
public class DataJpaApplication {

  public static void main(String[] args) {
    SpringApplication.run(DataJpaApplication.class, args);
  }
}
