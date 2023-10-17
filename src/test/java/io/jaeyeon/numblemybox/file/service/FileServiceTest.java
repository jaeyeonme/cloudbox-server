package io.jaeyeon.numblemybox.file.service;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.util.AssertionErrors.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import io.jaeyeon.numblemybox.file.domain.repository.FileEntityRepository;
import io.jaeyeon.numblemybox.file.dto.UploadFileResponse;
import io.jaeyeon.numblemybox.fixture.MemberFixture;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

	@InjectMocks private FileLocalServiceImpl fileLocalService;

	@Mock
	FileEntityRepository fileEntityRepository;

	@Mock
	private MultipartFile multipartFile;

	@Mock
	private ResourceLoader resourceLoader;

	private FileEntity fileEntity;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(fileLocalService, "folderPath", "/path/to/file");

		// MultipartFile 모킹
		lenient().when(multipartFile.getOriginalFilename()).thenReturn("testFile.txt");
		lenient().when(multipartFile.getContentType()).thenReturn("text/plain");
		lenient().when(multipartFile.getSize()).thenReturn(100L);

		fileEntity = FileEntity.builder()
				.fileName("testFile.txt")
				.fileType("text/plain")
				.size(100L)
				.path("/path/to/file")
				.owner(MemberFixture.MEMBER1)
				.build();
	}

	@Test
	@DisplayName("파일 업로드 테스트")
	void testUpload() throws Exception {
	    // given
		given(fileEntityRepository.save(any())).willReturn(fileEntity);

	    // when
		UploadFileResponse uploadFileResponse = fileLocalService.upload(multipartFile, MemberFixture.MEMBER1);

		// then
		assertEquals("testFile.txt", uploadFileResponse.fileName(), "testFile.txt");
		assertEquals("/path/to/file/testFile.txt", uploadFileResponse.fileDownloadUri(), "/path/to/file/testFile.txt");
		assertEquals("text/plain", uploadFileResponse.fileType(), "text/plain");

	}

	@Test
	@DisplayName("파일 다운로드 테스트")
	void testDownload() throws Exception {
		// given
		String encodedFileName = URLEncoder.encode(fileEntity.getFileName(), StandardCharsets.UTF_8);
		given(fileEntityRepository.findByFileName(anyString())).willReturn(Optional.of(fileEntity));

		UrlResource mockedResource = mock(UrlResource.class);  // UrlResource 모킹
		given(mockedResource.exists()).willReturn(true);
		given(mockedResource.getFilename()).willReturn(encodedFileName);
		given(resourceLoader.getResource(anyString())).willReturn(mockedResource);  // getResource 메서드 호출 시 모킹된 UrlResource 객체를 반환하도록 설정

		// when
		Resource resource = fileLocalService.downloadFile(encodedFileName, MemberFixture.MEMBER1);

		// then
		assertThat(resource, is(notNullValue()));
		assertEquals(encodedFileName, resource.getFilename(), "testFile.txt");
	}

}
