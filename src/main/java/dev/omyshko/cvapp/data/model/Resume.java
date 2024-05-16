package dev.omyshko.cvapp.data.model;

import java.util.List;

public class Resume {

    public Resume() {
    }

    public Resume(String name, String profession, int experience, List<String> skills, List<String> highlights, String resumeFileLocation) {
        this.name = name;
        this.profession = profession;
        this.experience = experience;
        this.skills = skills;
        this.highlights = highlights;
        this.resumeFileLocation = resumeFileLocation;
    }

    private Long id;
    private String name;
    private String profession;
    private int experience;
    private List<String> skills;
    private java.util.List<String> highlights;
    private String resumeFileLocation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public String getResumeFileLocation() {
        return resumeFileLocation;
    }

    public void setResumeFileLocation(String resumeFileLocation) {
        this.resumeFileLocation = resumeFileLocation;
    }
}
