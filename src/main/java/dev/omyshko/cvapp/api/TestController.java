package dev.omyshko.cvapp.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.omyshko.cvapp.service.model.LLMParsedResume;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_3_5_TURBO;

@RestController
public class TestController {

    ChatLanguageModel chatLanguageModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer(GPT_3_5_TURBO));


    TestController(ChatLanguageModel chatLanguageModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingStore = embeddingStore;
    }

    @PostMapping("cv/parse")
    public String parse(@RequestParam String filename) {
        //Load
        Document document = FileSystemDocumentLoader.loadDocument("src/main/resources/cv/hr_admin.txt");

        //Split
        DocumentSplitter splitter = DocumentSplitters.recursive(100, 0, new OpenAiTokenizer(GPT_3_5_TURBO));
        List<TextSegment> segments = splitter.split(document);

        //Embed
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        //Store
        embeddingStore.addAll(embeddings, segments);

        //Question
        String question = "What is the name of a person who is Dedicated Customer Service Manager with 15+ years of experience in Hospitality?";
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        int maxResults = 3;
        double minScore = 0.7;
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore
                .findRelevant(questionEmbedding, maxResults, minScore);


        return "Zaibis";
    }

    @GetMapping("/model")
    public LLMParsedResume model(@RequestParam(value = "message", defaultValue = "Hello") String message) throws JsonProcessingException {

        SystemMessage systemMessage = SystemMessage.from("""
                You are a tool that parses CV of a candidate and responds with JSON.

                You need to extract following fields:
                "name" - A name of a candidate. If missing then make up any random name. Be creative and don't use John Show
                "profession" - Procession of a candidate
                "experience" - Total years of experience. Example: 36. To calculate experience use reasoning and Chain of thoughts. Please put it into "experience_reasoning". Current year is 2024. Use it in your calculations
                "skills" - Main and most actual skills of a candidate
                "highlights" - Professional highlights of a candidate which characterises him the most

                Example of JSON response:
                {
                    "name": "Jack Black",
                    "profession": "Support Engineering Manager",
                    "experience_reasoning": "{your chain of thoughts to calculate 'experience' field}"
                    "experience": 31,
                    "skills":["Virtualization", "Networking","Windows Platforms"],
                    "highlights": ["Managed teams of up to 26 engineers across various technical fields.", "On-boarded and mentored new managers"]
                }
                """);

        Document document = FileSystemDocumentLoader.loadDocument("src/main/resources/cv/hr_admin.txt");

        PromptTemplate promptTemplate = PromptTemplate.from("Here is CV of a candidate: {{resume}}");
        Prompt prompt = promptTemplate.apply(Map.of("resume", document.text()));


        Response<AiMessage> aiMessage = chatLanguageModel.generate(List.of(systemMessage, prompt.toUserMessage()));

        String LLMresponse = aiMessage.content().text();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        LLMParsedResume llmParsedResume = objectMapper.readValue(LLMresponse, LLMParsedResume.class);

        return llmParsedResume;
    }

}
