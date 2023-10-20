package io.jaeyeon.numblemybox.folder.domain.repository;

import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FolderRepository extends JpaRepository<Folder, Long> {
  Optional<Folder> findByNameAndOwner(String rootFolderName, Member owner);

  /**
   * 폴더 이름이나 파일 이름이 같은 폴더가 있는지 확인
   *
   * @param path 검색할 폴더 또는 파일의 경로
   * @param name 검색할 폴더 또는 파일의 이름
   * @param memberId 소유자의 ID
   * @return 해당 조건에 맞는 폴더 또는 파일의 개수를 반환. 중복된 이름이 없으면 0을 반환.
   */
  @Query(
      "SELECT COUNT(f) FROM Folder f LEFT JOIN f.files file WHERE (f.path = :path AND f.name = :name AND f.owner.id = :memberId) OR (file.path = :path AND file.fileName = :name AND file.owner.id = :memberId)")
  int countByPathAndNameOrFileName(
      @Param("path") String path, @Param("name") String name, @Param("memberId") Long memberId);
}
