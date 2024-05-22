package io.jaeyeon.cloudboxserver.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadFileResponse(
    @NotBlank @Size(max = 255) @Schema(description = "파일명", example = "test.jpg") String fileName,
    @NotBlank @Schema(description = "파일 다운로드 URI", example = "http://localhost:8080/api/v1/files/1")
        String fileDownloadUri,
    @NotBlank @Schema(description = "파일 타입", example = "image/jpeg") String fileType,
    @Min(0) @Schema(description = "파일 크기", example = "1024") long size) {}
