package com.trabeya.engineering.babelfish.queue;

import com.trabeya.engineering.babelfish.queue.dto.TranscriptionDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

@Component
@Slf4j
public class DequeueMessage implements Queue {

    @Autowired
    private SimpMessagingTemplate template;

    /**
     * default transcription queue
     *
     * @param transcriptionDto
     * @throws JMSException
     */
   // @JmsListener(destination = TRANSCRIPTION_QUEUE)
    public void receiveTranscription(TranscriptionDto transcriptionDto) throws JMSException {
        log.info("Queue : {}, Message : {}", TRANSCRIPTION_QUEUE, transcriptionDto.toString());
//        template.convertAndSend("/service/transcriptions/realtime/response",
//                transcriptionDto.getMessage());
    }
}
