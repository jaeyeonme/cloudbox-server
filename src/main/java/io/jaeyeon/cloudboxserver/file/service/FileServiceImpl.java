package io.jaeyeon.cloudboxserver.file.service;

import static io.jaeyeon.cloudboxserver.exception.CloudBoxException.*;

import io.jaeyeon.cloudboxserver.common.FileUtility;
import io.jaeyeon.cloudboxserver.common.UUIDUtils;
import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.domain.repository.FileEntityRepository;
import io.jaeyeon.cloudboxserver.file.dto.UploadFileResponse;
import io.jaeyeon.cloudboxserver.file.dto.UploadMultipleFilesResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private final FileEntityRepository fileEntityRepository;

  @Override
  public UploadFileResponse upload(MultipartFile file) throws IOException {
    validateFile(file);
    String originalFilename = file.getOriginalFilename();
    String newFileName = UUIDUtils.getUUIDv4() + "." + originalFilename;

    String filePath = FileUtility.createFilePath(newFileName);

    try {
      file.transferTo(new File(filePath));
      fileEntityRepository.save(
          FileEntity.builder()
              .fileName(originalFilename)
              .fileType(file.getContentType())
              .size(file.getSize())
              .path(filePath)
              .build());
      return new UploadFileResponse(
          originalFilename, filePath, file.getContentType(), file.getSize());
    } catch (IOException e) {
      log.error("File transfer failed for file: {}", originalFilename, e);
      throw new FileTransferException(ErrorCode.FILE_TRANSFER_FAILED);
    } catch (DataAccessException e) {
      log.error("Database operation failed for file: {}", originalFilename, e);
      throw new FileDatabaseSaveException(ErrorCode.FILE_DATABASE_SAVE_FAILED);
    }
  }

  @Override
  public UploadMultipleFilesResponse uploadMultiple(List<MultipartFile> files) throws IOException {
    List<UploadFileResponse> responses = new ArrayList<>();
    long totalSize = 0;

    for (MultipartFile file : files) {
      validateFile(file);
      UploadFileResponse response = upload(file);
      responses.add(response);
      totalSize += file.getSize();
    }

    return new UploadMultipleFilesResponse(responses, totalSize);
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new FileProcessingException(ErrorCode.FILE_EMPTY);
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new FileProcessingException(ErrorCode.FILE_SIZE_EXCEEDED);
    }
  }

  @Override
  public Resource downloadFile(String fileName) throws IOException {
    FileEntity fileEntity =
        fileEntityRepository
            .findByFileName(fileName)
            .orElseThrow(() -> new FileNotFoundException(ErrorCode.FILE_NOT_FOUND));

    Path filePath = Paths.get(fileEntity.getPath()).normalize();
    Resource resource = new UrlResource(filePath.toUri());

    if (resource.exists()) {
      return resource;
    } else {
      throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND);
    }
  }

  @Override
  public void deleteFile(Long fileId) {
    FileEntity fileEntity =
        fileEntityRepository
            .findById(fileId)
            .orElseThrow(() -> new FileNotFoundException(ErrorCode.FILE_NOT_FOUND));

    try {
      FileUtility.deleteFile(Paths.get(fileEntity.getPath()));
      fileEntityRepository.delete(fileEntity);
    } catch (Exception e) {
      throw new FileStorageException(ErrorCode.FILE_DELETE_FAILED);
    }
  }

  @Override
  public List<FileEntity> listFiles(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<FileEntity> filePage = fileEntityRepository.findAll(pageable);
    return filePage.getContent();
  }
}
