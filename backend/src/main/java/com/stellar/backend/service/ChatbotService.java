package com.stellar.backend.service;

import com.stellar.backend.dto.EventResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final EventService eventService;
    private final RestTemplate restTemplate;

    public ChatbotService(EventService eventService, RestTemplate restTemplate) {
        this.eventService = eventService;
        this.restTemplate = restTemplate;
    }

    public String getChatResponse(String userMessage) {
        List<EventResponseDto> events = eventService.getAllEvents();
        String context = formatEventsContext(events);

        String systemInstruction = "Bạn là trợ lý thông minh của hệ thống quản lý sự kiện Ve'ryGood. " +
                "Nhiệm vụ của bạn LÀ CHỈ giúp người dùng tìm kiếm, đề xuất các sự kiện và giải đáp các thắc mắc LIÊN QUAN ĐẾN HỆ THỐNG Ve'ryGood. " +
                "Dưới đây là danh sách các sự kiện thực tế hiện có:\n" + context + "\n\n" +
                "QUY TẮC QUAN TRỌNG:\n" +
                "1. CHỈ trả lời các câu hỏi liên quan đến Ve'ryGood, sự kiện, giá vé, và quy trình đặt vé.\n" +
                "2. Nếu người dùng hỏi các câu hỏi ngoài phạm vi (như nấu ăn, thời tiết thế giới, kiến thức tổng quát, v.v.), hãy lịch sự từ chối và nói rằng bạn chỉ có thể hỗ trợ các thông tin liên quan đến Ve'ryGood.\n" +
                "3. Luôn trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp.";

        return callGemini(systemInstruction, userMessage);
    }

    private String formatEventsContext(List<EventResponseDto> events) {
        if (events == null || events.isEmpty()) return "Hiện không có sự kiện nào.";
        return events.stream().map(e -> String.format("- Tên: %s, Thời gian: %s, Địa điểm: %s, Giá từ: %s, Phân loại: %s, Nổi bật: %s",
                e.getTitle(), e.getDate(), e.getLocation(), e.getStartingPrice(), e.getCategory(), e.getIsFeatured()))
                .collect(Collectors.joining("\n"));
    }

    private String callGemini(String systemInstruction, String userMessage) {
        try {
            String fullPrompt = systemInstruction + "\n\nNgười dùng: " + userMessage;

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", fullPrompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String urlWithKey = apiUrl + "?key=" + apiKey;

            Map<String, Object> response = restTemplate.postForObject(urlWithKey, entity, Map.class);
            logger.info("Gemini API Response: {}", response);
            
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentRes = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> partsRes = (List<Map<String, Object>>) contentRes.get("parts");
                    if (!partsRes.isEmpty()) {
                        return (String) partsRes.get(0).get("text");
                    }
                }
            }
            return "Xin lỗi, tôi gặp chút trục trặc khi kết nối với não bộ AI. Vui lòng thử lại sau!";
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Gemini API Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Lỗi kết nối AI (404/403). Vui lòng kiểm tra lại cấu hình model hoặc API Key.";
        } catch (Exception e) {
            logger.error("System Error: ", e);
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
}
