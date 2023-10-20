package io.jaeyeon.numblemybox.file.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import io.jaeyeon.numblemybox.common.FileUtility;
import io.jaeyeon.numblemybox.common.UUIDUtils;
import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import io.jaeyeon.numblemybox.file.domain.repository.FileEntityRepository;
import io.jaeyeon.numblemybox.file.dto.UploadFileResponse;
import io.jaeyeon.numblemybox.fixture.MemberFixture;
import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.folder.domain.repository.FolderRepository;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @InjectMocks private FileLocalServiceImpl fileLocalService;

  @Mock private FileEntityRepository fileEntityRepository;

  @Mock private FolderRepository folderRepository;

  @Mock private UUIDUtils uuidUtils;

  @Mock private FileUtility fileUtility;

  @Mock private MultipartFile multipartFile;

  private FileEntity fileEntity;
  private Folder folder;

  @BeforeEach
  void setUp() throws IOException {
    // MultipartFile 모킹
    lenient().when(multipartFile.getOriginalFilename()).thenReturn("testFile.txt");
    lenient().when(multipartFile.getContentType()).thenReturn("text/plain");
    lenient().when(multipartFile.getSize()).thenReturn(100L);
    lenient().when(fileUtility.exists(any())).thenReturn(true);
    lenient().when(fileUtility.probeContentType(any())).thenReturn("text/plain");
    ReflectionTestUtils.setField(fileLocalService, "folderPath", "./path/to/storage");

    folder =
        Folder.builder().name("ROOT").path("/path/to/file").owner(MemberFixture.MEMBER1).build();
    // findByNameAndOwner 메서드가 가짜 루트 폴더 객체를 반환하도록 설정
    lenient()
        .when(folderRepository.findByNameAndOwner(anyString(), any()))
        .thenReturn(Optional.of(folder));

    fileEntity =
        FileEntity.builder()
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
    String uuid = "random-uuid";
    lenient().when(uuidUtils.getUUID()).thenReturn(uuid);
    lenient().when(multipartFile.getOriginalFilename()).thenReturn("testFile.txt");
    lenient().when(multipartFile.getContentType()).thenReturn("text/plain");
    lenient().when(multipartFile.getSize()).thenReturn(100L);
    lenient()
        .when(fileUtility.createFilePath(anyString(), anyString()))
        .thenReturn("/some/mock/path.txt");
    // 실제 파일 저장을 피하기 위한 모킹
    doNothing().when(multipartFile).transferTo(any(File.class));

    // when
    UploadFileResponse response =
        fileLocalService.upload(multipartFile, null, MemberFixture.MEMBER1);

    // then
    assertThat(response.fileName()).isEqualTo("testFile.txt");
    assertThat(response.fileDownloadUri()).isEqualTo("/some/mock/path.txt");
    assertThat(response.fileType()).isEqualTo("text/plain");
    assertThat(response.size()).isEqualTo(100L);
  }

  @Test
  @DisplayName("파일 다운로드 테스트")
  void testDownload() throws Exception {
    // given
    lenient()
        .when(fileEntityRepository.findByFileName(anyString()))
        .thenReturn(Optional.of(fileEntity));

    // when
    Resource resource = fileLocalService.downloadFile("testFile.txt", MemberFixture.MEMBER1);

    // then
    assertThat(resource).isNotNull();
  }
}
