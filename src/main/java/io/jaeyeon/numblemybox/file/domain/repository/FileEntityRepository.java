package io.jaeyeon.numblemybox.file.domain.repository;

import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {

  Optional<FileEntity> findByFileName(String fileName);
}
