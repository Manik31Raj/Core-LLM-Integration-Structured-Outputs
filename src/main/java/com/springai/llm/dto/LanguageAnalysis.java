package com.springai.llm.dto;

import java.util.List;

public record LanguageAnalysis(
        String language,
        int popularityScore,  //1-10
        String primaryUseCase,
        List<String> topFrameworks,
        int learningDifficulty    // Easy, Medium, Hard
) {}
