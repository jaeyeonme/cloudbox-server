package io.jaeyeon.cloudboxserver.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DownloadRequestDto(
        @NotBlank @Size(max = 255) String fileName) { }
