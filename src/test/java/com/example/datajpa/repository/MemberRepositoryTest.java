package com.example.datajpa.repository;

import com.example.datajpa.dto.MemberDto;
import com.example.datajpa.entity.Member;
import com.example.datajpa.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
  @Autowired MemberRepository memberRepository;
  @Autowired TeamRepository teamRepository;
  @Autowired MemberJpaRepository memberJpaRepository;

  @Autowired EntityManager em;

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

  @Test
  void findMemberLazy() {
    //given
    //member1 -> teamA
    //member2 -> teamB

    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");

    teamRepository.save(teamA);
    teamRepository.save(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 21, teamB);

    memberRepository.save(member1);
    memberRepository.save(member2);

    em.flush();
    em.clear();

    // when N + 1 = 10 + 1
    // select Member 1
    // 프록시로 표현되는데 fetch 조인을 하면 팀 엔티티를 클래스로 표현됨, 가짜 객체가 아닌 진짜 객체가 나옴
    //List<Member> members = memberRepository.findEntityGraphByUsername("member1");
    List<Member> members = memberRepository.findAll();

    for(Member member : members) {
      System.out.println("==================== :: member.getUsername() = " + member.getUsername());
      System.out.println("==================== :: member.getTeam().getClass() = " + member.getTeam().getClass());
      System.out.println("==================== :: member.getTeam().getName() = " + member.getTeam().getName());
    }
  }

  @Test
  void queryHint() {
    // given
    Member member1 = new Member("member1", 10);
    memberRepository.save(member1);
    em.flush();
    em.clear();

    // when
    Member findMember = memberRepository.findReadOnlyByUsername("member1");
    findMember.setUsername("member2");

    em.flush();
    // 변경감지라는 건데 member1 -> member2 로 변경하고 싶으면 1을 메모리 어딘가에 저장해놔야 한다는 점,
    // 결국 더미 비용이 생겨서 비효율적, 더티 체킹, 이런 것들을 효율적으로 사용할 수 있도록 하이버네이트가 힌트를 제공함.
    // 힌트를 사용하면 변경감지 하지 않음 (스냅샷이 없기 때문)
  }

  @Test
  void lock() {
    // given
    Member member1 = new Member("member1", 10);
    memberRepository.save(member1);
    em.flush();
    em.clear();

    // when
    Member findMember = memberRepository.findLockByUsername("member1");
    findMember.setUsername("member2");
    // 실시간 트래픽이 발생했을 때 lock 을 거는게 좋다.
    // 내용 자체가 깊기 때문에 lock 을 따로 공부하는게 좋다.

    em.flush();

  }
}
