package io.jaeyeon.cloudboxserver.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadRequestDto(
    @NotBlank @Size(max = 255) String fileName,
    @NotBlank @Size(max = 10) String extension,
    @NotBlank @Size(max = 50) String contentType) {}
