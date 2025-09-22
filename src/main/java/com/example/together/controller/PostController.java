package com.example.together.controller;

import com.example.together.domain.PostType;
import com.example.together.dto.comment.CommentCreateRequestDTO;
import com.example.together.dto.comment.CommentResponseDTO;
import com.example.together.dto.comment.CommentUpdateRequestDTO;
import com.example.together.dto.post.PostCreateRequestDTO;
import com.example.together.dto.post.PostResponseDTO;
import com.example.together.service.UserService;
import com.example.together.service.cafe.CafeService;
import com.example.together.service.comment.CommentService;
import com.example.together.service.post.PostService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final UserService userService;
    private final CafeService cafeService;

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }
        String userIdString = principal.getName();
        // userIdString으로 User를 찾고, 해당 User의 고유 ID(Long)를 반환
        return userService.findByUserId(userIdString).getId();
    }

    @GetMapping("/posts")
    public String getPostsByCafe(@PathVariable Long cafeId, Model model, Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);
        List<PostResponseDTO> posts = postService.getPostsByCafe(cafeId, userId);
        String cafeName = cafeService.getCafeNameById(cafeId);
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("posts", posts);
        model.addAttribute("cafeName", cafeName);
        return "post/list";
    }

    @GetMapping("/posts/register")
    public String showCreateForm(@PathVariable Long cafeId, Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);

        boolean isOwner = cafeService.isCafeOwner(cafeId, userId);

        model.addAttribute("cafeId", cafeId);
        model.addAttribute("postCreateRequestDTO", new PostCreateRequestDTO());
        model.addAttribute("isOwner", isOwner);

        return "post/register";
    }

    @PostMapping("/posts/register")
    public String createPost(@PathVariable Long cafeId,
                             @ModelAttribute PostCreateRequestDTO requestDTO,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        Long userId = getUserIdFromPrincipal(principal);
        requestDTO.setCafeId(cafeId);
        if (requestDTO.getPostType() == PostType.NOTICE) {
            if (!cafeService.isCafeOwner(cafeId, userId)) {
                redirectAttributes.addFlashAttribute("error", "공지사항을 작성할 권한이 없습니다.");
                return "redirect:/cafe/" + cafeId + "/posts";
            }
        }

        try {
            postService.createPost(requestDTO, userId, imageFile);
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 작성되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId + "/posts";
    }

    @GetMapping("/posts/{postId}")
    public String getPostDetail(@PathVariable Long cafeId, @PathVariable Long postId,
                                Model model, Principal principal,
                                HttpServletRequest request, HttpServletResponse response) {
        try {
            boolean isLoggedIn = (principal != null);

            Long userId = null;
            if (isLoggedIn) {
                userId = getUserIdFromPrincipal(principal);
            }

            handleViewCount(postId, request, response);


            PostResponseDTO post = postService.getPostById(postId, userId);
            List<CommentResponseDTO> comments = commentService.getCommentsByPost(postId, userId);

            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("commentCreateRequestDTO", new CommentCreateRequestDTO());
            model.addAttribute("cafeId", cafeId);
            model.addAttribute("isLoggedIn", isLoggedIn);
            model.addAttribute("loggedInUserId", userId);

            return "post/detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    private void handleViewCount(Long postId, HttpServletRequest request, HttpServletResponse response) {
        String postIdString = String.valueOf(postId);
        Cookie[] cookies = request.getCookies();
        Cookie postViewCookie = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("postView".equals(cookie.getName())) {
                    postViewCookie = cookie;
                    break;
                }
            }
        }

        if (postViewCookie != null) {
            if (!postViewCookie.getValue().contains("[" + postIdString + "]")) {
                postService.increaseViewCount(postId);
                postViewCookie.setValue(postViewCookie.getValue() + "[" + postIdString + "]");
                postViewCookie.setPath("/");
                postViewCookie.setMaxAge(60 * 60 * 24); // 24시간
                response.addCookie(postViewCookie);
            }
        } else {
            postService.increaseViewCount(postId);
            Cookie newCookie = new Cookie("postView", "[" + postIdString + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24); // 24시간
            response.addCookie(newCookie);
        }
    }


    @GetMapping("/posts/{postId}/edit")
    public String showEditForm(@PathVariable Long cafeId, @PathVariable Long postId, Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
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
        Long userId = getUserIdFromPrincipal(principal);
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
        Long userId = getUserIdFromPrincipal(principal);
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
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 후 댓글을 작성할 수 있습니다.");
            return "redirect:/cafe/" + cafeId + "/posts/" + postId;
        }

        Long userId = getUserIdFromPrincipal(principal);
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
        Long userId = getUserIdFromPrincipal(principal);
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
        Long userId = getUserIdFromPrincipal(principal);
        try {
            commentService.updateComment(commentId, requestDTO.getContent(), userId);
            return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}