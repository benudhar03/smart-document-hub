package com.ar.document.hub.controller;

import com.ar.document.hub.service.RedisService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/collaboration")
@Slf4j
public class CollaborationController {

    @Autowired
    private RedisService redisService;

    // In-memory store for active sessions (in production, use Redis)
    private final Map<String, CollaborationSession> activeSessions = new ConcurrentHashMap<>();

    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(@RequestBody CreateSessionRequest request) {
        String sessionId = UUID.randomUUID().toString();

        CollaborationSession session = new CollaborationSession();
        session.setSessionId(sessionId);
        session.setDocumentId(request.getDocumentId());
        session.setCreatedAt(System.currentTimeMillis());
        session.setParticipants(1);

        activeSessions.put(sessionId, session);
        redisService.cacheCollaborationSession(sessionId, session);

        log.info("Collaboration session created: {}", sessionId);

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "joinUrl", "/api/collaboration/sessions/" + sessionId + "/join"
        ));
    }

    @PostMapping("/sessions/{sessionId}/join")
    public ResponseEntity<?> joinSession(@PathVariable String sessionId) {
        CollaborationSession session = activeSessions.get(sessionId);

        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        session.setParticipants(session.getParticipants() + 1);
        activeSessions.put(sessionId, session);

        return ResponseEntity.ok(Map.of(
                "message", "Joined session successfully",
                "session", session
        ));
    }

    @PostMapping("/sessions/{sessionId}/leave")
    public ResponseEntity<?> leaveSession(@PathVariable String sessionId) {
        CollaborationSession session = activeSessions.get(sessionId);

        if (session != null) {
            session.setParticipants(Math.max(0, session.getParticipants() - 1));
            if (session.getParticipants() == 0) {
                activeSessions.remove(sessionId);
                redisService.removeCache("session:" + sessionId);
            } else {
                activeSessions.put(sessionId, session);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Left session successfully"));
    }

    @GetMapping("/sessions/{sessionId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable String sessionId) {
        CollaborationSession session = activeSessions.get(sessionId);

        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "participants", session.getParticipants()
        ));
    }

    @Data
    public static class CreateSessionRequest {
        private UUID documentId;
        private String createdBy;
    }

    @Data
    public static class CollaborationSession {
        private String sessionId;
        private UUID documentId;
        private long createdAt;
        private int participants;
    }
}