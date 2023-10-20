package io.jaeyeon.numblemybox.member.domain.entity;

import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.StorageLimitExceededException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Table(name = "MEMBER")
@EqualsAndHashCode(of = "id")
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

  @Column(name = "allocated_space", nullable = false)
  private Long allocatedSpace = 30 * 1024 * 1024 * 1024L;

  @Column(name = "used_space", nullable = false)
  private Long usedSpace = 0L;

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

  public void increaseUsedSpace(Long size) {
    if (this.usedSpace + size > this.allocatedSpace) {
      throw new StorageLimitExceededException(ErrorCode.STORAGE_LIMIT_EXCEEDED);
    }
    this.usedSpace += size;
  }

  public void decreaseUsedSpace(Long size) {
    this.usedSpace -= size;
    if (this.usedSpace < 0) {
      this.usedSpace = 0L;
    }
  }

  public Long getAvailableSpace() {
    return this.allocatedSpace - this.usedSpace;
  }
}
