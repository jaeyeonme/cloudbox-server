package io.jaeyeon.cloudboxserver.file.controller;

import com.amazonaws.HttpMethod;
import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.dto.UploadMultipleFilesResponse;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import io.jaeyeon.cloudboxserver.file.service.S3Service;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

  private final FileService fileService;
  private final S3Service s3Service;

  @GetMapping("/")
  public String listFiles(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    List<FileEntity> files = fileService.listFiles(page, size);
    model.addAttribute("files", files);
    model.addAttribute("currentPage", page);
    return "fileList";
  }

  @PostMapping("/upload")
  public String uploadFile(
      @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
      throws IOException {
    // Presigned URL을 생성
    String fileName = file.getOriginalFilename();
    URL presignedUrl = s3Service.generatePresignedUrl(fileName, "", HttpMethod.PUT);

    s3Service.uploadToS3(file, presignedUrl);

    redirectAttributes.addFlashAttribute(
        "message", "File uploaded successfully: " + file.getOriginalFilename());
    return "redirect:/files/";
  }

  @PostMapping("/upload/multiple")
  public String uploadMultipleFiles(
      @RequestParam("files") List<MultipartFile> files, RedirectAttributes redirectAttributes)
      throws IOException {
    UploadMultipleFilesResponse response = fileService.uploadMultiple(files);
    redirectAttributes.addFlashAttribute(
        "message", "Files uploaded successfully: " + response.files().size());
    return "redirect:/files/";
  }
}
