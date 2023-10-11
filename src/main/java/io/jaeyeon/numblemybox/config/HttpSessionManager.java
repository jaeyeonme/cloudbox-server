package io.jaeyeon.numblemybox.config;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HttpSessionManager implements SessionManager {

  private final HttpSession httpSession;

  @Override
  public void setAttribute(String name, Object value) {
    httpSession.setAttribute(name, value);
  }

  @Override
  public Object getAttribute(String name) {
    return httpSession.getAttribute(name);
  }

  @Override
  public void removeAttribute(String name) {
    httpSession.removeAttribute(name);
  }
}
