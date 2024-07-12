package io.jaeyeon.cloudboxserver.file.dto;

public record UploadResponseDto(
    String presignedUrl, String fileUrl, String finalFileUrl, String folderPath) {}
