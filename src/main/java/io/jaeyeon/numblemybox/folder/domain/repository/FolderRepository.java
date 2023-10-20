package io.jaeyeon.numblemybox.folder.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.member.domain.entity.Member;

public interface FolderRepository extends JpaRepository<Folder, Long> {
  Optional<Folder> findByNameAndOwner(String rootFolderName, Member owner);

	Folder findByPathAndName(String path, String name, Long memberId);
}
