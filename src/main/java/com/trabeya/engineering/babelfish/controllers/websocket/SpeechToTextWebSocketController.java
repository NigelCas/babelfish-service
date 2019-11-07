package com.trabeya.engineering.babelfish.controllers.websocket;

import com.trabeya.engineering.babelfish.client.gcp.GoogleSpeechToTextV1Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.apache.commons.codec.binary.Base64;



@Controller
@Slf4j
public class SpeechToTextWebSocketController {

    @Autowired
    private GoogleSpeechToTextV1Client googleSpeechToTextV1Client;

    private StopWatch stopWatch;

    @MessageMapping("/realtime/transcription/transmit")
    //@SendTo("/service/transcriptions/realtime/response")
    public void sendRealTimeSpeechForTextTranscriptions(String audioString) {
        if (null!=googleSpeechToTextV1Client.getNewRealTimeSpeechToTextV1TranscriptionDto()) {
//            stopWatch.start("/transcriptions/realtime/transmit - initialized");
//            if (stopWatch.getLastTaskTimeMillis() >= 60000) {
//                if (stopWatch.isRunning()) {
//                    stopWatch.stop();
//                }
//                else {
//                    log.info("Service - startSpeechToTextTranscription() - Timeout! ");
//                }
//            } else {
                byte[] decodedByte = Base64.decodeBase64(audioString.split(",")[1]);
                googleSpeechToTextV1Client.syncRecognizeFile(decodedByte);
                log.info("Service - sendRealTimeSpeechForTextTranscriptions() - Data Transmitted!");
//            }
        }
        else {
            log.info("Transcription not initiated - POST realtime/transcription/start to start");
        }
    }

}
