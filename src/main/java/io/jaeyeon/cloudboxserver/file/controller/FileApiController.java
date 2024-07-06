package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.FileListResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadResponseDto;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileApiController {

  private final FileService fileService;

  @GetMapping("/presigned-url")
  public ResponseEntity<UploadResponseDto> getUploadPresignedUrl(
      @RequestParam("fileName") String fileName,
      @RequestParam("extension") String extension,
      @RequestParam("contentType") String contentType,
      @RequestParam(value = "folderPath", required = false, defaultValue = "") String folderPath) {

    String fullPath = folderPath.isEmpty() ? fileName : folderPath + "/" + fileName;
    UploadRequestDto requestDto = new UploadRequestDto(fullPath, extension, contentType);
    UploadResponseDto responseDto = fileService.generatePresignedUrl(requestDto);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/download-url")
  public ResponseEntity<DownloadResponseDto> getDownloadPresignedUrl(
      @RequestParam("fileName") String fileName) {
    DownloadResponseDto responseDto = fileService.generateDownloadPresignedUrl(fileName);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/delete-url")
  public ResponseEntity<String> getDeletePresignedUrl(@RequestParam("fileName") String fileName) {
    URL deletePresignedUrl = fileService.generateDeletePresignedUrl(fileName);
    return ResponseEntity.ok(deletePresignedUrl.toString());
  }

  @PostMapping("/create-folder")
  public ResponseEntity<String> createFolder(@RequestParam("folderName") String folderName) {
    try {
      fileService.createFolder(folderName);
      return ResponseEntity.ok("Folder created successfully: " + folderName);
    } catch (Exception e) {
      log.error("Failed to create folder", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to create folder: " + folderName);
    }
  }

  @GetMapping("/list")
  public ResponseEntity<FileListResponseDto> listFiles(
      @RequestParam(defaultValue = "") String folderName,
      @RequestParam(required = false) String continuationToken,
      @RequestParam(defaultValue = "10") int size) {
    FileListResponseDto responseDto = fileService.listFiles(folderName, continuationToken, size);
    return ResponseEntity.ok(responseDto);
  }
}
