package io.jaeyeon.cloudboxserver.file.service;

import static io.jaeyeon.cloudboxserver.exception.CloudBoxException.*;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileType;
import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadResponseDto;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public void uploadFile(MultipartFile file) throws IOException {
    String originalFileName = file.getOriginalFilename();
    if (originalFileName == null || !originalFileName.contains(".")) {
      throw new IllegalArgumentException("유효한 파일 이름이 없습니다.");
    }

    String fileName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
    String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));

    UploadRequestDto requestDto = new UploadRequestDto(fileName, extension, file.getContentType());
    UploadResponseDto responseDto = generatePresignedUrl(requestDto);

    uploadToS3(requestDto, file, new URL(responseDto.presignedUrl()));
  }

  public UploadResponseDto generatePresignedUrl(UploadRequestDto requestDto) {
    validateUploadRequestDto(requestDto);

    String fileNameWithExtension = requestDto.fileName() + requestDto.extension();
    HttpMethod method = HttpMethod.PUT;

    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60; // 1 hour
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(bucket, fileNameWithExtension)
            .withMethod(method)
            .withExpiration(expiration);
    URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

    URL fileUrl = amazonS3.getUrl(bucket, fileNameWithExtension);

    return new UploadResponseDto(presignedUrl.toString(), fileUrl.toString());
  }

  public DownloadResponseDto generateDownloadPresignedUrl(String fileName) {
    HttpMethod method = HttpMethod.GET;

    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60; // 1 hour
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(bucket, fileName)
            .withMethod(method)
            .withExpiration(expiration);
    URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

    return new DownloadResponseDto(presignedUrl.toString());
  }

  public List<FileEntity> listFiles(int page, int size) {
    ListObjectsV2Request request =
        new ListObjectsV2Request().withBucketName(bucket).withMaxKeys(size);
    ListObjectsV2Result result = amazonS3.listObjectsV2(request);
    List<FileEntity> files = new ArrayList<>();
    for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
      String fileNameWithExtension = objectSummary.getKey();
      long fileSize = objectSummary.getSize();
      String filePath = "s3://" + bucket + "/" + fileNameWithExtension;

      String mimeType = URLConnection.guessContentTypeFromName(fileNameWithExtension);
      FileType fileType = FileType.fromMine(mimeType);

      boolean isFolder = fileNameWithExtension.endsWith("/");

      String fileName;
      String extension;
      if (isFolder) {
        fileName = fileNameWithExtension;
        extension = "";
      } else {

        int lastDotIndex = fileNameWithExtension.lastIndexOf('.');
        if (lastDotIndex == -1) {
          throw new IllegalArgumentException("파일 이름에 확장자가 없습니다: " + fileNameWithExtension);
        }
        fileName = fileNameWithExtension.substring(0, lastDotIndex);
        extension = fileNameWithExtension.substring(lastDotIndex);
      }

      FileEntity fileEntity =
          FileEntity.builder()
              .fileName(fileName)
              .extension(extension)
              .size(fileSize)
              .path(filePath)
              .fileType(fileType)
              .isFolder(isFolder)
              .build();

      files.add(fileEntity);
    }
    return files;
  }

  public void uploadToS3(UploadRequestDto uploadRequestDto, MultipartFile file, URL presignedUrl)
      throws IOException {
    validateUploadRequestDto(uploadRequestDto);
    HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("PUT");
    connection.setRequestProperty("Content-Type", file.getContentType());

    try (OutputStream outputStream = connection.getOutputStream()) {
      outputStream.write(file.getBytes());
    } catch (IOException e) {
      throw new FileUploadFailedException(ErrorCode.FILE_UPLOAD_FAILED);
    }

    int responseCode = connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new FileUploadFailedException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  public URL generateDeletePresignedUrl(String fileName) {
    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60; // 1 hour
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(bucket, fileName)
            .withMethod(HttpMethod.DELETE)
            .withExpiration(expiration);

    return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
  }

  public void deleteFile(String fileName) {
    try {
      URL deletePresignedUrl = generateDeletePresignedUrl(fileName);
      log.info("생성된 DELETE용 Presigned URL: {}", deletePresignedUrl);

      HttpURLConnection connection = (HttpURLConnection) deletePresignedUrl.openConnection();
      connection.setRequestMethod("DELETE");

      int responseCode = connection.getResponseCode();
      log.info("S3 DELETE 요청의 응답 코드: {}", responseCode);

      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT
          && responseCode != HttpURLConnection.HTTP_OK) {
        throw new FileDeleteFailedException(ErrorCode.FILE_DELETE_FAILED);
      }
      log.info("S3 파일 삭제 완료: {}", fileName);
    } catch (IOException e) {
      log.error("S3에서 파일 삭제 실패", e);
      throw new FileDeleteFailedException(ErrorCode.FILE_DELETE_FAILED);
    }
  }

  private void validateUploadRequestDto(UploadRequestDto uploadRequestDto) {
    if (uploadRequestDto.fileName() == null || uploadRequestDto.fileName().isBlank()) {
      throw new FileUploadFailedException(ErrorCode.INVALID_FILE_NAME);
    }
    if (uploadRequestDto.extension() == null || uploadRequestDto.extension().isBlank()) {
      throw new FileUploadFailedException(ErrorCode.INVALID_EXTENSION);
    }
    if (uploadRequestDto.contentType() == null || uploadRequestDto.contentType().isBlank()) {
      throw new FileUploadFailedException(ErrorCode.INVALID_CONTENT_TYPE);
    }
  }

  public void createFolder(String folderName) {
    if (!folderName.endsWith("/")) {
      folderName += "/";
    }

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);

    amazonS3.putObject(bucket, folderName, new ByteArrayInputStream(new byte[0]), metadata);
  }
}
