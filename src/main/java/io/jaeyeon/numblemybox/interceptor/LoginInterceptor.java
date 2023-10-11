package io.jaeyeon.numblemybox.interceptor;

import io.jaeyeon.numblemybox.annotation.AuthenticationRequired;
import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.UnAuthenticatedAccessException;
import io.jaeyeon.numblemybox.member.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

  private final LoginService loginService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (handler instanceof HandlerMethod
        && ((HandlerMethod) handler).hasMethodAnnotation(AuthenticationRequired.class)) {
      Long memberId = loginService.getLoginMemberId();

      if (memberId == null) {
        throw new UnAuthenticatedAccessException(ErrorCode.UNAUTHENTICATED_ACCESS);
      }
    }

    return true;
  }
}
