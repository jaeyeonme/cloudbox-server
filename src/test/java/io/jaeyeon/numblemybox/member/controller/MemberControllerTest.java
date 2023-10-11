package io.jaeyeon.numblemybox.member.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jaeyeon.numblemybox.fixture.MemberFixture;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import io.jaeyeon.numblemybox.member.dto.MemberRegistration;
import io.jaeyeon.numblemybox.member.service.LoginService;
import io.jaeyeon.numblemybox.member.service.MemberService;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

	@MockBean
	private MemberService memberService;

	@MockBean
	private LoginService loginService;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp(WebApplicationContext applicationContext) {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("회원 가입 요청 처리")
	void registration() throws Exception {
		MemberRegistration dto = MemberFixture.MEMBER_REGISTRATION_REQUEST;
		String content = objectMapper.writeValueAsString(dto);

		mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(content))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("이메일 중복 체크")
	void isDuplicatedEmail() throws Exception {
	    String email = MemberFixture.UNIQUE_MEMBER_EMAIL;
		when(memberService.isDuplicatedEmail(email)).thenReturn(false);

		mockMvc.perform(get("/api/members/duplicated/{email}", email))
				.andExpect(status().isOk());

	}

	@Test
	@DisplayName("로그인 요청 처리")
	void loginTest() throws Exception {
		MemberRegistration dto = MemberFixture.MEMBER_REGISTRATION_REQUEST;
		String content = objectMapper.writeValueAsString(dto);

		when(memberService.isValidMember(dto, passwordEncoder)).thenReturn(true);
		when(memberService.findMemberByEmail(dto.email())).thenReturn(mock(Member.class));

		mockMvc.perform(post("/api/members/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(content))
				.andExpect(status().isOk());
	}
}
