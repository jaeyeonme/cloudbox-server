package io.jaeyeon.numblemybox.folder.domain.repository;

import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
  Optional<Folder> findByNameAndOwner(String rootFolderName, Member owner);
}
