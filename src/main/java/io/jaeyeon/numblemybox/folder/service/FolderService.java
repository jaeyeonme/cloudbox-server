package io.jaeyeon.numblemybox.folder.service;

import io.jaeyeon.numblemybox.member.domain.entity.Member;
import java.io.IOException;
import org.springframework.core.io.Resource;

public interface FolderService {
  Resource downloadFolderAsZip(Long folderId, Member member) throws IOException;
}
