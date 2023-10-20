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

  private final FileEntityRepository filesRepository;
  private final FolderRepository folderRepository;
  private final FileUtility fileUtility;
  private final UUIDUtils uuidUtils;

  @Override
  public UploadFileResponse upload(
      MultipartFile file, Long folderId, String rootFolderName, Member owner) throws IOException {
    // 업로드 된 파일의 원래 이름을 가져온다.
    String originalFilename = file.getOriginalFilename();
    // 파일 확장자를 추출한다.
    String extension = fileUtility.extractFileExtension(originalFilename);
    // 새로운 파일 이름을 UUID를 통해 가져온다.
    String newFileName = uuidUtils.getUUID() + "." + extension;

    // 업로드 대상 폴더를 결정한다.
    Folder targetFolder =
        (folderId != null)
            ? folderRepository
                .findById(folderId)
                .orElseThrow(() -> new FileNotFoundException(ErrorCode.FOLDER_NOT_FOUND))
            : findRootFolderOrThrow(owner, rootFolderName);
    // 대상 폴더의 경로를 가져온다.
    String targetFolderPath = targetFolder.getPath();
    // 폴더가 존재하지 않으면 생성한다.
    fileUtility.createDirectoryIfNotExists(targetFolderPath);
    // 최종적으로 저장될 파일의 경로를 생성한다.
    String filePath = fileUtility.createFilePath(targetFolderPath, newFileName);
    // 같은 이름의 파일이 이미 존재하는지 확인한다.
    if (isDuplicateName(targetFolderPath, newFileName, owner.getId())) {
      throw new FileServiceException(ErrorCode.DUPLICATE_FILE_NAME);
    }
    // 업로드 될 파일의 크기를 체크하며 사용 간으한 공간을 초과하는지 확인
    long uploadFileZie = file.getSize();
    if (uploadFileZie > owner.getAvailableSpace()) {
      throw new FileServiceException(ErrorCode.STORAGE_LIMIT_EXCEEDED);
    }

    try {
      filesRepository.save(
          FileEntity.builder()
              .fileName(originalFilename)
              .fileType(file.getContentType())
              .size(file.getSize())
              .path(filePath)
              .owner(owner)
              .parentFolder(targetFolder)
              .build());
      // 사용자의 공간을 증가시킨다.
      owner.increaseUsedSpace(uploadFileZie);
      file.transferTo(new File(filePath));

      return new UploadFileResponse(
          originalFilename, filePath, file.getContentType(), file.getSize());
    } catch (Exception e) {
      log.error("Failed to upload file: {}", originalFilename, e);
      throw new FileServiceException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  @Override
  public Resource downloadFile(String encodedFileName, Member owner) throws IOException {
    // 인코딩 된 파일 이름을 디코딩
    String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);
    FileEntity fileEntity =
        filesRepository
            .findByFileName(fileName)
            .orElseThrow(() -> new FileNotFoundException(ErrorCode.FILE_NOT_FOUND));

    String filePathStr = fileEntity.getPath();
    // 파일을 읽어 리소스로 반환한다.
    try {
      Path filePath = Paths.get(filePathStr).normalize();
      if (!fileUtility.exists(filePath)) {
        throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND);
      }

      Resource resource = new UrlResource(filePath.toUri());
      // 파일 소유자인지 확인
      if (!fileEntity.isOwnedBy(owner)) {
        throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
      }
      // 파일의 타입을 결정
      String contentType = fileUtility.probeContentType(filePath);
      if (contentType == null) {
        throw new FileStorageException(ErrorCode.FILE_TYPE_DETERMINATION_FAILED);
      }

      return resource;
    } catch (IOException e) {
      log.error("Failed to download file: {}", fileName, e);
      throw new FileServiceException(ErrorCode.FILE_DOWNLOAD_FAILED);
    }
  }

  @Override
  public void deleteFile(Long fileId, Member member) {
    FileEntity fileEntity =
        filesRepository
            .findById(fileId)
            .orElseThrow(() -> new FileServiceException(ErrorCode.FILE_NOT_FOUND));
    // 파일 소유자를 확인
    if (!fileEntity.isOwnedBy(member)) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }

    Long fileSize = fileEntity.getSize();

    try {
      fileUtility.deleteFile(Paths.get(fileEntity.getPath()));
      // 사용자의 사용 공간을 감소
      member.decreaseUsedSpace(fileSize);
      filesRepository.delete(fileEntity);
    } catch (Exception e) {
      log.error("Failed to delete file: {}", fileEntity.getFileName(), e);
      throw new FileServiceException(ErrorCode.FILE_DELETE_FAILED);
    }
  }

  private Folder findRootFolderOrThrow(Member owner, String rootFolderName) {
    return folderRepository
        .findByNameAndOwner(rootFolderName, owner)
        .orElseThrow(() -> new NoSuchElementException(ErrorCode.FOLDER_NOT_FOUND));
  }

  private boolean isDuplicateName(String path, String name, Long memberId) {
    int count = folderRepository.countByPathAndNameOrFileName(path, name, memberId);
    return count > 0;
  }
}
