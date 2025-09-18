package com.example.together.service.post;

import com.example.together.domain.PostType;
import com.example.together.dto.post.PostCreateRequestDTO;
import com.example.together.dto.post.PostResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    PostResponseDTO createPost(PostCreateRequestDTO requestDTO, Long userId, MultipartFile imageFile);
    List<PostResponseDTO> getPostsByCafe(Long cafeId, Long userId);
    List<PostResponseDTO> getPostsByCafeAndType(Long cafeId, PostType postType, Long userId);
    PostResponseDTO getPostById(Long postId, Long userId);
    void updatePost(Long postId, PostCreateRequestDTO requestDTO, MultipartFile newImage, Long userId);
    void deletePost(Long postId, Long userId);
    void increaseViewCount(Long postId);
    List<PostResponseDTO> getLatestNotices(Long cafeId, Long userId);
    List<PostResponseDTO> getPopularPosts(Long cafeId, int limit, Long userId);
}
