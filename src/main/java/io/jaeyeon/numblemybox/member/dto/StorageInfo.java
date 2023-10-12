package io.jaeyeon.numblemybox.member.dto;

public record StorageInfo(double allocatedSpace, double usedSpace) {
  public StorageInfo(long allocatedSpaceBytes, long usedSpaceBytes) {
    this(
        (double) allocatedSpaceBytes / 1024 / 1024 / 1024,
        (double) usedSpaceBytes / 1024 / 1024 / 1024);
  }
}
