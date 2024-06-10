package io.jaeyeon.cloudboxserver.file.service;

import static io.jaeyeon.cloudboxserver.exception.CloudBoxException.*;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileType;
import io.jaeyeon.cloudboxserver.file.domain.repository.FileEntityRepository;
import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadResponseDto;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

  private final AmazonS3 amazonS3;
  private final FileEntityRepository fileEntityRepository;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public UploadResponseDto generatePresignedUrl(UploadRequestDto requestDto) {
    validateUploadRequestDto(requestDto);

    String fileName = requestDto.fileName();
    String extension = requestDto.extension();
    HttpMethod method = HttpMethod.PUT;

    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60; // 1 hour
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(bucket, fileName + extension)
            .withMethod(method)
            .withExpiration(expiration);
    URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

    URL fileUrl = amazonS3.getUrl(bucket, fileName + extension);

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
      String fileName = objectSummary.getKey();
      long fileSize = objectSummary.getSize();
      String filePath = "s3://" + bucket + "/" + fileName;
      String mimeType = URLConnection.guessContentTypeFromName(fileName); // 파일 이름에서 MIME 타입 추측
      FileType fileType = FileType.fromMine(mimeType); // 파일 유형 분류

      FileEntity fileEntity =
          fileEntityRepository
              .findByFileName(fileName)
              .orElse(
                  FileEntity.builder()
                      .fileName(fileName)
                      .size(fileSize)
                      .path(filePath)
                      .mine(fileType)
                      .build());

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
}
