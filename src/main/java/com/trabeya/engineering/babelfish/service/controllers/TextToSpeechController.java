package com.trabeya.engineering.babelfish.service.controllers;

import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.Voice;
import com.google.cloud.translate.Language;
import com.trabeya.engineering.babelfish.service.client.GoogleTextToSpeech;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
@RequestMapping("/babelfish/synthesis/text_to_speech")
@RestController
@Slf4j
public class TextToSpeechController {

    @Autowired
    private GoogleTextToSpeech textToSpeechClient;

    @GetMapping("/test")
    public ResponseEntity<byte[]> test() {
        // The test text to translate
        String text = "This is a test!";
        byte[] resource = textToSpeechClient.synthesizeTextV1(
                text, "en_us", SsmlVoiceGender.FEMALE, AudioEncoding.MP3);
        log.info("resource length :"+resource.length);

        //since the resulting audio is a .mp3
        String filename = "test_result.mp3";
        String contentType = "audio/mp3";
        String[] subType = contentType.split(Pattern.quote("/"));
        return ResponseEntity.ok()
                .contentType(new MediaType(subType[0],subType[1]))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/support/voices")
    public Resources<Resource<Map<String,String>>> getAllSupportedV1Voices() {
        List<Resource<Map<String,String>>> voices = new ArrayList<>();
        for (Voice voice : textToSpeechClient.listAllSupportedVoicesV1()) {
            Map<String,String> voiceList = new HashMap<>();
            voiceList.put(voice.getName(), voice.getSsmlGender().name());
            Resource<Map<String,String>> languageResource = new Resource<>(voiceList);
            voices.add(languageResource);
        }
        return new Resources<>(voices);

    }


}
