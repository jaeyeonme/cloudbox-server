package io.jaeyeon.numblemybox.member.service;

import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.NumbleMyBoxException;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.domain.repository.MemberRepository;
import io.jaeyeon.numblemybox.member.dto.ChangePasswordRequest;
import io.jaeyeon.numblemybox.member.dto.MemberRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GeneralMemberService implements MemberService {

  private final MemberRepository memberRepository;

  @Override
  public void registrationMember(Member member) {
    boolean isDuplicated = isDuplicatedEmail(member.getEmail());
    if (isDuplicated) {
      throw new NumbleMyBoxException(ErrorCode.MEMBER_ALREADY_EXISTS);
    }
    memberRepository.save(member);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isDuplicatedEmail(String email) {
    return memberRepository.existsByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public Member findMemberByEmail(String email) {
    return memberRepository
        .findMemberByEmail(email)
        .orElseThrow(() -> new NumbleMyBoxException(ErrorCode.MEMBER_NOT_FOUND));
  }

  @Override
  @Transactional(readOnly = true)
  public Member findMemberById(long id) {
    return memberRepository
        .findMemberById(id)
        .orElseThrow(() -> new NumbleMyBoxException(ErrorCode.MEMBER_NOT_FOUND));
  }

  @Override
  public boolean isValidMember(MemberRegistration dto, PasswordEncoder passwordEncoder) {
    Member member = findMemberByEmail(dto.email());
    return member.isPasswordMatching(dto.password(), passwordEncoder);
  }

  @Override
  public void changePassword(
      Member member, ChangePasswordRequest requestDto, PasswordEncoder passwordEncoder) {
    member.changePassword(passwordEncoder.encode(requestDto.newPassword()));
  }
}
