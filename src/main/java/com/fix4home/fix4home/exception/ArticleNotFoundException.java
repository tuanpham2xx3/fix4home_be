package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an article is not found
 */
public class ArticleNotFoundException extends BaseBusinessException {
    
    public ArticleNotFoundException(Long articleId) {
        super(
            "ARTICLE_NOT_FOUND",
            "Article not found with ID: " + articleId,
            "The requested article was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ArticleNotFoundException(String slug) {
        super(
            "ARTICLE_NOT_FOUND",
            "Article not found with slug: " + slug,
            "The requested article was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ArticleNotFoundException(Long articleId, Long userId) {
        super(
            "ARTICLE_NOT_FOUND",
            "Article ID " + articleId + " not found for user ID " + userId,
            "You don't have access to this article or it doesn't exist.",
            HttpStatus.NOT_FOUND
        );
    }
}

