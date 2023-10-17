package io.jaeyeon.numblemybox.file.domain.entity;

import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member owner;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FOLDER_ID")
  private Folder parentFolder;

  @Builder
  public FileEntity(
      String fileName, Long size, String path, String fileType, Member owner, Folder parentFolder) {
    this.fileName = fileName;
    this.size = size;
    this.path = path;
    this.fileType = fileType;
    this.owner = owner;
    this.parentFolder = parentFolder;
  }

  public boolean isOwnedBy(Member member) {
    return this.owner.equals(member);
  }
}
