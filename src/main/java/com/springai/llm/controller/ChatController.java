package com.springai.llm.controller;


import com.springai.llm.dto.LanguageAnalysis;
import com.springai.llm.dto.ProjectIdea;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatClient chatClient;

    @Value("classpath:/prompts/prompt1.st")
    private Resource promptResource;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                //This is system prompt that is given to a system message to set the context for the conversation. It is used to instruct the model on how to behave and respond to user inputs.
                .defaultSystem("You are a world-class expert in {domain}. Answer the following question clearly and concisely." +
                        "Any question that is asked outside of the domain, decline it by responding 'I am specialized in {domain} and cannot answer questions outside of this domain.'")
                .build();
    }

    @GetMapping("/ask")
    public String ask(){
        return chatClient
                .prompt()
                .user("Hey, What is Spring AI ? ")
                .call()
                .content();     // gives response in string format
    }


    @GetMapping("/ask2")
    public String ask2(@RequestParam String domain, @RequestParam String question) {
        String promptTemplate = """
                You are a world-class expert in {domain}.
                Answer the following question clearly and concisely.
                
                Question: {question}
                """;

        return chatClient
                .prompt()
                .user(u -> u.text(promptTemplate)
                            .param("domain", domain)
                            .param("question", question))
                .call()
                .content();
    }

    @GetMapping("/ask3")
    public String ask3(@RequestParam String domain, @RequestParam String question) {

        PromptTemplate promptTemplate =new PromptTemplate(promptResource);

        Prompt prompt=promptTemplate.create(Map.of("domain", domain, "question", question));

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }

    @GetMapping(value ="/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
     public Flux<String> stream() {
        return chatClient
                .prompt()
                .user("Hey, what is Spring AI?")
                .stream()
                .content();
    }

    @GetMapping("/strask")
    public ActorFilms strAsk(){

        ActorFilms entity = chatClient
                .prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);

        log.info("ActorFilms {}", entity);

        return entity;
    }

    @GetMapping("/analyze-language")
    public LanguageAnalysis analyzeLanguage(@RequestParam String language) {

         BeanOutputConverter<LanguageAnalysis> converter = new BeanOutputConverter<>(LanguageAnalysis.class);

         return chatClient
                .prompt()
                .user(u -> u.text("""
                        Analyze the programming language: {language}
                        
                        popularity score: between 1-10, 1 is low, 10 is high
                        learningDifficulty: either of these 3 values: "Easy", "Medium", "Hard"
                        {format}
                        """)
                        .param("language", language)
                        .param("format", converter.getFormat())) // format is used to specify the output format and respond in json
                .call()
                .entity(LanguageAnalysis.class);
    }

     @GetMapping("/suggest-projects")
    public List<ProjectIdea> suggestProjects(@RequestParam String techStack) {

        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<ProjectIdea>>() {
                }
        );

        return chatClient.prompt()
                .user(u -> u.text("""
                        Suggest 3 beginner project ideas for a developer using {techStack}.
                        
                        {format}
                        """)
                        .param("techStack", techStack)
                        .param("format", converter.getFormat()))
                .call()
                .entity(new ParameterizedTypeReference<>(){});
    }

}

 record ActorFilms(String actor, List<String> movies) {}
