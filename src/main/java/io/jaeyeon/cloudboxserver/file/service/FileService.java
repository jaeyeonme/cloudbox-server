package io.jaeyeon.cloudboxserver.file.service;

import io.jaeyeon.cloudboxserver.file.dto.UploadFileResponse;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
  /* 파일 업로드 */
  UploadFileResponse upload(MultipartFile file, String targetFolderPath) throws IOException;

  /* 파일 다운로드 */
  Resource downloadFile(String fileName) throws IOException;

  /* 파일 삭제 */
  void deleteFile(Long fileId);
}
