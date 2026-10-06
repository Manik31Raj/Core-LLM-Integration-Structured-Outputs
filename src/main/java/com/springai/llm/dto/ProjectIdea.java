package com.springai.llm.dto;


import java.util.List;

public record ProjectIdea(
        String title,
        String description,
        String difficulty,
        List<String> skillsLearned
) {}