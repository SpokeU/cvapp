package dev.omyshko.cvapp.service.model;

import java.util.List;

public record LLMParsedResume(String name, String profession, int experience, List<String> skills, List<String> highlights) {
}
