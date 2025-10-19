package com.triageagent.bedrocktriageagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.triageagent.bedrocktriageagent.model.TriageResult;

public interface TriageService {
    TriageResult triage(String text) throws JsonProcessingException;
}
