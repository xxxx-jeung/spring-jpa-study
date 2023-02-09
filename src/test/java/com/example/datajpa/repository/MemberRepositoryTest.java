package com.example.datajpa.repository;

import com.example.datajpa.dto.MemberDto;
import com.example.datajpa.entity.Member;
import com.example.datajpa.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
  @Autowired MemberRepository memberRepository;
  @Autowired TeamRepository teamRepository;
  @Autowired MemberJpaRepository memberJpaRepository;

  @Test
  void selectUser() {
    // given
    Member user1 = new Member("ohjeung1", 22);
    Member user2 = new Member("ohjeung2", 23);
    memberRepository.save(user1);
    memberRepository.save(user2);

    // when
    Member findUser = memberRepository.findUser(user1.getUsername(), user1.getAge());

    // then
    assertThat(findUser.getAge()).isEqualTo(22);
    assertThat(findUser.getUsername()).isEqualTo("ohjeung1");
  }

  @Test
  void selectUserNameList() {
    // given
    Member user1 = new Member("ohjeung1", 22);
    Member user2 = new Member("ohjeung2", 23);
    memberRepository.save(user1);
    memberRepository.save(user2);

    // when
    List<String> userNameList = memberRepository.findUserNameList();

    // then
    System.out.println("userNameList = " + userNameList);
  }

  @Test
  public void testMember() {
    Member member = new Member("memberA");
    Member savedMember = memberRepository.save(member);
    Member findMember = memberRepository.findById(savedMember.getId()).get();
    assertThat(findMember.getId()).isEqualTo(member.getId());
    assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    assertThat(findMember).isEqualTo(member); // JPA 엔티티 동일성
  }

  @Test
  public void basicCRUD() {
    Member member1 = new Member("member1");
    Member member2 = new Member("member2");
    memberRepository.save(member1);
    memberRepository.save(member2);

    // 단건 조회 검증
    Member findMember1 = memberRepository.findById(member1.getId()).get();
    Member findMember2 = memberRepository.findById(member2.getId()).get();
    assertThat(findMember1).isEqualTo(member1);
    assertThat(findMember2).isEqualTo(member2);

    // 리스트 조회 검증
    List<Member> all = memberRepository.findAll();
    assertThat(all.size()).isEqualTo(2);

    // 카운트 검증
    long count = memberRepository.count();
    assertThat(count).isEqualTo(2);

    // 삭제 검증
    memberRepository.delete(member1);
    memberRepository.delete(member2);

    long deletedCount = memberRepository.count();
    assertThat(deletedCount).isEqualTo(0);
  }

  @Test
  void selectMemberDtoTest() {
    // given
    Team team = new Team("내마음대로팀");
    teamRepository.save(team);

    Member user1 = new Member("ohjeung1", 22);
    user1.setTeam(team);
    memberRepository.save(user1);

    // when
    List<MemberDto> memberDto = memberRepository.findMemberDto();

    // then
    System.out.println("memberDto = " + memberDto);
    assertThat(memberDto.get(0)).isEqualTo(new MemberDto(2L, "ohjeung1", "내마음대로팀"));
  }

  @Test
  void findByNamesTest() {
    // given
    Member user1 = new Member("ohjeung1", 22);
    Member user2 = new Member("ohjeung2", 23);
    memberRepository.save(user1);
    memberRepository.save(user2);

    // when
    List<String> byNames = memberRepository.findByNames(List.of("ohjeung1", "ohjeung2"));

    // then
    assertThat(byNames.get(0)).isEqualTo("ohjeung1");
    assertThat(byNames.get(1)).isEqualTo("ohjeung2");
  }

  @Test
  void paging() {
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));
    memberRepository.save(new Member("member6", 10));
    memberRepository.save(new Member("member7", 10));
    memberRepository.save(new Member("member8", 10));

    int age = 10;
    int offset = 0;
    int limit = 3;

    List<Member> byPage = memberJpaRepository.findByPage(age, offset, limit);
    long totalCount = memberJpaRepository.totalCount(age);

    assertThat(byPage.size()).isEqualTo(3);
    assertThat(totalCount).isEqualTo(8);
  }

  @Test
  void pagingTest() {
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));
    memberRepository.save(new Member("member6", 10));
    memberRepository.save(new Member("member7", 10));
    memberRepository.save(new Member("member8", 10));

    int age = 10;
    PageRequest pageRequest = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "username"));
    Slice<Member> pageReuslt = memberRepository.findByAge(age, pageRequest);
    Slice<MemberDto> map = pageReuslt.map(m -> new MemberDto(m.getId(), m.getUsername(), null));


    // then
    List<Member> content = pageReuslt.getContent();
    //long totalElements = pageReuslt.getTotalElements();

    for (Member member : pageReuslt) {
      System.out.println("member = " + member);
    }
    assertThat(content.size()).isEqualTo(3);
    //assertThat(pageReuslt.getTotalElements()).isEqualTo(5);
  }

  @Test
  void bulkUpdate() {
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 19));
    memberRepository.save(new Member("member3", 20));
    memberRepository.save(new Member("member4", 21));
    memberRepository.save(new Member("member5", 40));

    // when
    int resultCount = memberJpaRepository.bulkAgePlus(20);

    //then
    assertThat(resultCount).isEqualTo(3);
  }

  @Test
  void bulkUpdateSpringJpa() {
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 19));
    memberRepository.save(new Member("member3", 20));
    memberRepository.save(new Member("member4", 21));
    memberRepository.save(new Member("member5", 40));

    // when
    int resultCount = memberRepository.bulkAgePlus(20);

    //then
    assertThat(resultCount).isEqualTo(3);
  }
}
