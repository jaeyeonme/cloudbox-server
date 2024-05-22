package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.dto.UploadFileResponse;
import io.jaeyeon.cloudboxserver.file.dto.UploadMultipleFilesResponse;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Tag(name = "File 컨트롤러", description = "파일 관련 API")
public class FileApiController {

  private final FileService fileService;

  @PostMapping(
      value = "/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "파일 업로드 API", description = "파일 업로드. 최대 파일 크기는 10MB")
  public ResponseEntity<UploadFileResponse> uploadFile(@RequestPart("file") MultipartFile file)
      throws IOException {
    UploadFileResponse uploadFileResponse = fileService.upload(file);
    return ResponseEntity.status(HttpStatus.OK).body(uploadFileResponse);
  }

  @PostMapping(
      value = "/upload/multiple",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "다중 파일 업로드 API", description = "다중 파일 업로드. 각 파일의 최대 크기는 10MB")
  public ResponseEntity<UploadMultipleFilesResponse> uploadMultipleFiles(
      @RequestParam("files") List<MultipartFile> files) throws IOException {
    UploadMultipleFilesResponse response = fileService.uploadMultiple(files);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping("/download/{fileName}")
  @Operation(summary = "파일 다운로드 API", description = "파일 다운로드")
  public ResponseEntity<Resource> downloadFile(@PathVariable("fileName") String fileName)
      throws IOException {
    Resource resource = fileService.downloadFile(fileName);
    String contentType = Files.probeContentType(Paths.get(resource.getURI()));

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }

  @GetMapping("/list")
  @Operation(summary = "파일 리스트 API", description = "업로드된 파일 리스트를 반환")
  public ResponseEntity<List<FileEntity>> listFiles() {
    List<FileEntity> files = fileService.listFiles();
    return ResponseEntity.status(HttpStatus.OK).body(files);
  }
}
