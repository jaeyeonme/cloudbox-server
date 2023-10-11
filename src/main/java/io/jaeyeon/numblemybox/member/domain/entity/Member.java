package io.jaeyeon.numblemybox.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Table(name = "MEMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "MEMBER_ID")
  private Long id;

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Builder
  public Member(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public boolean isPasswordMatching(String rawPassword, PasswordEncoder passwordEncoder) {
    return passwordEncoder.matches(rawPassword, this.password);
  }

  public void changePassword(String password) {
    this.password = password;
  }
}
