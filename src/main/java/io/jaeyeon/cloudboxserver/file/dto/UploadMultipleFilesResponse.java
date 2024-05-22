package io.jaeyeon.cloudboxserver.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UploadMultipleFilesResponse(
    @NotNull @Schema(description = "업로드된 파일 목록") List<UploadFileResponse> files,
    @Min(0) @Schema(description = "총 파일 크기", example = "2048") long totalSize) {}
