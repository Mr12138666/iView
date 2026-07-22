package com.example.service;

import com.example.entity.Account;
import com.example.entity.MultimodalRecord;
import com.example.mapper.MultimodalRecordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MultimodalRecordServiceTest {

    @Test
    void createCodeRecordStoresReservedCodePayload() {
        MultimodalRecordMapper recordMapper = mock(MultimodalRecordMapper.class);
        MultimodalRecordService service = new MultimodalRecordService();
        ReflectionTestUtils.setField(service, "recordMapper", recordMapper);
        when(recordMapper.insert(any(MultimodalRecord.class))).thenAnswer(invocation -> {
            MultimodalRecord record = invocation.getArgument(0);
            record.setId(42L);
            return 1;
        });

        Account account = new Account();
        account.setId(5);

        MultimodalRecord record = service.createCodeRecord(
                account,
                "session-1",
                "java",
                "System.out.println(\"hello\");"
        );

        assertThat(record.getId()).isEqualTo(42L);
        assertThat(record.getUserId()).isEqualTo(5);
        assertThat(record.getSessionId()).isEqualTo("session-1");
        assertThat(record.getModality()).isEqualTo("CODE");
        assertThat(record.getStatus()).isEqualTo("RESERVED");
        assertThat(record.getCodeLanguage()).isEqualTo("java");
        assertThat(record.getCodeContent()).contains("hello");
        assertThat(record.getRunOutput()).isEqualTo("");
        verify(recordMapper).insert(any(MultimodalRecord.class));
    }
}
