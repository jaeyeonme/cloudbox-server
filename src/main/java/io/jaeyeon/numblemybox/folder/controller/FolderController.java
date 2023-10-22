package io.jaeyeon.numblemybox.folder.controller;

import io.jaeyeon.numblemybox.annotation.CurrentMember;
import io.jaeyeon.numblemybox.folder.service.FolderService;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/folders")
@Tag(name = "Folder 컨트롤러", description = "폴더 관련 API")
public class FolderController {

  private final FolderService folderService;

  @GetMapping("/download/{folderId}")
  @Tag(name = "폴더 다운로드", description = "폴더 다운로드 API")
  public ResponseEntity<Resource> downloadFolderAsZip(
      @PathVariable("folderId") Long folderId, @CurrentMember Member member) throws IOException {
    Resource resource = folderService.downloadFolderAsZip(folderId, member);

    String encodedFilename =
        URLEncoder.encode(
            Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8); // 파일 이름 인코딩

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename*=UTF-8''" + encodedFilename) // 인코딩된 파일 이름 사용
        .body(resource);
  }
}
