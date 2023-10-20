package io.jaeyeon.numblemybox.file.service;

import io.jaeyeon.numblemybox.common.FileUtility;
import io.jaeyeon.numblemybox.common.UUIDUtils;
import io.jaeyeon.numblemybox.exception.AccessDeniedException;
import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.FileNotFoundException;
import io.jaeyeon.numblemybox.exception.FileServiceException;
import io.jaeyeon.numblemybox.exception.FileStorageException;
import io.jaeyeon.numblemybox.exception.NoSuchElementException;
import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import io.jaeyeon.numblemybox.file.domain.repository.FileEntityRepository;
import io.jaeyeon.numblemybox.file.dto.UploadFileResponse;
import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.folder.domain.repository.FolderRepository;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileLocalServiceImpl implements FileService {

  @Value("${file.storage-directory}")
  private String folderPath;

  private final FileEntityRepository filesRepository;
  private final FolderRepository folderRepository;
  private final FileUtility fileUtility;
  private final UUIDUtils uuidUtils;

  @Override
  public UploadFileResponse upload(MultipartFile file, Long folderId, Member owner) {
    // 1. 파일 원래 이름과 확장자 추출
    String originalFilename = file.getOriginalFilename();
    String extension = fileUtility.extractFileExtension(originalFilename);

    // 2. UUID를 이용한 새로운 파일 이름 생성
    String newFileName = uuidUtils.getUUID() + "." + extension;

    // 루트 폴더 이름을 유저의 이메일로 설정
    String rootFolderName = getRootFolderName(owner);

    // 3. 파일 업로드 폴더 결정 (지정 폴더 or 기본 폴더)
    Folder targetFolder = determineUploadFolder(folderId, owner);
    String targetFolderPath = getFolderPath(targetFolder, rootFolderName);
    fileUtility.createDirectoryIfNotExists(targetFolderPath);

    // 4. 파일 업로드에 대한 제약 조건 검증 (중복 이름, 사용 가능한 공간 등)
    validateFileUploadConstraints(targetFolderPath, newFileName, owner, file.getSize());

    String filePath = fileUtility.createFilePath(targetFolderPath, newFileName);

    try {
      // 5. 실제 파일 저장 및 DB에 파일 정보 저장
      file.transferTo(new File(filePath));
      filesRepository.save(
          FileEntity.builder()
              .fileName(originalFilename)
              .fileType(file.getContentType())
              .size(file.getSize())
              .path(filePath)
              .owner(owner)
              .parentFolder(targetFolder)
              .build());
      owner.increaseUsedSpace(file.getSize());
      return new UploadFileResponse(
          originalFilename, filePath, file.getContentType(), file.getSize());
    } catch (Exception e) {
      log.error("Failed to upload file: {}", originalFilename, e);
      throw new FileServiceException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  @Override
  public Resource downloadFile(String encodedFileName, Member owner) throws IOException {
    // 1. 파일 이름 디코딩
    String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);

    // 2. DB에서 파일 정보 조회
    FileEntity fileEntity =
        filesRepository
            .findByFileName(fileName)
            .orElseThrow(() -> new FileNotFoundException(ErrorCode.FILE_NOT_FOUND));

    // 3. 파일 다운로드에 필요한 리소스 반환
    return getResourceForDownload(fileEntity, owner);
  }

  @Override
  public void deleteFile(Long fileId, Member member) {
    // 1. DB에서 파일 정보 조회
    FileEntity fileEntity =
        filesRepository
            .findById(fileId)
            .orElseThrow(() -> new FileServiceException(ErrorCode.FILE_NOT_FOUND));

    // 2. 파일 소유자 확인
    if (!fileEntity.isOwnedBy(member)) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }

    try {
      // 3. 실제 파일 삭제 및 DB에서 파일 정보 삭제
      fileUtility.deleteFile(Paths.get(fileEntity.getPath()));
      member.decreaseUsedSpace(fileEntity.getSize());
      filesRepository.delete(fileEntity);
    } catch (Exception e) {
      log.error("Failed to delete file: {}", fileEntity.getFileName(), e);
      throw new FileServiceException(ErrorCode.FILE_DELETE_FAILED);
    }
  }

  // 경로
  private String getFolderPath(Folder folder, String rootFolderName) {
    return Paths.get(folderPath, rootFolderName, folder.getName()).normalize().toString();
  }

  // 지정 폴더 또는 기본 폴더 결정
  private Folder determineUploadFolder(Long folderId, Member owner) {
    return (folderId != null)
        ? folderRepository
            .findById(folderId)
            .orElseThrow(() -> new FileNotFoundException(ErrorCode.FOLDER_NOT_FOUND))
        : findRootFolderOrThrow(owner);
  }

  // 파일 업로드 제약 조건 검증
  private void validateFileUploadConstraints(
      String path, String name, Member owner, Long fileSize) {
    if (isDuplicateName(path, name, owner.getId())) {
      throw new FileServiceException(ErrorCode.DUPLICATE_FILE_NAME);
    }

    if (fileSize > owner.getAvailableSpace()) {
      throw new FileServiceException(ErrorCode.STORAGE_LIMIT_EXCEEDED);
    }
  }

  // 기본 폴더 검색 또는 예외 발생
  private Folder findRootFolderOrThrow(Member owner) {
    String rootFolderName = getRootFolderName(owner);
    return folderRepository
        .findByNameAndOwner(rootFolderName, owner)
        .orElseThrow(() -> new NoSuchElementException(ErrorCode.FOLDER_NOT_FOUND));
  }

  private String getRootFolderName(Member owner) {
    return owner.getEmail();
  }

  // 중복된 파일/폴더 이름이 있는지 검사
  private boolean isDuplicateName(String path, String name, Long memberId) {
    return folderRepository.countByPathAndNameOrFileName(path, name, memberId) > 0;
  }

  // 파일 다운로드에 필요한 리소스 생성 및 반환
  private Resource getResourceForDownload(FileEntity fileEntity, Member owner) throws IOException {
    Path filePath = Paths.get(fileEntity.getPath()).normalize();

    if (!fileUtility.exists(filePath)) {
      throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND);
    }

    if (!fileEntity.isOwnedBy(owner)) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }

    String contentType = fileUtility.probeContentType(filePath);
    if (contentType == null) {
      throw new FileStorageException(ErrorCode.FILE_TYPE_DETERMINATION_FAILED);
    }

    return new UrlResource(filePath.toUri());
  }
}
