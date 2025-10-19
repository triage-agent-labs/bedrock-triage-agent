package com.triageagent.bedrocktriageagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.triageagent.bedrocktriageagent.model.TriageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.BedrockRuntimeException;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;

@Service
@Primary
public class BedrockTriageService implements TriageService {

    private static final String ANTHROPIC_VERSION = "bedrock-2023-05-31";

    private final BedrockRuntimeClient client;   // <-- injected (uses your profile/SSO)
    private final ObjectMapper mapper = new ObjectMapper();

    private final String modelId;
    private final int maxTokens;
    private final double temperature;
    private final String guardrailId;        // optional
    private final String guardrailVersion;   // optional

    public BedrockTriageService(
            BedrockRuntimeClient client,  // <-- inject the bean defined in BedrockConfig
            @Value("${app.bedrock.modelId:anthropic.claude-3-haiku-20240307-v1:0}") String modelId,
            @Value("${app.bedrock.maxTokens:512}") int maxTokens,
            @Value("${app.bedrock.temperature:0.0}") double temperature,
            @Value("${app.bedrock.guardrailId:}") String guardrailId,
            @Value("${app.bedrock.guardrailVersion:}") String guardrailVersion
    ) {
        this.client = client;
        this.modelId = modelId;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.guardrailId = guardrailId == null ? "" : guardrailId.trim();
        this.guardrailVersion = guardrailVersion == null ? "" : guardrailVersion.trim();
    }

    @Override
    public TriageResult triage(String text) throws JsonProcessingException {
        final String instruction = """
            You are a triage bot. Return STRICT JSON with keys:
            {"category":"BUG|QUESTION|FEATURE","severity":"LOW|MEDIUM|HIGH","summary":"<one line>"}
            Classify the user's issue and keep the summary short.
            """;

        ObjectNode payload = mapper.createObjectNode();
        payload.put("anthropic_version", ANTHROPIC_VERSION);
        payload.put("max_tokens", maxTokens);
        payload.put("temperature", temperature);

        if (!guardrailId.isEmpty() && !guardrailVersion.isEmpty()) {
            ObjectNode gr = mapper.createObjectNode();
            gr.put("guardrailId", guardrailId);
            gr.put("guardrailVersion", guardrailVersion);
            payload.set("guardrailConfig", gr);
        }

        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        var contentArr = mapper.createArrayNode();
        contentArr.add(mapper.createObjectNode()
                .put("type", "text")
                .put("text", instruction + "\n\nIssue:\n" + (text == null ? "" : text)));
        userMsg.set("content", contentArr);
        payload.set("messages", mapper.createArrayNode().add(userMsg));

        InvokeModelResponse res;
        try {
            res = client.invokeModel(InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromString(payload.toString(), StandardCharsets.UTF_8))
                    .build());
        } catch (BedrockRuntimeException e) {
            throw new RuntimeException("Bedrock invoke failed: " + e.getMessage(), e);
        }

        String body = res.body().asUtf8String();
        JsonNode root = mapper.readTree(body);
        String jsonFromModel = root.path("content").isArray() && root.path("content").size() > 0
                ? root.path("content").get(0).path("text").asText("{}")
                : "{}";

        JsonNode out;
        try {
            out = mapper.readTree(jsonFromModel);
        } catch (JsonProcessingException badJson) {
            out = mapper.createObjectNode();
        }

        String category = asTextOr(out, "category", "QUESTION");
        String severity = asTextOr(out, "severity", "LOW");
        String summary  = asTextOr(out, "summary", "No summary.");

        return new TriageResult(text, category, severity, summary);
    }

    private static String asTextOr(JsonNode node, String field, String fallback) {
        JsonNode v = node.get(field);
        return (v != null && v.isTextual()) ? v.asText() : fallback;
    }
}
