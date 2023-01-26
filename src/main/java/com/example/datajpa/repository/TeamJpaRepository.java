package com.example.datajpa.repository;

import com.example.datajpa.entity.Team;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

/**
 * {@link MemberJpaRepository} Member -> Team 으로 변경된 것 뿐 모든 기능은 동일하다.
 * 반복되는 작업을 줄이기 위해 JPA 를 사용한다.
 *
 * 해당 반복 작업은 {@link MemberRepository} 처리해준다.
 */
@Repository
public class TeamJpaRepository {
  @PersistenceContext private EntityManager em;

  public Team save(Team team) {
    em.persist(team);
    return team;
  }

  public void delete(Team team) {
    em.remove(team);
  }

  public List<Team> findAll() {
    return em.createQuery("select t from Team t", Team.class).getResultList();
  }

  public Optional<Team> findById(Long id) {
    Team team = em.find(Team.class, id);
    return Optional.ofNullable(team);
  }

  public long count() {
    return em.createQuery("select count(m) from Team m", Long.class).getSingleResult(); // 단 건 반환
  }
}
