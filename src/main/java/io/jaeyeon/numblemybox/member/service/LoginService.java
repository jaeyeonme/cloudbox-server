package io.jaeyeon.numblemybox.member.service;

import io.jaeyeon.numblemybox.member.domain.entity.Member;

public interface LoginService {

  void login(long id);

  void logout();

  Member getLoginMember();

  Long getLoginMemberId();
}
