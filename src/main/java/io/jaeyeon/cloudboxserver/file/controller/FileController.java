package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import java.io.IOException;
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

    fileService.uploadFile(file);

    redirectAttributes.addFlashAttribute(
        "message", "File uploaded successfully: " + file.getOriginalFilename());
    return "redirect:/files/";
  }

  @GetMapping("/download")
  public String downloadFile(@RequestParam("fileName") String fileName) {
    DownloadResponseDto responseDto = fileService.generateDownloadPresignedUrl(fileName);
    return "redirect:" + responseDto.presignedUrl();
  }

  @PostMapping("/create-folder")
  public String createFolder(
      @RequestParam("folderName") String folderName, RedirectAttributes redirectAttributes) {
    try {
      fileService.createFolder(folderName);
      redirectAttributes.addFlashAttribute("message", "Folder created successfully: " + folderName);
    } catch (RuntimeException e) {
      redirectAttributes.addFlashAttribute("message", "Failed to create folder: " + folderName);
    }
    return "redirect:/files/";
  }
}
