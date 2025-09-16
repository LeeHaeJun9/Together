package com.example.together.controller;

import com.example.together.dto.comment.CommentCreateRequestDTO;
import com.example.together.dto.comment.CommentResponseDTO;
import com.example.together.dto.comment.CommentUpdateRequestDTO;
import com.example.together.dto.post.PostCreateRequestDTO;
import com.example.together.dto.post.PostResponseDTO;
import com.example.together.service.comment.CommentService;
import com.example.together.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cafe/{cafeId}")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    private Long getLoggedInUserId(Principal principal) {
        // 실제 구현에서는 Principal.getName()을 사용해 DB에서 userId를 찾습니다.
        return 1L;
    }

    @GetMapping("/posts")
    public String getPostsByCafe(@PathVariable Long cafeId, Model model, Principal principal) {

        Long userId = getLoggedInUserId(principal);
        List<PostResponseDTO> posts = postService.getPostsByCafe(cafeId, userId);
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/posts/create")
    public String showCreateForm(@PathVariable Long cafeId, Model model) {
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("postCreateRequestDTO", new PostCreateRequestDTO());
        return "post/create";
    }

    @PostMapping("/posts/create")
    public String createPost(@PathVariable Long cafeId,
                             @ModelAttribute PostCreateRequestDTO requestDTO,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        Long userId = getLoggedInUserId(principal);
        requestDTO.setCafeId(cafeId);
        try {
            postService.createPost(requestDTO, userId, imageFile);
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 작성되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId + "/posts";
    }

    @GetMapping("/posts/{postId}") // ✅ 게시글 상세 조회 메서드 (하나만 남김)
    public String getPostDetail(@PathVariable Long cafeId, @PathVariable Long postId, Model model, Principal principal) {
        Long userId = getLoggedInUserId(principal);
        try {
            PostResponseDTO post = postService.getPostById(postId, userId);
            List<CommentResponseDTO> comments = commentService.getCommentsByPost(postId, userId);
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("commentCreateRequestDTO", new CommentCreateRequestDTO());
            model.addAttribute("cafeId", cafeId);
            return "post/detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    @GetMapping("/posts/{postId}/edit")
    public String showEditForm(@PathVariable Long cafeId, @PathVariable Long postId, Model model, Principal principal) {
        Long userId = getLoggedInUserId(principal);
        try {
            PostResponseDTO post = postService.getPostById(postId, userId);
            if (!post.isOwner()) {
                model.addAttribute("error", "수정 권한이 없습니다.");
                return "error/accessDenied";
            }
            model.addAttribute("post", post);
            model.addAttribute("postCreateRequestDTO", new PostCreateRequestDTO());
            return "post/edit";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/posts/{postId}/update")
    public String updatePost(@PathVariable Long cafeId, @PathVariable Long postId,
                             @ModelAttribute PostCreateRequestDTO requestDTO,
                             @RequestParam(value = "newImage", required = false) MultipartFile newImage,
                             Principal principal, RedirectAttributes redirectAttributes) {
        Long userId = getLoggedInUserId(principal);
        try {
            postService.updatePost(postId, requestDTO, newImage, userId);
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 수정되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId + "/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Long cafeId, @PathVariable Long postId,
                             Principal principal, RedirectAttributes redirectAttributes) {
        Long userId = getLoggedInUserId(principal);
        try {
            postService.deletePost(postId, userId);
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 삭제되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId + "/posts";
    }

    @PostMapping("/posts/{postId}/comments")
    public String createComment(@PathVariable Long cafeId, @PathVariable Long postId,
                                @ModelAttribute CommentCreateRequestDTO requestDTO,
                                Principal principal, RedirectAttributes redirectAttributes) {
        Long userId = getLoggedInUserId(principal);
        requestDTO.setPostId(postId);
        try {
            commentService.createComment(requestDTO, userId);
            redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 작성되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId + "/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long cafeId, @PathVariable Long postId,
                                @PathVariable Long commentId, Principal principal,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoggedInUserId(principal);
        try {
            commentService.deleteComment(commentId, userId);
            redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 삭제되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId + "/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/update")
    @ResponseBody
    public ResponseEntity<String> updateComment(@PathVariable Long commentId,
                                                @RequestBody CommentUpdateRequestDTO requestDTO,
                                                Principal principal) {
        Long userId = getLoggedInUserId(principal);
        try {
            commentService.updateComment(commentId, requestDTO.getContent(), userId);
            return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}