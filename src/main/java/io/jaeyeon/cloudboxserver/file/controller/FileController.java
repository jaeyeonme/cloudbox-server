package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.dto.DownloadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadRequestDto;
import io.jaeyeon.cloudboxserver.file.dto.UploadResponseDto;
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

  private final S3Service s3Service;

  @GetMapping("/")
  public String listFiles(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          Model model) {
    List<FileEntity> files = s3Service.listFiles(page, size);
    model.addAttribute("files", files);
    model.addAttribute("currentPage", page);
    return "fileList";
  }

  @PostMapping("/upload")
  public String uploadFile(
          @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
          throws IOException {

    String fileName = file.getOriginalFilename();

    String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
    UploadRequestDto requestDto = new UploadRequestDto(
            fileName, extension, file.getContentType());
    UploadResponseDto responseDto = s3Service.generatePresignedUrl(requestDto);

    s3Service.uploadToS3(requestDto, file, new URL(responseDto.presignedUrl()));

    redirectAttributes.addFlashAttribute(
            "message", "File uploaded successfully: " + file.getOriginalFilename());
    return "redirect:/files/";
  }

  @GetMapping("/download")
  public String downloadFile(
          @RequestParam("fileName") String fileName) {
    DownloadResponseDto responseDto = s3Service.generateDownloadPresignedUrl(fileName);
    return "redirect:" + responseDto.presignedUrl();
  }
}
