package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an article slug already exists
 */
public class ArticleSlugAlreadyExistsException extends BaseBusinessException {
    
    public ArticleSlugAlreadyExistsException(String slug) {
        super(
            "ARTICLE_SLUG_ALREADY_EXISTS",
            "Article slug already exists: " + slug,
            "An article with this slug already exists. Please choose a different slug.",
            HttpStatus.CONFLICT
        );
    }
    
    public ArticleSlugAlreadyExistsException(String slug, Long existingArticleId) {
        super(
            "ARTICLE_SLUG_ALREADY_EXISTS",
            "Article slug already exists: " + slug + " (Article ID: " + existingArticleId + ")",
            "An article with this slug already exists. Please choose a different slug.",
            HttpStatus.CONFLICT
        );
    }
}

