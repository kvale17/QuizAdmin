package com.myquiz.quizadmin;

public class SubjectModel {

    private String id;
    private String name;
    private String noOfGrades;
    private String gradeCounter;

    public SubjectModel(String id, String name, String noOfGrades, String gradeCounter) {
        this.id = id;
        this.name = name;
        this.noOfGrades = noOfGrades;
        this.gradeCounter = gradeCounter;
    }

    public String getGradeCounter() {
        return gradeCounter;
    }

    public void setGradeCounter(String gradeCounter) {
        this.gradeCounter = gradeCounter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNoOfGrades() {
        return noOfGrades;
    }

    public void setNoOfGrades(String noOfGrades) {
        this.noOfGrades = noOfGrades;
    }
}
