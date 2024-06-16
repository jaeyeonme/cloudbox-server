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
  private String extension;

  @Column(nullable = false)
  private String path;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private FileType fileType;

  @Builder
  public FileEntity(String fileName, Long size, String extension, String path, FileType fileType) {
    this.fileName = fileName;
    this.size = size;
    this.extension = extension;
    this.path = path;
    this.fileType = fileType;
  }
}
