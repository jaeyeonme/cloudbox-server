package io.jaeyeon.cloudboxserver.file.controller;

import com.amazonaws.HttpMethod;
import io.jaeyeon.cloudboxserver.file.service.S3Service;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileApiController {

  private final S3Service s3Service;

  @GetMapping("/presigned-url")
  public ResponseEntity<String> getUploadPresignedUrl(
      @RequestParam String fileName, @RequestParam String extension) {
    URL url = s3Service.generatePresignedUrl(fileName, extension, HttpMethod.PUT);
    return ResponseEntity.ok(url.toString());
  }
}
