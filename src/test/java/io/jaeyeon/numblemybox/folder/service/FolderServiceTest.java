package io.jaeyeon.numblemybox.folder.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jaeyeon.numblemybox.common.FileUtility;
import io.jaeyeon.numblemybox.file.domain.entity.FileEntity;
import io.jaeyeon.numblemybox.fixture.MemberFixture;
import io.jaeyeon.numblemybox.folder.domain.entity.Folder;
import io.jaeyeon.numblemybox.folder.domain.repository.FolderRepository;
import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

  @InjectMocks private FolderServiceImpl folderService;

  @Mock private FolderRepository folderRepository;

  @Mock private FileUtility fileUtility;

  @Mock Resource resource;

  private Member member;
  private FileEntity fileEntity;
  private Folder folder;

  @BeforeEach
  void setUp() throws IOException {
    member = MemberFixture.MEMBER1;
    folder = mock(Folder.class);
    fileEntity = mock(FileEntity.class);

    when(folder.getId()).thenReturn(1L);
    when(folder.isOwnedBy(member)).thenReturn(true);
    when(folder.getFiles()).thenReturn(Collections.singletonList(fileEntity));
    when(fileEntity.getFileName()).thenReturn("testFile.txt");
    when(folderRepository.findById(1L)).thenReturn(Optional.of(folder));
    when(resource.getURI()).thenReturn(URI.create("some/path/1/1_testFile.txt.zip"));
    when(fileEntity.getPath()).thenReturn("some/file/path.txt");

    // `folderPath` 값으로 상대 경로로 설정
    ReflectionTestUtils.setField(folderService, "folderPath", "./path/to/storage");

    // fileUtility.newInputStream 메소드 호출 모킹
    InputStream mockInputStream = mock(InputStream.class);
    ZipOutputStream mockZipOutputStream = mock(ZipOutputStream.class);
    when(fileUtility.newInputStream(any(Path.class))).thenReturn(mockInputStream);
    when(fileUtility.createZipOutputStream(any())).thenReturn(mockZipOutputStream);
    // 기대하는 경로를 절대 경로로 변경
    String expectedPath =
        "file:" + Paths.get("./path/to/storage/1/1_testFile.txt.zip").toAbsolutePath().normalize();
    when(resource.getURI()).thenReturn(URI.create(expectedPath));
    when(mockInputStream.read(any())).thenReturn(-1);
  }

  @Test
  @DisplayName("폴더 다운로드 성공")
  void shouldDownloadFolderAsZipSuccessfully() throws Exception {
    // Given
    Long folderId = 1L;

    // When
    Resource resultResource = folderService.downloadFolderAsZip(folderId, member);

    // Then
    assertThat(resultResource).isNotNull();
    assertThat(resultResource.getURI().toString()).isEqualTo(resource.getURI().toString());
  }
}
