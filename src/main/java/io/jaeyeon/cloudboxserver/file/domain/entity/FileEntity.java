package io.jaeyeon.cloudboxserver.file.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "FILE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "FILE_ID")
  private Long id;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false)
  private String path;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private FileType mine;

  @Builder
  public FileEntity(String fileName, Long size, String path, FileType mine) {
    this.fileName = fileName;
    this.size = size;
    this.path = path;
    this.mine = mine;
  }
}
