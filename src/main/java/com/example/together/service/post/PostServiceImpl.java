package com.example.together.service.post;

import com.example.together.domain.*;
import com.example.together.dto.post.PostCreateRequestDTO;
import com.example.together.dto.post.PostResponseDTO;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.DemandSurveyRepository;
import com.example.together.repository.PostRepository;
import com.example.together.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CafeRepository cafeRepository;
    private final UserRepository userRepository;
    private final DemandSurveyRepository demandSurveyRepository;

    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    @Transactional
    @Override
    public PostResponseDTO createPost(PostCreateRequestDTO requestDTO, Long userId, MultipartFile imageFile) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
        Cafe cafe = cafeRepository.findById(requestDTO.getCafeId())
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                File directory = new File(uploadPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                File destinationFile = new File(uploadPath, fileName);
                imageFile.transferTo(destinationFile);
                imageUrl = "/upload/" + fileName;

            } catch (IOException e) {
                throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
            }
        }

        Post post = Post.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .image(imageUrl)
                .viewCount(0)
                .postType(requestDTO.getPostType())
                .pinned(requestDTO.isPinned())
                .author(author)
                .cafe(cafe)
                .build();

        Post savedPost = postRepository.save(post);

        // ✅ 수정된 부분: 게시글 유형이 DEMAND일 때만 수요조사를 생성합니다.
        if (requestDTO.getPostType() == PostType.DEMAND && requestDTO.getDemandSurvey() != null) {
            DemandSurvey demandSurvey = DemandSurvey.builder()
                    .title(requestDTO.getDemandSurvey().getTitle())
                    .content(requestDTO.getDemandSurvey().getContent())
                    .deadline(requestDTO.getDemandSurvey().getDeadline())
                    .voteType(requestDTO.getDemandSurvey().getVoteType())
                    .post(savedPost)
                    .author(author)
                    .build();
            demandSurveyRepository.save(demandSurvey);
        }

        return new PostResponseDTO(savedPost, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getPostsByCafe(Long cafeId, Long userId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        List<Post> posts = postRepository.findByCafeOrderByPinnedDescRegDateDesc(cafe);

        AtomicInteger originalIndex = new AtomicInteger(1);

        return posts.stream()
                .map(post -> {
                    PostResponseDTO dto = new PostResponseDTO(post, userId);
                    boolean isPinned = post.isPinned(); // pinned 상태 변수에 저장
                    int currentOriginalIndex = 0; // 초기화

                    if (!isPinned) {
                        currentOriginalIndex = originalIndex.getAndIncrement();
                        dto.setOriginalIndex(currentOriginalIndex);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostsByCafeAndType(Long cafeId, PostType postType, Long userId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));
        List<Post> posts = postRepository.findByCafeAndPostType(cafe, postType);
        return posts.stream()
                .map(post -> new PostResponseDTO(post, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PostResponseDTO getPostById(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
//        post.setViewCount(post.getViewCount() + 1);
//        postRepository.save(post);
        return new PostResponseDTO(post, userId);
    }

    @Transactional
    @Override
    public void updatePost(Long postId, PostCreateRequestDTO requestDTO, MultipartFile newImage, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("게시글을 수정할 권한이 없습니다.");
        }

        String newImageUrl = post.getImage(); // 기존 이미지 URL을 유지
        if (newImage != null && !newImage.isEmpty()) {
            try {
                // 기존 이미지 삭제 (선택 사항: 서버 공간 절약)
                if (post.getImage() != null) {
                    String oldImageName = post.getImage().substring("/upload/".length());
                    File oldFile = new File(uploadPath, oldImageName);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // 새 이미지 저장
                String fileName = UUID.randomUUID().toString() + "_" + newImage.getOriginalFilename();
                File destinationFile = new File(uploadPath, fileName);
                newImage.transferTo(destinationFile);
                newImageUrl = "/upload/" + fileName;
            } catch (IOException e) {
                throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
            }
        }

        post.setTitle(requestDTO.getTitle());
        post.setContent(requestDTO.getContent());
        post.setImage(newImageUrl); // 이미지 URL 업데이트
        post.setPostType(requestDTO.getPostType());

        if (requestDTO.getPostType() == PostType.NOTICE) {
            if (requestDTO.getDemandSurvey() != null) {
                DemandSurvey existingSurvey = post.getDemandSurvey();
                if (existingSurvey != null) {
                    // 기존 수요조사가 있으면 업데이트
                    existingSurvey.setTitle(requestDTO.getDemandSurvey().getTitle());
                    existingSurvey.setContent(requestDTO.getDemandSurvey().getContent());
                    existingSurvey.setDeadline(requestDTO.getDemandSurvey().getDeadline());
                    existingSurvey.setVoteType(requestDTO.getDemandSurvey().getVoteType());
                } else {
                    // 기존 수요조사가 없으면 새로 생성
                    DemandSurvey newSurvey = DemandSurvey.builder()
                            .title(requestDTO.getDemandSurvey().getTitle())
                            .content(requestDTO.getDemandSurvey().getContent())
                            .deadline(requestDTO.getDemandSurvey().getDeadline())
                            .voteType(requestDTO.getDemandSurvey().getVoteType())
                            .post(post)
                            .author(post.getAuthor())
                            .build();
                    demandSurveyRepository.save(newSurvey);
                    post.setDemandSurvey(newSurvey);
                }
            } else {
                // 기존 수요조사 데이터 삭제
                if (post.getDemandSurvey() != null) {
                    demandSurveyRepository.delete(post.getDemandSurvey());
                    post.setDemandSurvey(null);
                }
            }
        } else {
            // 게시글 유형이 GENERAL로 변경되었을 경우, 기존 수요조사 삭제
            if (post.getDemandSurvey() != null) {
                demandSurveyRepository.delete(post.getDemandSurvey());
                post.setDemandSurvey(null);
            }
        }

        postRepository.save(post);
    }

    @Transactional
    @Override
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("게시글을 삭제할 권한이 없습니다.");
        }

        if (post.getImage() != null) {
            String imageName = post.getImage().substring("/upload/".length());
            File imageFile = new File(uploadPath, imageName);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }

        // ✅ 3. 게시글 삭제
        postRepository.delete(post);
    }

    @Transactional
    @Override
    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    @Override
    public List<PostResponseDTO> getLatestNotices(Long cafeId, Long userId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));
        List<Post> notices = postRepository.findTop5ByCafeAndPostTypeOrderByRegDateDesc(cafe, PostType.NOTICE);
        return notices.stream()
                .map(post -> new PostResponseDTO(post, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPopularPosts(Long cafeId, int limit, Long userId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"));

        List<Post> posts = postRepository.findByCafeOrderByViewCountDesc(cafe, pageable);

        return posts.stream()
                .map(post -> new PostResponseDTO(post, userId))
                .collect(Collectors.toList());
    }
}