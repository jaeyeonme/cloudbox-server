package io.jaeyeon.numblemybox.file.controller;

import io.jaeyeon.numblemybox.annotation.CurrentMember;
import io.jaeyeon.numblemybox.file.dto.UploadFileResponse;
import io.jaeyeon.numblemybox.file.service.FileService;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

  private final FileService fileService;

  @PostMapping("/upload")
  public ResponseEntity<UploadFileResponse> uploadFile(
      @RequestPart("file") MultipartFile file, @CurrentMember Member currentMember)
      throws IOException {
    UploadFileResponse uploadFileResponse = fileService.upload(file, currentMember);
    return ResponseEntity.status(HttpStatus.OK).body(uploadFileResponse);
  }

  @GetMapping("/download/{fileName}")
  public ResponseEntity<Resource> downloadFile(
      @PathVariable("fileName") String fileName, @CurrentMember Member member) throws IOException {
    Resource resource = fileService.downloadFile(fileName, member);
    String contentType = Files.probeContentType(Paths.get(resource.getURI()));

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }
}
