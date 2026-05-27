package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartInterviewResponse {

    private Long interviewId;
    private String openingMessage;
}
