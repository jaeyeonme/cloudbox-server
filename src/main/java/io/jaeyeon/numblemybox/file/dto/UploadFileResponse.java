package io.jaeyeon.numblemybox.file.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadFileResponse(
    @NotBlank @Size(max = 255) String fileName,
    @NotBlank String fileDownloadUri,
    @NotBlank String fileType,
    @Min(0) long size) {}
