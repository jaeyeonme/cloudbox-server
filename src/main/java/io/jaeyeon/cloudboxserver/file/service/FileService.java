package io.jaeyeon.cloudboxserver.file.service;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.dto.UploadFileResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
  /* 파일 업로드 */
  UploadFileResponse upload(MultipartFile file) throws IOException;

  /* 파일 다운로드 */
  Resource downloadFile(String fileName) throws IOException;

  /* 파일 삭제 */
  void deleteFile(Long fileId);

  /* 파일 리스트 */
  List<FileEntity> listFiles();
}
