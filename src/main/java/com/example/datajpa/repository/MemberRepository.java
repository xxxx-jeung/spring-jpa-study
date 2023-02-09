package com.example.datajpa.repository;

import com.example.datajpa.dto.MemberDto;
import com.example.datajpa.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
