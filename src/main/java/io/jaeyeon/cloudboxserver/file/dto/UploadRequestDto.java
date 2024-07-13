package io.jaeyeon.cloudboxserver.file.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadRequestDto(
    @NotBlank String fileName,
    @NotBlank String extension,
    @NotBlank String contentType,
    String folderPath) {}
