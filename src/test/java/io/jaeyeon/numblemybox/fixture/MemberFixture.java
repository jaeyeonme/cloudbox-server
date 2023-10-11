package io.jaeyeon.numblemybox.fixture;

import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.dto.MemberRegistration;

public class MemberFixture {

  public static final Long MEMBER_UNIQUE_ID = 1L;
  public static final String UNIQUE_MEMBER_EMAIL = "testUnique@gmail.com";
  public static final String DUPLICATED_MEMBER_EMAIL = "testDuplicated@gmail.com";

  public static final Member MEMBER1 = new Member("test@gmail.com", "Test1234!@#$");

  public static final Member MEMBER2 = new Member("test2@gmail.com", "Test12345!@#$");

  public static final MemberRegistration MEMBER_REGISTRATION_REQUEST =
      new MemberRegistration("test@gmail.com", "Test1234!@#$");
}
