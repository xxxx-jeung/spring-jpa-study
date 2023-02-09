package com.example.datajpa.repository;

import com.example.datajpa.entity.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {
  @PersistenceContext private EntityManager em;

  public Member save(Member member) {
    em.persist(member);
    return member;
  }

  public void delete(Member member) {
    em.remove(member);
  }

  public List<Member> findAll() {
    return em.createQuery("select m from Member m", Member.class).getResultList(); // 리스트로 반환
  }

  public Optional<Member> findById(Long id) {
    Member member = em.find(Member.class, id);
    return Optional.ofNullable(member);
  }

  public long count() {
    return em.createQuery("select count(m) from Member m", Long.class).getSingleResult(); // 단 건 반환
  }

  public Member find(Long id) {
    return em.find(Member.class, id);
  }

  public List<Member> findByPage(int age, int offset, int limit) {
    return em.createQuery("select m from Member m where m.age = :age order by m.username desc")
        .setParameter("age", age)
        .setFirstResult(offset) // 어디서 부터 가져올꺼야?
        .setMaxResults(limit) // 몇개 가져올거야?
        .getResultList();
  }

  public long totalCount(int age) {
    return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
        .setParameter("age", age)
        .getSingleResult();
  }

  public int bulkAgePlus(int age) {
    int resultCount = em.createQuery("update Member m set m.age = m.age + 1" +
                    "where m.age >= :age")
            .setParameter("age", age)
            .executeUpdate();
    return resultCount;
  }
}
