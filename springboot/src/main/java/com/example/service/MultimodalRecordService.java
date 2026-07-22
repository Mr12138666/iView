package com.example.service;

import com.example.entity.Account;
import com.example.entity.MultimodalRecord;
import com.example.mapper.MultimodalRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultimodalRecordService {

    private static final String STATUS_RESERVED = "RESERVED";

    @Resource
    private MultimodalRecordMapper recordMapper;

    public MultimodalRecord createCodeRecord(Account account, String sessionId, String language, String code) {
        MultimodalRecord record = baseRecord(account, sessionId, "CODE");
        record.setCodeLanguage(language == null || language.trim().isEmpty() ? "unknown" : language.trim());
        record.setCodeContent(code == null ? "" : code.trim());
        record.setCompileOutput("");
        record.setRunOutput("");
        record.setSuggestion("Code-run sandbox is reserved. The server stores code text for interview evaluation and does not execute untrusted code.");
        recordMapper.insert(record);
        return record;
    }

    public MultimodalRecord createAudioRecord(Account account, String sessionId, String fileName, long fileSize) {
        MultimodalRecord record = baseRecord(account, sessionId, "AUDIO");
        record.setTranscript("");
        record.setSuggestion("ASR is reserved. Uploaded audio metadata has been recorded for future transcription integration.");
        record.setAudioUrl(formatUploadMarker(fileName, fileSize));
        recordMapper.insert(record);
        return record;
    }

    public MultimodalRecord createVideoRecord(Account account, String sessionId, String fileName, long fileSize) {
        MultimodalRecord record = baseRecord(account, sessionId, "VIDEO");
        record.setVisualResult("{}");
        record.setSuggestion("Video analysis is reserved. Uploaded visual metadata has been recorded for future non-verbal analysis.");
        record.setVideoUrl(formatUploadMarker(fileName, fileSize));
        recordMapper.insert(record);
        return record;
    }

    public List<MultimodalRecord> listBySession(Account account, String sessionId) {
        return recordMapper.selectBySessionId(sessionId, account.getId());
    }

    private MultimodalRecord baseRecord(Account account, String sessionId, String modality) {
        MultimodalRecord record = new MultimodalRecord();
        record.setSessionId(normalizeSessionId(sessionId));
        record.setUserId(account.getId());
        record.setModality(modality);
        record.setStatus(STATUS_RESERVED);
        return record;
    }

    private String normalizeSessionId(String sessionId) {
        return sessionId == null || sessionId.trim().isEmpty() ? null : sessionId.trim();
    }

    private String formatUploadMarker(String fileName, long fileSize) {
        String safeName = fileName == null || fileName.trim().isEmpty() ? "unnamed" : fileName.trim();
        return "pending://" + safeName + "?size=" + Math.max(fileSize, 0);
    }
}
