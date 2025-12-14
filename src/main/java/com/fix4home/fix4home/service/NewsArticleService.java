package com.fix4home.fix4home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix4home.fix4home.exception.ArticleNotFoundException;
import com.fix4home.fix4home.exception.ArticleSlugAlreadyExistsException;
import com.fix4home.fix4home.model.dto.article.*;
import com.fix4home.fix4home.model.entity.NewsArticle;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ArticleStatus;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsArticleService extends BaseService implements DTOConverter<NewsArticle, ArticleDTO> {

    private final NewsArticleRepository articleRepository;
    private final ObjectMapper objectMapper;

    // ==================== ADMIN OPERATIONS ====================

    @Transactional
    public ArticleDTO createArticle(CreateArticleRequest request) {
        logBusinessOperation("CREATE_ARTICLE", "title=" + request.getTitle());

        validateRequired(request, "request");
        User user = getCurrentUser();
        requireRole(Role.ADMIN);

        // Validate slug uniqueness
        if (articleRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new ArticleSlugAlreadyExistsException(request.getSlug());
        }

        // Convert DTOs to JSON strings
        String contentJson = convertToJson(request.getContent());
        String heroImageJson = request.getHeroImage() != null ? convertToJson(request.getHeroImage()) : null;
        String sectionsJson = request.getSections() != null && !request.getSections().isEmpty() 
                ? convertToJson(request.getSections()) : null;
        String contactInfoJson = request.getContactInfo() != null ? convertToJson(request.getContactInfo()) : null;

        // Create article
        NewsArticle article = NewsArticle.builder()
                .author(user)
                .title(request.getTitle())
                .shortDescription(request.getShortDescription())
                .slug(request.getSlug())
                .contentJson(contentJson)
                .heroImageJson(heroImageJson)
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .sectionsJson(sectionsJson)
                .contactInfoJson(contactInfoJson)
                .status(ArticleStatus.DRAFT)
                .build();

        NewsArticle savedArticle = articleRepository.save(article);
        return convertToDTO(savedArticle);
    }

    @Transactional
    public ArticleDTO updateArticle(Long id, UpdateArticleRequest request) {
        logBusinessOperation("UPDATE_ARTICLE", "id=" + id);

        validatePositiveId(id, "id");
        validateRequired(request, "request");
        requireRole(Role.ADMIN);

        NewsArticle article = findArticleById(id);

        // Update fields if provided
        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getShortDescription() != null) {
            article.setShortDescription(request.getShortDescription());
        }
        if (request.getSlug() != null && !request.getSlug().equals(article.getSlug())) {
            // Validate slug uniqueness (excluding current article)
            if (articleRepository.existsBySlugExcludingId(request.getSlug(), id)) {
                throw new ArticleSlugAlreadyExistsException(request.getSlug());
            }
            article.setSlug(request.getSlug());
        }
        if (request.getContent() != null) {
            article.setContentJson(convertToJson(request.getContent()));
        }
        if (request.getHeroImage() != null) {
            article.setHeroImageJson(convertToJson(request.getHeroImage()));
        } else if (request.getHeroImage() == null && request.getContent() != null) {
            // Explicitly set to null if provided as null
            article.setHeroImageJson(null);
        }
        if (request.getMetaDescription() != null) {
            article.setMetaDescription(request.getMetaDescription());
        }
        if (request.getMetaKeywords() != null) {
            article.setMetaKeywords(request.getMetaKeywords());
        }
        if (request.getSections() != null) {
            article.setSectionsJson(request.getSections().isEmpty() ? null : convertToJson(request.getSections()));
        }
        if (request.getContactInfo() != null) {
            article.setContactInfoJson(convertToJson(request.getContactInfo()));
        }

        NewsArticle savedArticle = articleRepository.save(article);
        return convertToDTO(savedArticle);
    }

    @Transactional
    public ArticleDTO publishArticle(Long id) {
        logBusinessOperation("PUBLISH_ARTICLE", "id=" + id);

        validatePositiveId(id, "id");
        requireRole(Role.ADMIN);

        NewsArticle article = findArticleById(id);

        if (!article.canBePublished()) {
            throw new IllegalStateException("Article with status " + article.getStatus() + " cannot be published");
        }

        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());

        NewsArticle savedArticle = articleRepository.save(article);
        return convertToDTO(savedArticle);
    }

    @Transactional
    public ArticleDTO unpublishArticle(Long id) {
        logBusinessOperation("UNPUBLISH_ARTICLE", "id=" + id);

        validatePositiveId(id, "id");
        requireRole(Role.ADMIN);

        NewsArticle article = findArticleById(id);

        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("Only published articles can be unpublished");
        }

        article.setStatus(ArticleStatus.UNPUBLISHED);

        NewsArticle savedArticle = articleRepository.save(article);
        return convertToDTO(savedArticle);
    }

    @Transactional
    public void deleteArticle(Long id) {
        logBusinessOperation("DELETE_ARTICLE", "id=" + id);

        validatePositiveId(id, "id");
        requireRole(Role.ADMIN);

        NewsArticle article = findArticleById(id);
        articleRepository.delete(article);
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(Long id, boolean adminAccess) {
        logBusinessOperation("GET_ARTICLE_BY_ID", "id=" + id, "adminAccess=" + adminAccess);

        validatePositiveId(id, "id");

        NewsArticle article = findArticleById(id);

        // Public access: only return published articles
        if (!adminAccess && article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new ArticleNotFoundException(id);
        }

        return convertToDTO(article);
    }

    @Transactional(readOnly = true)
    public ArticleListResponseDTO getArticles(ArticleStatus status, Integer page, Integer limit) {
        logBusinessOperation("GET_ARTICLES", "status=" + status, "page=" + page, "limit=" + limit);

        requireRole(Role.ADMIN);

        // Set defaults for pagination
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        Page<NewsArticle> articlePage;
        if (status != null) {
            articlePage = articleRepository.findByStatus(status, pageable);
        } else {
            articlePage = articleRepository.findAll(pageable);
        }

        List<ArticleDTO> articleDTOs = articlePage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        return ArticleListResponseDTO.builder()
                .articles(articleDTOs)
                .total(articlePage.getTotalElements())
                .page(pageNumber)
                .limit(pageSize)
                .totalPages(articlePage.getTotalPages())
                .build();
    }

    // ==================== PUBLIC OPERATIONS ====================

    @Transactional(readOnly = true)
    public ArticleDTO getArticleBySlug(String slug) {
        logBusinessOperation("GET_ARTICLE_BY_SLUG", "slug=" + slug);

        validateRequired(slug, "slug");

        NewsArticle article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ArticleNotFoundException(slug));

        // Only return published articles
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new ArticleNotFoundException(slug);
        }

        return convertToDTO(article);
    }

    @Transactional(readOnly = true)
    public ArticleListResponseDTO getPublishedArticles(Integer page, Integer limit) {
        logBusinessOperation("GET_PUBLISHED_ARTICLES", "page=" + page, "limit=" + limit);

        // Set defaults for pagination
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("publishedAt").descending());

        Page<NewsArticle> articlePage = articleRepository.findPublishedArticles(ArticleStatus.PUBLISHED, pageable);

        List<ArticleDTO> articleDTOs = articlePage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        return ArticleListResponseDTO.builder()
                .articles(articleDTOs)
                .total(articlePage.getTotalElements())
                .page(pageNumber)
                .limit(pageSize)
                .totalPages(articlePage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public ArticleListResponseDTO searchArticles(String keyword, Integer page, Integer limit) {
        logBusinessOperation("SEARCH_ARTICLES", "keyword=" + keyword, "page=" + page, "limit=" + limit);

        // Set defaults for pagination
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("publishedAt").descending());

        Page<NewsArticle> articlePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            articlePage = articleRepository.searchPublishedArticles(ArticleStatus.PUBLISHED, keyword.trim(), pageable);
        } else {
            articlePage = articleRepository.findPublishedArticles(ArticleStatus.PUBLISHED, pageable);
        }

        List<ArticleDTO> articleDTOs = articlePage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        return ArticleListResponseDTO.builder()
                .articles(articleDTOs)
                .total(articlePage.getTotalElements())
                .page(pageNumber)
                .limit(pageSize)
                .totalPages(articlePage.getTotalPages())
                .build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private NewsArticle findArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    // ==================== JSON CONVERSION HELPERS ====================

    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    private <T> T convertFromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to object: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    private <T> T convertFromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to object: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    // ==================== DTO CONVERSION METHODS ====================

    @Override
    public ArticleDTO convertToDTO(NewsArticle article) {
        // Convert JSON strings to DTOs
        ArticleDTO.ContentStructureDTO content = convertFromJson(
                article.getContentJson(),
                ArticleDTO.ContentStructureDTO.class
        );

        HeroImageDTO heroImage = convertFromJson(
                article.getHeroImageJson(),
                HeroImageDTO.class
        );

        List<SectionDTO> sections = null;
        if (article.getSectionsJson() != null && !article.getSectionsJson().trim().isEmpty()) {
            sections = convertFromJson(
                    article.getSectionsJson(),
                    new TypeReference<List<SectionDTO>>() {}
            );
        }

        ContactInfoDTO contactInfo = convertFromJson(
                article.getContactInfoJson(),
                ContactInfoDTO.class
        );

        // Build metadata
        ArticleMetadataDTO metadata = ArticleMetadataDTO.builder()
                .author(article.getAuthor().getUsername())
                .authorId(article.getAuthor().getId())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .build();

        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .shortDescription(article.getShortDescription())
                .slug(article.getSlug())
                .content(content)
                .heroImage(heroImage)
                .metaDescription(article.getMetaDescription())
                .metaKeywords(article.getMetaKeywords())
                .status(article.getStatus())
                .sections(sections)
                .contactInfo(contactInfo)
                .metadata(metadata)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}

