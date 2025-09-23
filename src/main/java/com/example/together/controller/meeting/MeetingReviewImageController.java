package com.example.together.controller.meeting;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

@RestController
@Log4j2
@RequiredArgsConstructor
public class MeetingReviewImageController {

    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    @GetMapping(value = "/upload/display", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> display(String fileName) {
        ResponseEntity<byte[]> result = null;

        try {
            File file = new File(uploadPath, fileName);
            HttpHeaders headers = new HttpHeaders();

            headers.add("Content-Type", Files.probeContentType(file.toPath()));
            byte[] fileBytes = FileCopyUtils.copyToByteArray(file);
            result = new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("파일 로드 중 오류 발생: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    @GetMapping(value = "/upload/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(String fileName) {
        try {
            File file = new File(uploadPath, fileName);

            HttpHeaders headers = new HttpHeaders();
            String originalFileName = fileName.substring(fileName.indexOf("_") + 1);
            headers.add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(originalFileName, "UTF-8"));

            byte[] fileBytes = FileCopyUtils.copyToByteArray(file);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("파일 다운로드 중 오류 발생: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}