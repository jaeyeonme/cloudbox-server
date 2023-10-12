package io.jaeyeon.numblemybox.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jaeyeon.numblemybox.annotation.AuthenticationRequired;
import io.jaeyeon.numblemybox.folder.dto.StorageInfo;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.dto.MemberRegistration;
import io.jaeyeon.numblemybox.member.service.LoginService;
import io.jaeyeon.numblemybox.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

  private final MemberService memberService;
  private final PasswordEncoder passwordEncoder;
  private final LoginService loginService;

  @PostMapping
  public ResponseEntity<Void> registration(@RequestBody @Valid MemberRegistration dto) {
    Member member = MemberRegistration.toEntity(dto, passwordEncoder);
    memberService.registrationMember(member);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/duplicated/{email}")
  public ResponseEntity<Void> isDuplicatedEmail(@PathVariable String email) {
    boolean duplicatedEmail = memberService.isDuplicatedEmail(email);
    if (duplicatedEmail) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } else {
      return ResponseEntity.ok().build();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody @Valid MemberRegistration dto) {
    boolean validMember = memberService.isValidMember(dto, passwordEncoder);
    if (validMember) {
      loginService.login(memberService.findMemberByEmail(dto.email()).getId());
      return ResponseEntity.status(HttpStatus.OK).build();
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }

  @AuthenticationRequired
  @GetMapping("/logout")
  public ResponseEntity<Void> logout() {
    loginService.logout();
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @AuthenticationRequired
  @GetMapping("/storage")
  public ResponseEntity<StorageInfo> getStorageInfo() {
    Long memberId = loginService.getLoginMemberId();
    StorageInfo storageInfo = memberService.getStorageInfo(memberId);
    return ResponseEntity.ok(storageInfo);
  }
}
