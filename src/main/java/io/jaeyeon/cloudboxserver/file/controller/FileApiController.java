package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.FileListResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadResponseDto;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import java.io.IOException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileApiController {

  private final FileService fileService;

  @PostMapping("/upload")
  public ResponseEntity<String> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "folderPath", defaultValue = "") String folderPath)
      throws IOException {
    String fileUrl = fileService.uploadFile(file, folderPath);
    return ResponseEntity.ok(fileUrl);
  }

  @GetMapping("/presigned-url")
  public ResponseEntity<UploadResponseDto> getUploadPresignedUrl(
      @RequestParam("fileName") String fileName,
      @RequestParam("extension") String extension,
      @RequestParam("contentType") String contentType,
      @RequestParam(value = "folderPath", required = false, defaultValue = "") String folderPath) {
    String fullPath = fileService.getFullPath(folderPath, fileName + extension);
    UploadRequestDto requestDto =
        new UploadRequestDto(fullPath, extension, contentType, folderPath);
    UploadResponseDto responseDto = fileService.generatePresignedUrl(requestDto);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/download-url")
  public ResponseEntity<DownloadResponseDto> getDownloadPresignedUrl(
      @RequestParam String fileName) {
    DownloadResponseDto responseDto = fileService.generateDownloadPresignedUrl(fileName);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/delete-url")
  public ResponseEntity<String> getDeletePresignedUrl(@RequestParam String fileName) {
    URL deletePresignedUrl = fileService.generateDeletePresignedUrl(fileName);
    return ResponseEntity.ok(deletePresignedUrl.toString());
  }

  @PostMapping("/folders")
  public ResponseEntity<String> createFolder(@RequestParam String folderName) {
    fileService.createFolder(folderName);
    return ResponseEntity.ok("Folder created successfully: " + folderName);
  }

  @GetMapping("/list")
  public ResponseEntity<FileListResponseDto> listFiles(
      @RequestParam(defaultValue = "") String folderPath,
      @RequestParam(required = false) String continuationToken,
      @RequestParam(defaultValue = "10") int size) {
    FileListResponseDto responseDto = fileService.listFiles(folderPath, continuationToken, size);
    return ResponseEntity.ok(responseDto);
  }
}
