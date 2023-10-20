package io.jaeyeon.numblemybox.folder.service;

import io.jaeyeon.numblemybox.common.FileUtility;
import io.jaeyeon.numblemybox.exception.AccessDeniedException;
import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.FileNotFoundException;
import io.jaeyeon.numblemybox.exception.FileProcessingException;
import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.folder.domain.repository.FolderRepository;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

  @Value("${file.storage-directory}")
  private String folderPath;

  private final FolderRepository folderRepository;
  private final FileUtility fileUtility;

  @Override
  public Resource downloadFolderAsZip(Long folderId, Member member) throws IOException {
    // 폴더 ID가 유효한지 확인
    Folder folder =
        folderRepository
            .findById(folderId)
            .orElseThrow(() -> new FileNotFoundException(ErrorCode.FOLDER_NOT_FOUND));

    // 소유권 확인
    if (!folder.isOwnedBy(member)) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }

    // 폴더 ID 가져오기
    String strFolderId = String.valueOf(folder.getId());

    // 첫 번째 파일 이름 가져오기 (폴더 내에 파일이 없을 경우에 대한 처리 필요)
    String firstFileName =
        !folder.getFiles().isEmpty() ? folder.getFiles().get(0).getFileName() : "default";

    // 폴더 ID와 첫 번째 파일 이름을 사용하여 zip 파일 이름 생성
    String zipFileName = strFolderId + "_" + firstFileName + ".zip";

    // 폴더 경로 생성 (폴더 경로는 폴더 ID를 기반)
    Path folderPath = Paths.get(this.folderPath).resolve(strFolderId).normalize();

    // zip 파일의 전체 경로 생성 (folderPath를 기반으로 함)
    Path zipFilePath = folderPath.resolve(zipFileName);

    Path parentDir = zipFilePath.getParent();
    if (!fileUtility.exists(parentDir)) {
      fileUtility.createDirectories(parentDir); // 해당 경로에 디렉토리가 없으면 생성
    }

    try (FileOutputStream fos = fileUtility.createFileOutputStream(zipFilePath.toFile());
        ZipOutputStream zos = fileUtility.createZipOutputStream(fos)) {
      // 현재 폴더와 그 하위 폴더/파일을 ZIP 파일에 추가
      addToZip(folder, zos, "");
    } catch (FileNotFoundException e) {
      log.error("File not found: {}", e.getMessage());
      log.error("Stack trace: ", e);
      throw e;
    }

    log.info("Folder download successfully as a ZIP file : {} ", zipFileName);
    // 생성된 ZIP 파일의 urlResource 객체 반환
    return new UrlResource(zipFilePath.toUri().toString());
  }

  private void addToZip(Folder folder, ZipOutputStream zos, String parentPath) throws IOException {
    // 하위 폴더를 압축 파일에 추가
    for (Folder subFolder : folder.getSubFolders()) {
      addToZip(subFolder, zos, parentPath + "/" + subFolder.getName());
    }

    // 현재 폴더의 모든 파일을 압축 파일에 추가
    for (FileEntity file : folder.getFiles()) {
      // 각 파일을 ZIP 파일에 추가
      String absoluteFilePath = file.getPath();
      addFileToZip(absoluteFilePath, zos);
    }
  }

  private void addFileToZip(String fileFullPath, ZipOutputStream zos) throws IOException {
    log.info("Adding file to zip: {}", fileFullPath);
    if (zos == null) {
      throw new IllegalArgumentException("ZipOutputStream cannot be null");
    }

    // 파일의 전체 경로 생성
    Path filePath = Paths.get(fileFullPath);

    try (InputStream fis = fileUtility.newInputStream(filePath)) {
      ZipEntry zipEntry = new ZipEntry(fileFullPath);
      zos.putNextEntry(zipEntry);

      // 버퍼 생성
      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zos.write(bytes, 0, length);
      }
      zos.closeEntry();
    } catch (IOException e) {
      log.error("Failed to add file to zip: {}", fileFullPath, e);
      throw new FileProcessingException(ErrorCode.FILE_PROCESSING_FAILED);
    }
  }
}
