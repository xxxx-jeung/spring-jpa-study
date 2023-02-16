package com.example.datajpa.querydsl;

import com.example.datajpa.entity.Member;
import com.example.datajpa.entity.QMember;
import com.example.datajpa.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.datajpa.entity.QMember.member;
import static com.example.datajpa.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @Autowired private EntityManager em;
  JPAQueryFactory queryFactory;
  //  @PersistenceContext EntityManagerFactory emf;

  @BeforeEach
  void before() {
    queryFactory = new JPAQueryFactory(em);
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamB);

    em.persist(member1);
    em.persist(member2);
  }

  @Test
  public void startQuerydsl() {
    // JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    // QMember m = new QMember("m"); // 별칭 직접 주기 , 같은 테이블일 경우만 따로 별칭 만들어서 사용한다.
    // QMember m = QMember.member; // 이렇게 사용해도된다.

    Member findMember =
        queryFactory.select(member).from(member).where(member.username.eq("member1")).fetchOne();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  public void search() {
    Member findMember =
        queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1").and(member.age.between(10, 30)))
            .fetchOne();

    assertThat(findMember.getUsername()).isEqualTo("member1");

    /*
    goe >=
    gt >
    loe <=
    lt <

    like "member%" like
    contains "member" %member%
    startsWith "member" member%
     */
  }

  @Test
  public void searchAndParam() {
    Member findMember =
        queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"), member.age.eq(10))
            .fetchOne();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  public void resultFetch() {
    List<Member> fetch = queryFactory.selectFrom(member).fetch();
    Member member1 = queryFactory.selectFrom(member).fetchOne();
    Member member2 = queryFactory.selectFrom(member).fetchFirst();

    QueryResults<Member> results =
        queryFactory
            .selectFrom(member)
            .fetchResults(); // result total 을 제공하지만 fetchResults 는 지원중단 됨
    List<Member> results1 = results.getResults();

    queryFactory.selectFrom(member).fetchCount(); // 이것도 지원중단?
  }

  /** */
  @Test
  public void sort() {
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast()) // nullsFirst 도 존재
            .fetch();

    Member member5 = result.get(0);
    Member member6 = result.get(1);
    Member memberNull = result.get(2);

    assertThat(member5.getUsername()).isEqualTo("member5");
    assertThat(member6.getUsername()).isEqualTo("member6");
    assertThat(memberNull.getUsername()).isNull();
  }

  @Test
  public void paging1() {
    List<Member> result =
        queryFactory.selectFrom(member).orderBy(member.username.desc()).offset(1).limit(2).fetch();
  }

  @Test
  public void paging2() {
    QueryResults<Member> memberQueryResults =
        queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();

    // 실무에서 사용할 때도 못 사용할 때도 있다. count 쿼리가 간단하면 가능하지만 복잡하면 성능에 문제가 생길 수 있기 때문에 분리해서 사용하는게 맞다.
  }

  @Test
  public void aggregation() {
    List<Tuple> fetch =
        queryFactory
            .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min())
            .from(member)
            .fetch(); // 쿼리dsl 에서 제공해주는 기능 (Tuple)

    Tuple tuple = fetch.get(0);
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);

    // 실무에서 그렇게 많이 사용하지 않는다
    // dto 로 직접 뽑아오는 방법을 많이 사용한다.
  }

  /** 팀 이름, 각 팀의 평균 연령 */
  @Test
  public void group() {
    List<Tuple> result =
        queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            // having 가능
            .fetch();

    Tuple teamA = result.get(0);
    Tuple teamB = result.get(1);

    assertThat(teamA.get(team.name)).isEqualTo("teamA");
    assertThat(teamA.get(member.age.avg())).isEqualTo(15);

    assertThat(teamB.get(team.name)).isEqualTo("teamB");
    assertThat(teamB.get(member.age.avg())).isEqualTo(35);
  }

  /** 팀 A에 소속 */
  @Test
  public void join() {
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .join(member.team, team)
            // leftjoin, rightjoin, innerjoin
            // on 으로 제약을 걸 수 있음 , 딱 필요한 값만 가져올 수 있다고 함
            .where(team.name.eq("teamA"))
            .fetch();

    assertThat(result).extracting("username").containsExactly("member1");
    // 하이버네이트 요즘 버전은 연관관계가 없는 테이블도 조인할 수 있게 업데이터 됐다.
  }

  /** 세타 조인? 회원의 이름이 팀 이름과 같은 회원 조회?? 카타시안 조인 */
  @Test
  public void theta_join() {}

  /**
   * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회 JPQL : select m, t from Member m left join m.team t
   * on t.name = 'teamA'
   */
  @Test
  void join_on_filtering() {
    List<Tuple> result =
        queryFactory
            .select(member, team)
            .from(member)
            // .leftJoin(member.team, team)
            .join(member.team, team) // 내부조인
            .on(team.name.eq("teamA"))
            // .where(team.name.eq("teamA")) // innerjoin 이면 on 이나 where 결과가 동일하다
            .fetch();

    // B팀도 있는데 left join 이기 때문에 null, A팀만 가져오기 함
    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
  }

  /** 연관관계가 없는 엔티티 외부 조인 회원의 이름이 팀 이름과 같은 대상 외부 조인해라 */
  @Test
  void join_on_no_relation() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    List<Tuple> result =
        queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team)
            .on(member.username.eq(team.name))
            .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
    // 5.1 버전 부터 on 을 사용해 서로 관계가 없는 필드로 외부 조인하는 기능이 추가.
  }

  // 페치 조인
  // 연관된 엔티티를 SQL 한번에 조회하는 기능
  // 주로 성능 최적화에 사용하는 방법이고, 실제 많이 사용한다.
  @Test
  void fetchJoinNo() {
    em.flush();
    em.clear();

    Member findMember =
        queryFactory.selectFrom(member).where(member.username.eq("member1")).fetchOne();

    /*boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
    assertThat(loaded).as("페치 조인 미적용").isFalse();*/
  }

  /** 나이가 가장 많은 회원 조회 */
  @Test
  void subQuery() {
    QMember memberSub = new QMember("memberSub");
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(member.age.eq(JPAExpressions.select(memberSub.age.max()).from(memberSub)))
            .fetch();

    System.out.println(result.get(0));
  }

  /** 나이가 평균인 회원 조회 */
  @Test
  void subQueryGoe() {
    QMember memberSub = new QMember("memberSub");
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(member.age.goe(JPAExpressions.select(memberSub.age.avg()).from(memberSub)))
            .fetch();

    System.out.println(result.get(0));
  }

  /** 나이가 */
  @Test
  void subQueryIn() {
    QMember memberSub = new QMember("memberSub");
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(
                member.age.in(
                    JPAExpressions.select(memberSub.age)
                        .from(memberSub)
                        .where(memberSub.age.gt(10))))
            .fetch();

    System.out.println(result.get(0));
  }

  /** from 절 안에 서브쿼리를 지원하지 않는다. */
  @Test
  void selectSubQuery() {
    QMember memberSub = new QMember("memberSub");
    List<Tuple> fetch =
        queryFactory
            .select(member.username, JPAExpressions.select(memberSub.age.avg()).from(memberSub))
            .from(member)
            .fetch();

    for (Tuple tuple : fetch) {
      System.out.println("tuple = " + tuple);
    }
  }
  // 쿼리를 복잡하게 한방에 짜지말고 두번 세번 호출해서 데이터를 짜집기 하는게 좋다

  @Test
  void basicCase() {
    List<String> fetch =
        queryFactory.select(member.age.when(10).then("열살").otherwise("기타")).from(member).fetch();

    for (String s : fetch) {
      System.out.println("s = " + s);
    }
  }

  @Test
  void complexCase() {
    List<String> result =
        queryFactory
            .select(
                new CaseBuilder()
                    .when(member.age.between(0, 20))
                    .then("0~20살")
                    .when(member.age.between(21, 30))
                    .then("21~30살")
                    .otherwise("기타"))
            .from(member)
            .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }
  }

  @Test
  void constant() {
    List<Tuple> fetch =
        queryFactory.select(member.username, Expressions.constant("A")).from(member).fetch();

    for (Tuple tuple : fetch) {
      System.out.println("tuple = " + tuple);
    }
  }

  @Test
  void concat() {

    List<String> fetch = queryFactory
            .select(member.username.concat("_").concat(member.age.stringValue())) // stringValue enum 처리 할 때 가장 많이 사용
            .from(member)
            .where(member.username.eq("member1"))
            .fetch();

    for (String s : fetch) {
      System.out.println("s = " + s);
    }
  }
}
