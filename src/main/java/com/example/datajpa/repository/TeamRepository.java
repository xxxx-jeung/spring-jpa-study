package com.example.datajpa.repository;

import com.example.datajpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 인터페이스인데 기본 등록, 수정, 삭제, count 등 기능을 제공해준다.
 * 즉 JPA가 인터페이스를 바탕으로 구현 클래스를 만들어 Injection 해준다.
 * JpaRepository<Entity, Id Type>
 */
public interface TeamRepository extends JpaRepository<Team, Long> {
}
