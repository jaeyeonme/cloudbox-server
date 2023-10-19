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
  private final DirectoryCreator directoryCreator;
  private final UUIDUtils uuidUtils;
  private final FileUtility fileUtility;

  @Override
  public UploadFileResponse upload(
      MultipartFile file, Long folderId, String rootFolderName, Member owner) throws IOException {
    String originalFilename = file.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    String newFileName = uuidUtils.getUUID() + "." + extension;

    Folder targetFolder;
    if (folderId != null) {
      // folderId 값이 제공되면 해당 폴더에 파일을 업로드
      targetFolder =
          folderRepository
              .findById(folderId)
              .orElseThrow(() -> new FileNotFoundException(ErrorCode.FOLDER_NOT_FOUND));
    } else {
      // folderId 값이 null이면 루트 폴더 또는 기본 폴더에 파일을 업로드
      // 여기에서는 예시로 루트 폴더 경로를 사용
      targetFolder = findOrCreateRootFolder(owner, rootFolderName);
    }

    String targetFolderPath = targetFolder.getPath();
    // 디렉토리 생성 로직 호출
    directoryCreator.createDirectoryIfNotExists(targetFolderPath);

    String filePath = Paths.get(targetFolderPath, newFileName).toString();

    try {
      // DB에 파일 정보를 저장
      filesRepository.save(
          FileEntity.builder()
              .fileName(originalFilename)
              .fileType(file.getContentType()) // 파일의 MIME 타입을 설정
              .size(file.getSize()) // 파일 크기를 설정
              .path(filePath) // 파일 경로를 설정
              .owner(owner) // 유저지정
              .parentFolder(targetFolder) // parentFolder 속성을 설정
              .build());

      // 파일 시스템에 파일을 저장
      file.transferTo(new File(filePath));

      // 응답 객체를 생성하여 반환
      UploadFileResponse response =
          new UploadFileResponse(originalFilename, filePath, file.getContentType(), file.getSize());
      log.info("File uploaded successfully: {}", originalFilename);
      return response;

    } catch (Exception e) {
      log.error("Failed to upload file: {}", originalFilename, e);
      throw new FileServiceException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  private Folder findOrCreateRootFolder(Member owner, String rootFolderName) {
    return folderRepository
        .findByNameAndOwner(rootFolderName, owner)
        .orElseThrow(
            () -> {
              // 루트 폴더가 존재하지 않는 경우 새로운 예외를 생성하고 발생
              return new NoSuchElementException(ErrorCode.FOLDER_NOT_FOUND);
            });
  }

  @Override
  public Resource downloadFile(String encodedFileName, Member owner) throws IOException {
    // URL 디코딩 과정 추가
    String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);

    // DB에서 fileName으로 FileEntity 객체를 찾아온다
    FileEntity fileEntity =
        filesRepository
            .findByFileName(fileName)
            .orElseThrow(() -> new FileNotFoundException(ErrorCode.FILE_NOT_FOUND));

    String filePathStr = fileEntity.getPath(); // 실제 저장된 경로와 이름을 가져온다

    try {
      Path filePath = Paths.get(filePathStr).normalize();
      if (!fileUtility.exists(filePath)) {
        throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND);
      }

      Resource resource = new UrlResource(filePath.toUri());

      log.info("FileEntity owner: {}", fileEntity.getOwner());
      log.info("Current owner: {}", owner);
      if (!fileEntity.isOwnedBy(owner)) {
        throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
      }

      String contentType = fileUtility.probeContentType(filePath);

      if (contentType == null) {
        throw new FileStorageException(ErrorCode.FILE_TYPE_DETERMINATION_FAILED);
      }

      log.info("File downloaded successfully: {}", fileName);

      return resource;
    } catch (IOException e) {
      log.error("Failed to download file: {}", fileName, e);
      throw new FileServiceException(ErrorCode.FILE_DOWNLOAD_FAILED);
    }
  }
}
