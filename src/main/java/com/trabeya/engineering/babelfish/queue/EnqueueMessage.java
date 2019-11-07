package com.trabeya.engineering.babelfish.queue;

import com.trabeya.engineering.babelfish.queue.dto.TranscriptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EnqueueMessage implements Queue {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendTranscription(TranscriptionDto transcriptionDto) {
        try {
            jmsTemplate.convertAndSend(TRANSCRIPTION_QUEUE, transcriptionDto);
            log.info("transcription sent to queue {} ", transcriptionDto);
        } catch (Exception e) {
            log.error("transcription sent error {} ", transcriptionDto, e);
        }
    }

}
