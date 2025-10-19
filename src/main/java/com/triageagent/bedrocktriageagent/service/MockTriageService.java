package com.triageagent.bedrocktriageagent.service;

import com.triageagent.bedrocktriageagent.model.TriageResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockTriageService implements TriageService {

    @Override
    public TriageResult triage(String text) {
        String category = text.toLowerCase().contains("error") ? "BUG" : "QUESTION";
        String severity = category.equals("BUG") ? "HIGH" : "LOW";
        String summary  = category.equals("BUG")
                ? "Potential defect detected; investigate logs and recent changes."
                : "General inquiry; provide guidance or documentation links.";
        return new TriageResult(text, category, severity, summary);
    }
}
