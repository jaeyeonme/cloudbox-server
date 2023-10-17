package io.jaeyeon.numblemybox.file.service;

import io.jaeyeon.numblemybox.exception.AccessDeniedException;
import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.FileDatabaseException;
import io.jaeyeon.numblemybox.exception.FileNotFoundException;
import io.jaeyeon.numblemybox.exception.FileServiceException;
import io.jaeyeon.numblemybox.exception.FileStorageException;
import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import io.jaeyeon.numblemybox.file.domain.repository.FileEntityRepository;
import io.jaeyeon.numblemybox.file.dto.UploadFileResponse;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
  private final ResourceLoader resourceLeader;

  @Override
  public UploadFileResponse upload(MultipartFile file, Member owner) throws IOException {
    String originalFilename = file.getOriginalFilename();
    String encoderFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
    String filePath = Paths.get(folderPath, encoderFilename).toString();
    try {
      // DB에 파일 정보를 저장
      try {
        filesRepository.save(
            FileEntity.builder()
                .fileName(originalFilename)
                .fileType(file.getContentType()) // 파일의 MIME 타입을 설정
                .size(file.getSize()) // 파일 크기를 설정
                .path(filePath) // 파일 경로를 설정
                .owner(owner) // 유저지정
                .build());
      } catch (Exception e) {
        throw new FileDatabaseException(ErrorCode.FILE_DATABASE_ERROR);
      }

      // 파일 시스템에 파일을 저장
      try {
        file.transferTo(new File(filePath));
      } catch (IOException e) {
        throw new FileStorageException(ErrorCode.FILE_STORAGE_ERROR);
      }

      // 응답 객체를 생성하여 반환
      UploadFileResponse response =
          new UploadFileResponse(originalFilename, filePath, file.getContentType(), file.getSize());
      log.info("File uploaded successfully: {}", originalFilename);
      return response;
    } catch (FileDatabaseException | FileStorageException e) {
      log.error("Failed to upload file: {}", originalFilename, e);
      throw e;
    }
  }

  @Override
  public Resource downloadFile(String fileName, Member owner) throws IOException {
    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    Path filePath = Paths.get(folderPath).resolve(encodedFileName).normalize();

    try {
      // 파일에 대한 리소스 객체를 생성
      UrlResource resource = (UrlResource) resourceLeader.getResource("file: " + filePath.toUri());
      // 파일이 존재하지 않는 경우 예외를 발생
      if (!resource.exists()) {
        throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND);
      }

      // 파일 소유자를 확인 (EX: DB에서 파일 정보를 가져오고 소유자를 확인)
      FileEntity fileEntity =
          filesRepository
              .findByFileName(fileName)
              .orElseThrow(() -> new FileNotFoundException(ErrorCode.FILE_NOT_FOUND));

      log.info("FileEntity owner: {}", fileEntity.getOwner());
      log.info("Current owner: {}", owner);
      if (!fileEntity.isOwnedBy(owner)) {
        throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
      }

      // MIME 타입을 결정
      String contentType = Files.probeContentType(filePath);
      if (contentType == null) {
        throw new FileStorageException(ErrorCode.FILE_TYPE_DETERMINATION_FAILED);
      }
      log.info("File downloaded successfully: {}", fileName);
      // 파일 리소스를 반환
      return resource;
    } catch (IOException e) {
      log.error("Failed to download file: {}", fileName, e);
      throw new FileServiceException(ErrorCode.FILE_DOWNLOAD_FAILED);
    }
  }
}
