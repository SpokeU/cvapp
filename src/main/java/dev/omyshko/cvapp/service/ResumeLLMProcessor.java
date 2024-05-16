package dev.omyshko.cvapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.omyshko.cvapp.service.model.LLMParsedResume;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_3_5_TURBO;

@Component
public class ResumeLLMProcessor {

    private static final SystemMessage SYSTEM_MESSAGE = SystemMessage.from("""
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

    public static final String CV_MESSAGE = "Here is CV of a candidate: {{resume}}";


    public static final ApacheTikaDocumentParser DOCUMENT_PARSER = new ApacheTikaDocumentParser();
    private final OpenAiChatModel chatLanguageModel;
    private final ObjectMapper objectMapper;
    private final EmbeddingStoreIngestor embeddingStoreIngestor;


    //TODO
    ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer(GPT_3_5_TURBO));

    public ResumeLLMProcessor(OpenAiChatModel chatLanguageModel, ObjectMapper objectMapper, EmbeddingStoreIngestor embeddingStoreIngestor) {
        this.chatLanguageModel = chatLanguageModel;
        this.objectMapper = objectMapper;
        this.embeddingStoreIngestor = embeddingStoreIngestor;
    }

    public LLMParsedResume process(String filePath) {
        //Load
        Document document = FileSystemDocumentLoader.loadDocument(filePath, DOCUMENT_PARSER);

        //Embed
        embeddingStoreIngestor.ingest(document);

        //Parse using LLM
        Prompt prompt = PromptTemplate.from(CV_MESSAGE).apply(Map.of("resume", document.text()));
        Response<AiMessage> aiMessage = chatLanguageModel.generate(List.of(SYSTEM_MESSAGE, prompt.toUserMessage()));

        String llmResponse = aiMessage.content().text();

        //Convert to an object
        try {
            LLMParsedResume llmParsedResume = objectMapper.readValue(llmResponse, LLMParsedResume.class);
            return llmParsedResume;
        } catch (JsonProcessingException e) {
            //TODO Retry asking LLM to fix the error?
            throw new RuntimeException(e);
        }
    }




}
