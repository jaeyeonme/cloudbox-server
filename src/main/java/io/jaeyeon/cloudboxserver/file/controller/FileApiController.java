package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadResponseDto;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
      @RequestParam("contentType") String contentType) {
    UploadRequestDto requestDto = new UploadRequestDto(fileName, extension, contentType);
    UploadResponseDto responseDto = fileService.generatePresignedUrl(requestDto);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/download-url")
  public ResponseEntity<DownloadResponseDto> getDownloadPresignedUrl(
      @RequestParam("fileName") String fileName) {
    DownloadResponseDto responseDto = fileService.generateDownloadPresignedUrl(fileName);
    return ResponseEntity.ok(responseDto);
  }

  @DeleteMapping("/file")
  public ResponseEntity<Void> deleteFile(@RequestParam("fileName") String fileName) {
    fileService.deleteFile(fileName);
    return ResponseEntity.noContent().build();
  }
}
