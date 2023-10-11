package io.jaeyeon.numblemybox.config;

public interface SessionManager {

  void setAttribute(String name, Object value);

  Object getAttribute(String name);

  void removeAttribute(String name);
}
