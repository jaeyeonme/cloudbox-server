package io.jaeyeon.numblemybox.member.controller;

import io.jaeyeon.numblemybox.annotation.AuthenticationRequired;
import io.jaeyeon.numblemybox.folder.dto.StorageInfo;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.dto.MemberRegistration;
import io.jaeyeon.numblemybox.member.service.LoginService;
import io.jaeyeon.numblemybox.member.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member 컨트롤러", description = "회원 관련 API")
public class MemberController {

  private final MemberService memberService;
  private final PasswordEncoder passwordEncoder;
  private final LoginService loginService;

  @PostMapping
  @Tag(name = "회원가입", description = "회원가입 API")
  public ResponseEntity<Void> registration(@RequestBody @Valid MemberRegistration dto) {
    Member member = MemberRegistration.toEntity(dto, passwordEncoder);
    memberService.registrationMember(member);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/duplicated/{email}")
  @Tag(name = "이메일 중복 확인", description = "이메일 중복 확인 API")
  public ResponseEntity<Void> isDuplicatedEmail(@PathVariable String email) {
    boolean duplicatedEmail = memberService.isDuplicatedEmail(email);
    if (duplicatedEmail) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } else {
      return ResponseEntity.ok().build();
    }
  }

  @PostMapping("/login")
  @Tag(name = "로그인", description = "로그인 API")
  public ResponseEntity<Void> login(@RequestBody @Valid MemberRegistration dto) {
    Member member = memberService.validateAndFindMemberByEmail(dto, passwordEncoder);
    loginService.login(member.getId());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @AuthenticationRequired
  @GetMapping("/logout")
  @Tag(name = "로그아웃", description = "로그아웃 API")
  public ResponseEntity<Void> logout() {
    loginService.logout();
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @AuthenticationRequired
  @GetMapping("/storage")
  @Tag(name = "스토리지 정보 조회", description = "스토리지 정보 조회 API")
  public ResponseEntity<StorageInfo> getStorageInfo() {
    Long memberId = loginService.getLoginMemberId();
    StorageInfo storageInfo = memberService.getStorageInfo(memberId);
    return ResponseEntity.ok(storageInfo);
  }
}
