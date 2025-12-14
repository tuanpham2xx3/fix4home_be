package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.article.*;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.enums.ArticleStatus;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.NewsArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "News Article Controller", description = "APIs for managing news articles")
public class NewsArticleController {

    private final NewsArticleService articleService;

    // ==================== ADMIN ENDPOINTS ====================

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Create new article", 
               description = "Admin creates a new article (default status: DRAFT)")
    public ResponseEntity<ApiResponse<ArticleDTO>> createArticle(
            @Valid @RequestBody CreateArticleRequest request) {
        log.info("Creating article with title: {}", request.getTitle());
        
        ArticleDTO article = articleService.createArticle(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", article));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Update article", 
               description = "Admin updates article details (all fields optional)")
    public ResponseEntity<ApiResponse<ArticleDTO>> updateArticle(
            @Parameter(description = "Article ID") @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest request) {
        log.info("Updating article with ID: {}", id);
        
        ArticleDTO article = articleService.updateArticle(id, request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Article updated successfully", article));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Publish article", 
               description = "Admin publishes an article (changes status to PUBLISHED)")
    public ResponseEntity<ApiResponse<ArticleDTO>> publishArticle(
            @Parameter(description = "Article ID") @PathVariable Long id) {
        log.info("Publishing article with ID: {}", id);
        
        ArticleDTO article = articleService.publishArticle(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Article published successfully", article));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Unpublish article", 
               description = "Admin unpublishes an article (changes status to UNPUBLISHED)")
    public ResponseEntity<ApiResponse<ArticleDTO>> unpublishArticle(
            @Parameter(description = "Article ID") @PathVariable Long id) {
        log.info("Unpublishing article with ID: {}", id);
        
        ArticleDTO article = articleService.unpublishArticle(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Article unpublished successfully", article));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Delete article", 
               description = "Admin deletes an article permanently")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @Parameter(description = "Article ID") @PathVariable Long id) {
        log.info("Deleting article with ID: {}", id);
        
        articleService.deleteArticle(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Article deleted successfully", null));
    }

    @GetMapping("/admin")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all articles (Admin)", 
               description = "Admin retrieves all articles with optional status filter and pagination")
    public ResponseEntity<ApiResponse<ArticleListResponseDTO>> getArticles(
            @Parameter(description = "Filter by status (DRAFT, PUBLISHED, UNPUBLISHED)") 
            @RequestParam(required = false) ArticleStatus status,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (limit)") 
            @RequestParam(required = false) Integer limit) {
        log.info("Admin fetching articles - status: {}, page: {}, limit: {}", status, page, limit);
        
        ArticleListResponseDTO articles = articleService.getArticles(status, page, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Articles retrieved successfully", articles));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get article details (Admin)", 
               description = "Admin retrieves article details including DRAFT articles")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleByIdAdmin(
            @Parameter(description = "Article ID") @PathVariable Long id) {
        log.info("Admin fetching article details for ID: {}", id);
        
        ArticleDTO article = articleService.getArticleById(id, true);
        
        return ResponseEntity.ok(
                ApiResponse.success("Article details retrieved successfully", article));
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping
    @Operation(summary = "Get published articles", 
               description = "Public endpoint to retrieve published articles with pagination")
    public ResponseEntity<ApiResponse<ArticleListResponseDTO>> getPublishedArticles(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (limit)") 
            @RequestParam(required = false) Integer limit) {
        log.info("Fetching published articles - page: {}, limit: {}", page, limit);
        
        ArticleListResponseDTO articles = articleService.getPublishedArticles(page, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Published articles retrieved successfully", articles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get article details", 
               description = "Public endpoint to retrieve published article details by ID")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleById(
            @Parameter(description = "Article ID") @PathVariable Long id) {
        log.info("Fetching article details for ID: {}", id);
        
        ArticleDTO article = articleService.getArticleById(id, false);
        
        return ResponseEntity.ok(
                ApiResponse.success("Article details retrieved successfully", article));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get article by slug", 
               description = "Public endpoint to retrieve published article by slug")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleBySlug(
            @Parameter(description = "Article slug") @PathVariable String slug) {
        log.info("Fetching article by slug: {}", slug);
        
        ArticleDTO article = articleService.getArticleBySlug(slug);
        
        return ResponseEntity.ok(
                ApiResponse.success("Article retrieved successfully", article));
    }

    @GetMapping("/search")
    @Operation(summary = "Search published articles", 
               description = "Public endpoint to search published articles by keyword")
    public ResponseEntity<ApiResponse<ArticleListResponseDTO>> searchArticles(
            @Parameter(description = "Search keyword") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (limit)") 
            @RequestParam(required = false) Integer limit) {
        log.info("Searching articles - keyword: {}, page: {}, limit: {}", keyword, page, limit);
        
        ArticleListResponseDTO articles = articleService.searchArticles(keyword, page, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Articles retrieved successfully", articles));
    }
}

