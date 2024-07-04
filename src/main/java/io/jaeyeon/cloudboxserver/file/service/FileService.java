package io.jaeyeon.cloudboxserver.file.service;

import static io.jaeyeon.cloudboxserver.exception.CloudBoxException.*;

import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileType;
import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.FileListResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadResponseDto;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public void uploadFile(MultipartFile file, String folderName) throws IOException {
    try {
      String originalFileName = file.getOriginalFilename();
      if (originalFileName == null || !originalFileName.contains(".")) {
        throw new FileUploadFailedException(ErrorCode.INVALID_FILE_NAME);
      }

      String fileName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
      String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));

      String fullPath =
          folderName.isEmpty() ? fileName + extension : folderName + "/" + fileName + extension;

      UploadRequestDto requestDto =
          new UploadRequestDto(fullPath, extension, file.getContentType());
      UploadResponseDto responseDto = generatePresignedUrl(requestDto);

      uploadToS3(requestDto, file, new URL(responseDto.presignedUrl()));

      log.info("File uploaded successfully: {}", responseDto.fileUrl());
    } catch (Exception e) {
      log.error("Failed to upload file", e);
      throw new FileUploadFailedException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  public UploadResponseDto generatePresignedUrl(UploadRequestDto requestDto) {
    try {
      validateUploadRequestDto(requestDto);

      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(requestDto.fileName())
              .contentType(requestDto.contentType())
              .build();

      PutObjectPresignRequest putObjectPresignRequest =
          PutObjectPresignRequest.builder()
              .signatureDuration(Duration.ofHours(1))
              .putObjectRequest(putObjectRequest)
              .build();

      PresignedPutObjectRequest presignedPutObjectRequest =
          s3Presigner.presignPutObject(putObjectPresignRequest);

      URL presignedUrl = presignedPutObjectRequest.url();
      URL fileUrl =
          s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(requestDto.fileName()));

      return new UploadResponseDto(presignedUrl.toString(), fileUrl.toString());
    } catch (Exception e) {
      log.error("Failed to generate presigned URL", e);
      throw new FileServiceException(ErrorCode.FILE_PROCESSING_FAILED);
    }
  }

  public DownloadResponseDto generateDownloadPresignedUrl(String fileName) {
    try {
      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(bucket).key(fileName).build();

      GetObjectPresignRequest getObjectPresignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(Duration.ofHours(1))
              .getObjectRequest(getObjectRequest)
              .build();

      PresignedGetObjectRequest presignedGetObjectRequest =
          s3Presigner.presignGetObject(getObjectPresignRequest);

      URL presignedUrl = presignedGetObjectRequest.url();
      return new DownloadResponseDto(presignedUrl.toString());
    } catch (Exception e) {
      log.error("Failed to generate download presigned URL", e);
      throw new FileServiceException(ErrorCode.FILE_DOWNLOAD_FAILED);
    }
  }

  public FileListResponseDto listFiles(String folderName, String continuationToken, int size) {
    try {
      String prefix =
          folderName.isEmpty() ? "" : (folderName.endsWith("/") ? folderName : folderName + "/");
      ListObjectsV2Request.Builder requestBuilder =
          ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).delimiter("/").maxKeys(size);

      if (continuationToken != null && !continuationToken.isEmpty()) {
        requestBuilder.continuationToken(continuationToken);
      }

      ListObjectsV2Response result = s3Client.listObjectsV2(requestBuilder.build());
      List<FileEntity> files = new ArrayList<>();

      for (S3Object s3Object : result.contents()) {
        files.add(createFileEntityFromS3Object(s3Object));
      }

      for (CommonPrefix commonPrefix : result.commonPrefixes()) {
        files.add(createFolderEntityFromCommonPrefix(commonPrefix));
      }

      return FileListResponseDto.of(files, result.nextContinuationToken(), result.isTruncated());
    } catch (Exception e) {
      log.error("Failed to list files", e);
      throw new FileServiceException(ErrorCode.FILE_PROCESSING_FAILED);
    }
  }

  private FileEntity createFileEntityFromS3Object(S3Object s3Object) {
    String key = s3Object.key();
    String fileName = key.substring(key.lastIndexOf('/') + 1);
    String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
    FileType fileType = FileType.fromPath(key);

    return FileEntity.builder()
        .fileName(fileName)
        .size(s3Object.size())
        .extension(extension)
        .path("s3://" + bucket + "/" + key)
        .fileType(fileType)
        .isFolder(fileType == FileType.FOLDER)
        .build();
  }

  private FileEntity createFolderEntityFromCommonPrefix(CommonPrefix commonPrefix) {
    String folderPath = commonPrefix.prefix();
    String folderName = folderPath.substring(0, folderPath.length() - 1); // 마지막 '/' 제거
    folderName = folderName.substring(folderName.lastIndexOf('/') + 1);

    return FileEntity.builder()
        .fileName(folderName)
        .size(0L)
        .extension("")
        .path("s3://" + bucket + "/" + folderPath)
        .fileType(FileType.FOLDER)
        .isFolder(true)
        .build();
  }

  public void uploadToS3(UploadRequestDto uploadRequestDto, MultipartFile file, URL presignedUrl)
      throws IOException {
    try {
      validateUploadRequestDto(uploadRequestDto);
      HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("PUT");
      connection.setRequestProperty("Content-Type", file.getContentType());

      try (OutputStream outputStream = connection.getOutputStream()) {
        outputStream.write(file.getBytes());
      }

      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new FileUploadFailedException(ErrorCode.FILE_UPLOAD_FAILED);
      }
    } catch (Exception e) {
      log.error("Failed to upload file to S3", e);
      throw new FileUploadFailedException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  public URL generateDeletePresignedUrl(String fileName) {
    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucket).key(fileName).build();

      DeleteObjectPresignRequest deleteObjectPresignRequest =
          DeleteObjectPresignRequest.builder()
              .signatureDuration(Duration.ofHours(1))
              .deleteObjectRequest(deleteObjectRequest)
              .build();

      PresignedDeleteObjectRequest presignedDeleteObjectRequest =
          s3Presigner.presignDeleteObject(deleteObjectPresignRequest);

      return presignedDeleteObjectRequest.url();
    } catch (Exception e) {
      log.error("Failed to generate delete presigned URL", e);
      throw new FileServiceException(ErrorCode.FILE_PROCESSING_FAILED);
    }
  }

  public void deleteFile(String fileName) {
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(fileName).build());
    } catch (Exception e) {
      log.error("Failed to delete file", e);
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
    try {
      if (!folderName.endsWith("/")) {
        folderName += "/";
      }
      s3Client.putObject(
          PutObjectRequest.builder().bucket(bucket).key(folderName).build(), RequestBody.empty());

      // 폴더 생성 확인
      HeadObjectRequest headObjectRequest =
          HeadObjectRequest.builder().bucket(bucket).key(folderName).build();
      s3Client.headObject(headObjectRequest);
    } catch (S3Exception e) {
      log.error("Failed to create folder", e);
      throw new FileServiceException(ErrorCode.DIRECTORY_CREATION_FAILED);
    }
  }
}
