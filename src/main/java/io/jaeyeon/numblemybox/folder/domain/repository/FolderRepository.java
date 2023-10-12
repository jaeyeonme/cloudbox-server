package io.jaeyeon.numblemybox.folder.domain.repository;

import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {}
