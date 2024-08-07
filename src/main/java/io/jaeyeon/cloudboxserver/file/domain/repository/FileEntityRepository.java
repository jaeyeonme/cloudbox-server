package io.jaeyeon.cloudboxserver.file.domain.repository;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {

  Optional<FileEntity> findByFileName(String fileName);

  void deleteByFileName(String fileName);
}
