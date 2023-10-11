package io.jaeyeon.numblemybox.member.domain.repository;

import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByEmail(String email);

  Optional<Member> findMemberByEmail(String email);

  Optional<Member> findMemberById(long id);
}
