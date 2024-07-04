package io.jaeyeon.cloudboxserver.file.dto;

import io.jaeyeon.cloudboxserver.file.domain.entity.FileEntity;
import java.util.List;

public record FileListResponseDto(
    List<FileEntity> files, String nextContinuationToken, boolean isTruncated) {
  public FileListResponseDto {
    files = List.copyOf(files);
  }

  public static FileListResponseDto of(
      List<FileEntity> files, String nextContinuationToken, boolean isTruncated) {
    return new FileListResponseDto(files, nextContinuationToken, isTruncated);
  }

  public boolean hasNextPage() {
    return isTruncated && nextContinuationToken != null && !nextContinuationToken.isEmpty();
  }
}
