package io.jaeyeon.numblemybox.member.service;

import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.dto.ChangePasswordRequest;
import io.jaeyeon.numblemybox.member.dto.MemberRegistration;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface MemberService {
  void registrationMember(Member member);

  boolean isDuplicatedEmail(String email);

  Member findMemberByEmail(String email);

  Member findMemberById(long id);

  boolean isValidMember(MemberRegistration dto, PasswordEncoder passwordEncoder);

  void changePassword(
      Member member, ChangePasswordRequest requestDto, PasswordEncoder passwordEncoder);
}
