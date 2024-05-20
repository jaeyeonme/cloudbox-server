package io.jaeyeon.cloudboxserver.file.service;

import io.jaeyeon.cloudboxserver.common.FileUtility;
import io.jaeyeon.cloudboxserver.common.UUIDUtils;
import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.domain.repository.FileEntityRepository;
import io.jaeyeon.cloudboxserver.file.dto.UploadFileResponse;
import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static io.jaeyeon.cloudboxserver.exception.CloudBoxException.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

  private final FileEntityRepository fileEntityRepository;
  private final FileUtility fileUtility;
  private final UUIDUtils uuidUtils;

  @Override
  public UploadFileResponse upload(MultipartFile file, String targetFolderPath) throws IOException {
    String originalFilename = file.getOriginalFilename();
    String newFileName = uuidUtils.getUUID() + "." + originalFilename;

    fileUtility.createDirectories(Paths.get(targetFolderPath));

    String filePath = fileUtility.createFilePath(targetFolderPath, newFileName);

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
    } catch (Exception e) {
      throw new FileProcessingException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  @Override
  public Resource downloadFile(String fileName) throws IOException {
    FileEntity fileEntity =
            fileEntityRepository
                    .findByFileName(fileName)
                    .orElseThrow(() -> new RuntimeException("File not found " + fileName));

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
      fileUtility.deleteFile(Paths.get(fileEntity.getPath()));
      fileEntityRepository.delete(fileEntity);
    } catch (Exception e) {
      throw new FileStorageException(ErrorCode.FILE_DELETE_FAILED);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<FileEntity> listFiles() {
    return fileEntityRepository.findAll();
  }
}
