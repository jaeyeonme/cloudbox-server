package io.jaeyeon.cloudboxserver.common;

import java.util.UUID;

public class UUIDUtils {

  public static String getUUIDv4() {
    return UUID.randomUUID().toString();
  }
}
