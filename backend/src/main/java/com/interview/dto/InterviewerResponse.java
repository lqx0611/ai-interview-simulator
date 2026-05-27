package com.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InterviewerResponse {

    private String content;

    @JsonProperty("current_topic")
    private String currentTopic;

    @JsonProperty("answer_score")
    private Integer answerScore;

    private String action;
}
