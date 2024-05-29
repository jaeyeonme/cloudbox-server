package io.jaeyeon.cloudboxserver.file.service;

import static io.jaeyeon.cloudboxserver.exception.CloudBoxException.*;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public URL generatePresignedUrl(String fileName, String extension, HttpMethod method) {
    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60; // 1 hour
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(bucket, fileName + extension)
            .withMethod(method)
            .withExpiration(expiration);
    return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
  }

  public void uploadToS3(MultipartFile file, URL presignedUrl) throws IOException {
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
}
