package io.jaeyeon.numblemybox.member;

import static io.jaeyeon.numblemybox.member.service.SessionLoginService.*;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.service.GeneralMemberService;
import io.jaeyeon.numblemybox.member.service.SessionLoginService;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

	private SessionLoginService loginService;

	@Mock
	private GeneralMemberService memberService;

	private MockHttpSession mockHttpSession;

	private Member member;

	@BeforeEach
	void setUp() {

		member = Member.builder()
				.email("test@gmail.com")
				.password("Password123!")
				.build();

		mockHttpSession = new MockHttpSession();
		loginService = new SessionLoginService(mockHttpSession, memberService);
	}

	@Test
	@DisplayName("로그인 테스트")
	void loginTest() throws Exception {
	    // when
		loginService.login(1L);

	    // then
		Assertions.assertThat(mockHttpSession.getAttribute(MEMBER_ID)).isNotNull();
		Assertions.assertThat(mockHttpSession.getAttribute(MEMBER_ID)).isEqualTo(1L);
	}

	@Test
	@DisplayName("로그아웃 테스트")
	void logoutTest() throws Exception {
	    // given
		mockHttpSession.setAttribute(MEMBER_ID, 1L);

	    // when
		loginService.logout();

	    // then
		Assertions.assertThat(mockHttpSession.getAttribute(MEMBER_ID)).isNull();
	}

	@Test
	@DisplayName("로그안된 회원 정보 가져오기")
	void getLoginMemberTest() throws Exception {
	    // given
		mockHttpSession.setAttribute(MEMBER_ID, 1L);
		when(memberService.findMemberById(1L)).thenReturn(member);

	    // when
		Member loginMember = loginService.getLoginMember();

		// then
		Assertions.assertThat(loginMember).isNotNull();
	}

	@Test
	@DisplayName("로그인된 회원 ID 가져오기")
	void getLoginMemberIdTest() throws Exception {
	    // given
		mockHttpSession.setAttribute(MEMBER_ID, 1L);

	    // when
		Long loginMemberId = loginService.getLoginMemberId();

		// then
		Assertions.assertThat(loginMemberId).isNotNull();
		Assertions.assertThat(loginMemberId).isEqualTo(1L);
	}
}
