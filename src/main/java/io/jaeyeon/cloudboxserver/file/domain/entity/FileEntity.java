package io.jaeyeon.cloudboxserver.file.domain.entity;

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

  // Bytes
  @Column(nullable = false)
  private Long size;

  // Path in local file system
  @Column(nullable = false)
  private String path;

  // MIME type
  @Column(nullable = false)
  private String fileType;

  @Builder
  public FileEntity(
      String fileName, Long size, String path, String fileType) {
    this.fileName = fileName;
    this.size = size;
    this.path = path;
    this.fileType = fileType;
  }
}
