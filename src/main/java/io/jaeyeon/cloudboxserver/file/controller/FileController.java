package io.jaeyeon.cloudboxserver.file.controller;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import io.jaeyeon.cloudboxserver.file.dto.UploadMultipleFilesResponse;
import io.jaeyeon.cloudboxserver.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    @Value("${file.storage-directory}")
    private String uploadDir;

    private final FileService fileService;

    @GetMapping("/")
    public String listFiles(Model model) {
        List<FileEntity> files = fileService.listFiles();
        model.addAttribute("files", files);
        return "fileList";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        fileService.upload(file);
        redirectAttributes.addFlashAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());
        return "redirect:/files/";
    }

    @PostMapping("/upload/multiple")
    public String uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files, RedirectAttributes redirectAttributes) throws IOException {
        UploadMultipleFilesResponse response = fileService.uploadMultiple(files);
        redirectAttributes.addFlashAttribute("message", "Files uploaded successfully: " + response.files().size());
        return "redirect:/files/";
    }

    @GetMapping("/download")
    @ResponseBody
    public Resource downloadFile(@RequestParam String fileName) throws IOException {
        return fileService.downloadFile(fileName);
    }
}
