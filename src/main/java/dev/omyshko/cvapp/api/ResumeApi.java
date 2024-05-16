package dev.omyshko.cvapp.api;

import dev.omyshko.cvapp.data.ResumeRepository;
import dev.omyshko.cvapp.data.model.Resume;
import dev.omyshko.cvapp.service.ResumeLLMProcessor;
import dev.omyshko.cvapp.service.StorageService;
import dev.omyshko.cvapp.service.model.LLMParsedResume;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/resume")
public class ResumeApi {

    private final StorageService storageService;
    private final ResumeLLMProcessor resumeLLMProcessor;
    private final ResumeRepository resumeRepository;

    public ResumeApi(StorageService storageService, ResumeLLMProcessor resumeLLMProcessor, ResumeRepository resumeRepository) {
        this.storageService = storageService;
        this.resumeLLMProcessor = resumeLLMProcessor;
        this.resumeRepository = resumeRepository;
    }

    @GetMapping("/")
    public List<Resume> getAll() {
        return resumeRepository.findAll();
    }

    @GetMapping("/{id}")
    public Resume getById(@PathVariable int id) {
        return resumeRepository.findById(id);
    }

    @PostMapping("/upload")
    public ResponseEntity<Resume> upload(@RequestParam("file") MultipartFile file) {
        Resume resume = processResume(file);
        return ResponseEntity.ok(resume);
    }

    @PostMapping("/uploadMultiple")
    public ResponseEntity<List<Resume>> uploadMultiple(@RequestParam("files") List<MultipartFile> file) {
        List<Resume> list = file.stream().map(this::processResume).toList();
        return ResponseEntity.ok(list);
    }

    private Resume processResume(MultipartFile file) {
        String storedFilePath = storageService.store(file);
        LLMParsedResume llmResume = resumeLLMProcessor.process(storedFilePath);
        Resume resume = convert(storedFilePath, llmResume);
        return resumeRepository.save(resume);
    }

    private Resume convert(String resumeFileLocation, LLMParsedResume llmParsedResume) {
        return new Resume(llmParsedResume.name(), llmParsedResume.profession(), llmParsedResume.experience(), llmParsedResume.skills(), llmParsedResume.highlights(), resumeFileLocation);
    }

}
