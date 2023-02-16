package com.example.datajpa.repository;

import com.example.datajpa.dto.MemberDto;
import com.example.datajpa.entity.Member;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;

/** 인터페이스인데 기본 등록, 수정, 삭제, count 등 기능을 제공해준다. 즉 JPA가 인터페이스를 바탕으로 구현 클래스를 만들어 Injection 해준다. */
public interface MemberRepository extends JpaRepository<Member, Long> {
  @Query("select m from Member m where m.username = :username and m.age = :age")
  Member findUser(@Param("username") String username, @Param("age") int age);

  @Query("select m.username from Member m")
  List<String> findUserNameList();

  @Query(
      "select new com.example.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
  List<MemberDto> findMemberDto();

  @Query("select m.username from Member m where m.username in :names")
  List<String> findByNames(@Param("names") Collection<String> names);

  //  Page<Member> findByAge(int age, Pageable pageable);
  Slice<Member> findByAge(int age, Pageable pageable);

  @Modifying
  @Query("update Member m set m.age = m.age+1 where m.age >= :age")
  int bulkAgePlus(@Param("age") int age);

  // fetch 조인을 하면 팀과 연관 Member 와 연관된 팀 데이터를 한번에 가져온다.
  // 이렇게 fetch 를 작성하려면 JPQL 을 사용해야한다. 하지만 항상 JPQL 을 사용할 수 없는 노릇
  // 그래서 나온게 EntityGraph
  @Query("select m from Member m left join fetch m.team")
  List<Member> findMemberFetchJoin();

  @Override
  @EntityGraph(attributePaths = {"team"})
  List<Member> findAll();

  @EntityGraph(attributePaths = {"team"})
  @Query("select m from Member m")
  List<Member> findMemberEntityGraph();

  @EntityGraph("Member.all")
  List<Member> findEntityGraphByUsername(@Param("username") String username);

  // 이미 내부적으로 최적화를 해놓은 상태이기 때문에 스냅샷을 안 만들어놓는다.
  @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
  Member findReadOnlyByUsername(String username);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Member findLockByUsername(String username);
}
