package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.dto.DownloadResponseDto;
import io.jaeyeon.cloudboxserver.file.dto.FileListResponseDto;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import java.io.IOException;
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
      @RequestParam(value = "folderName", defaultValue = "") String folderName,
      @RequestParam(required = false) String continuationToken,
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    FileListResponseDto responseDto = fileService.listFiles(folderName, continuationToken, size);

    model.addAttribute("files", responseDto.files());
    model.addAttribute("folderName", folderName);
    model.addAttribute("nextContinuationToken", responseDto.nextContinuationToken());
    model.addAttribute("hasNextPage", responseDto.hasNextPage());
    return "fileList";
  }

  @PostMapping("/upload")
  public String uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "folderName", defaultValue = "") String folderName,
      RedirectAttributes redirectAttributes) {
    try {
      fileService.uploadFile(file, folderName);
      redirectAttributes.addFlashAttribute(
          "message", "File uploaded successfully: " + file.getOriginalFilename());
    } catch (IOException e) {
      redirectAttributes.addFlashAttribute(
          "message", "Failed to upload file: " + file.getOriginalFilename());
    }
    return "redirect:/files/?folderName=" + folderName;
  }

  @GetMapping("/download")
  public String downloadFile(
      @RequestParam("fileName") String fileName, @RequestParam("folderName") String folderName) {
    String fullPath = folderName.isEmpty() ? fileName : folderName + "/" + fileName;
    DownloadResponseDto responseDto = fileService.generateDownloadPresignedUrl(fullPath);
    return "redirect:" + responseDto.presignedUrl();
  }

  @PostMapping("/create-folder")
  public String createFolder(
      @RequestParam("folderName") String folderName,
      @RequestParam("parentFolder") String parentFolder,
      RedirectAttributes redirectAttributes) {
    try {
      String fullPath = parentFolder.isEmpty() ? folderName : parentFolder + "/" + folderName;
      fileService.createFolder(fullPath);
      redirectAttributes.addFlashAttribute("message", "Folder created successfully: " + folderName);
    } catch (RuntimeException e) {
      redirectAttributes.addFlashAttribute("message", "Failed to create folder: " + folderName);
    }
    return "redirect:/files/?folderName=" + parentFolder;
  }
}
