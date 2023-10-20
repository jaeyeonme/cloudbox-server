package io.jaeyeon.numblemybox.file.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {

  Optional<FileEntity> findByFileName(String fileName);

	FileEntity findByPathAndFileName(String path, String fileName, Long memberId);
}
