package com.fix4home.fix4home.exception;
 
public class SkillNotFoundException extends BusinessValidationException {
    public SkillNotFoundException(Long skillId) {
        super("Skill not found with id: " + skillId);
    }
} 