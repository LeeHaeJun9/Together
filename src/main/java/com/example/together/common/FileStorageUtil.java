package com.example.together.common;

import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileStorageUtil {

  private FileStorageUtil(){}

  /** 업로드 루트 (application.yml의 org.zerock.upload.path와 동일) */
  public static Path uploadRoot(String uploadPath){
    return Paths.get(uploadPath).toAbsolutePath().normalize();
  }

  public static Path toPhysicalPath(String imageUrl, String urlPrefix, String uploadPath){
    if (!StringUtils.hasText(imageUrl)) return null;

    if (imageUrl.matches("^[a-zA-Z]:\\\\.*")) {
      Path p = Paths.get(imageUrl).normalize();
      if (!p.startsWith(uploadRoot(uploadPath))) {
        throw new IllegalArgumentException("Invalid absolute path outside upload root: " + imageUrl);
      }
      return p;
    }

    String prefix = urlPrefix.endsWith("/") ? urlPrefix : (urlPrefix + "/");
    String rest = imageUrl.startsWith(prefix) ? imageUrl.substring(prefix.length()) : imageUrl;

    Path p = uploadRoot(uploadPath).resolve(rest.replace('/', '\\')).normalize();
    if (!p.startsWith(uploadRoot(uploadPath))) {
      throw new IllegalArgumentException("Invalid path (traversal) detected: " + imageUrl);
    }
    return p;
  }

  /** 커밋 후 실제 파일 삭제 시 사용 */
  public static boolean deleteQuietly(Path p){
    try{
      return p != null && Files.deleteIfExists(p);
    }catch(Exception ignore){
      return false;
    }
  }
}
