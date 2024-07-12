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
      @RequestParam(value = "folderPath", defaultValue = "") String folderPath,
      @RequestParam(required = false) String continuationToken,
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    folderPath = normalizeFolderPath(folderPath);
    FileListResponseDto responseDto = fileService.listFiles(folderPath, continuationToken, size);

    model.addAttribute("files", responseDto.files());
    model.addAttribute("folderPath", folderPath);
    model.addAttribute("parentFolderPath", fileService.getParentFolderPath(folderPath));
    model.addAttribute("nextContinuationToken", responseDto.nextContinuationToken());
    model.addAttribute("hasNextPage", responseDto.hasNextPage());
    return "fileList";
  }

  @PostMapping("/upload")
  public String uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "folderPath", defaultValue = "") String folderPath,
      RedirectAttributes redirectAttributes) {
    try {
      String fileUrl = fileService.uploadFile(file, folderPath);
      redirectAttributes.addFlashAttribute(
          "message", "File uploaded successfully: " + file.getOriginalFilename());
      redirectAttributes.addFlashAttribute("fileUrl", fileUrl);
    } catch (IOException e) {
      redirectAttributes.addFlashAttribute(
          "message", "Failed to upload file: " + file.getOriginalFilename());
    }
    return "redirect:/files/?folderPath=" + folderPath;
  }

  @GetMapping("/download")
  public String downloadFile(
      @RequestParam("fileName") String fileName, @RequestParam("folderPath") String folderPath) {
    String fullPath = fileService.getFullPath(folderPath, fileName);
    DownloadResponseDto responseDto = fileService.generateDownloadPresignedUrl(fullPath);
    return "redirect:" + responseDto.presignedUrl();
  }

  @PostMapping("/create-folder")
  public String createFolder(
      @RequestParam("folderName") String folderName,
      @RequestParam("parentFolder") String parentFolder,
      RedirectAttributes redirectAttributes) {
    try {
      String fullPath = fileService.getFullPath(parentFolder, folderName);
      fileService.createFolder(fullPath);
      redirectAttributes.addFlashAttribute("message", "Folder created successfully: " + folderName);
    } catch (RuntimeException e) {
      redirectAttributes.addFlashAttribute("message", "Failed to create folder: " + folderName);
    }
    return "redirect:/files/?folderPath=" + parentFolder;
  }

  private String normalizeFolderPath(String folderPath) {
    return !folderPath.isEmpty() && !folderPath.endsWith("/") ? folderPath + "/" : folderPath;
  }
}
