package io.jaeyeon.numblemybox.member.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.NumbleMyBoxException;
import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.folder.domain.repository.FolderRepository;
import io.jaeyeon.numblemybox.folder.dto.StorageInfo;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.domain.repository.MemberRepository;
import io.jaeyeon.numblemybox.member.dto.ChangePasswordRequest;
import io.jaeyeon.numblemybox.member.dto.MemberRegistration;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GeneralMemberService implements MemberService {

  @Value("${file.storage-directory}")
  private String baseFolderPath;

  private final MemberRepository memberRepository;
  private final FolderRepository folderRepository;

  @Override
  public void registrationMember(Member member) {
    boolean isDuplicated = isDuplicatedEmail(member.getEmail());
    if (isDuplicated) {
      throw new NumbleMyBoxException(ErrorCode.MEMBER_ALREADY_EXISTS);
    }
    memberRepository.save(member);
    // 사용자 등록 후 Root 폴더 생성 및 사용 가능한 공간 설정
    createRootFolderAndSetInitialSpace(member);
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
  public Member validateAndFindMemberByEmail(MemberRegistration dto, PasswordEncoder passwordEncoder) {
    Member member = findMemberByEmail(dto.email());
    if (!member.isPasswordMatching(dto.password(), passwordEncoder)) {
      throw new NumbleMyBoxException(ErrorCode.INVALID_PASSWORD);
    }
    return member;
  }

  @Override
  @Transactional(readOnly = true)
  public Member findMemberById(long id) {
    return memberRepository
        .findMemberById(id)
        .orElseThrow(() -> new NumbleMyBoxException(ErrorCode.MEMBER_NOT_FOUND));
  }


  @Override
  public void changePassword(
      Member member, ChangePasswordRequest requestDto, PasswordEncoder passwordEncoder) {
    member.changePassword(passwordEncoder.encode(requestDto.newPassword()));
  }

  @Override
  public void createRootFolderAndSetInitialSpace(Member member) {
    // ROOT 폴더 생성
    String rootFolderPath = baseFolderPath + "/" + member.getRootFolderId();
    Folder rootFolder =
        Folder.builder().name(member.getRootFolderId()).path(rootFolderPath).owner(member).build();
    folderRepository.save(rootFolder);
  }

  @Override
  public StorageInfo getStorageInfo(Long memberId) {
    Member member =
        memberRepository
            .findMemberById(memberId)
            .orElseThrow(() -> new NumbleMyBoxException(ErrorCode.MEMBER_NOT_FOUND));
    return new StorageInfo(member.getAllocatedSpace(), member.getUsedSpace());
  }
}
