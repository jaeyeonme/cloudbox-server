package io.jaeyeon.numblemybox.folder.domain.entity;

import java.util.ArrayList;
import java.util.List;

import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "FOLDER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Folder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "FOLDER_ID")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String path; // Path in local file system

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID", nullable = false)
  private Member owner;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PARENT_FOLDER_ID")
  private Folder parentFolder;

  @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
  private List<FileEntity> files = new ArrayList<>();

  @OneToMany(mappedBy = "parentFolder")
  private List<Folder> subFolders = new ArrayList<>();

  @Builder
  public Folder(String name, String path, Member owner, Folder parentFolder) {
    this.name = name;
    this.path = path;
    this.owner = owner;
    this.parentFolder = parentFolder;
  }

  public boolean isOwnedBy(Member member) {
    return this.owner.equals(member);
  }
}
