package io.jaeyeon.cloudboxserver.file.service;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.dto.UploadFileResponse;
import io.jaeyeon.cloudboxserver.file.dto.UploadMultipleFilesResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
  UploadFileResponse upload(MultipartFile file) throws IOException;
  UploadMultipleFilesResponse uploadMultiple(List<MultipartFile> files) throws IOException;
  Resource downloadFile(String fileName) throws IOException;
  void deleteFile(Long fileId);
  List<FileEntity> listFiles(int page, int size);
}
