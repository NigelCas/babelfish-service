package com.trabeya.engineering.babelfish.controllers;

import com.trabeya.engineering.babelfish.client.GoogleSpeechToTextClient;
import com.trabeya.engineering.babelfish.controllers.dtos.NewSpeechToTextRemoteTranscriptionDto;
import com.trabeya.engineering.babelfish.controllers.dtos.NewSpeechToTextTranscriptionDto;
import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisNotFoundException;
import com.trabeya.engineering.babelfish.model.SpeechToTextTranscription;
import com.trabeya.engineering.babelfish.repository.SpeechToTextTranscriptionRepository;
import com.trabeya.engineering.babelfish.service.CloudStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RequestMapping("/babelfish/speech_to_text")
@RestController
@Slf4j
public class SpeechToTextController {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private GoogleSpeechToTextClient googleSpeechToTextClient;

    @Autowired
    private SpeechToTextTranscriptionRepository speechToTextTranscriptionRepository;

    @GetMapping("/transcription/{id}")
    public Resource<SpeechToTextTranscription> getTextToSpeechTranscription(@PathVariable Long id) {
        SpeechToTextTranscription transcription = speechToTextTranscriptionRepository.findById(id)
                .orElseThrow(() -> new TextToSpeechSynthesisNotFoundException(id));
        return new Resource<>(transcription,
                linkTo(methodOn(SpeechToTextController.class).getTextToSpeechTranscription(id)).withSelfRel(),
                linkTo(methodOn(SpeechToTextController.class).getAllTextToSpeechTranscriptions())
                        .withRel("speech-to-text-transcriptions"));
    }


    @GetMapping("/transcriptions")
    public Resources<Resource<SpeechToTextTranscription>> getAllTextToSpeechTranscriptions() {
        List<Resource<SpeechToTextTranscription>> transcriptions =
                                                speechToTextTranscriptionRepository.findAll().stream()
                .map(transcription -> new Resource<>(transcription,
                        linkTo(methodOn(SpeechToTextController.class)
                                .getTextToSpeechTranscription(transcription.getId())).withSelfRel(),
                        linkTo(methodOn(SpeechToTextController.class)
                                .getAllTextToSpeechTranscriptions()).withRel("speech-to-text-transcriptions")))
                .collect(Collectors.toList());
        return new Resources<>(transcriptions,
                linkTo(methodOn(TextToSpeechController.class).getAllTextToSpeechSynthesizations()).withSelfRel());
    }

    @PostMapping("/transcription/short")
    public Resource<SpeechToTextTranscription> startNewShortAudioTranscription
            (@RequestBody() NewSpeechToTextRemoteTranscriptionDto transcription) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/transcription/short/upload")
    public Resource<SpeechToTextTranscription> startNewLocalShortAudioTranscription
            (@RequestParam(value = "file", required = true) MultipartFile file,
            @RequestBody() NewSpeechToTextTranscriptionDto transcription) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/transcription/long")
    public Resource<SpeechToTextTranscription> startNewLongAudioTranscription
            (@RequestBody() NewSpeechToTextRemoteTranscriptionDto transcription) {
        throw new UnsupportedOperationException();
    }

//    @PostMapping("/transcription/streaming/upload")
//    public Resource<SpeechToTextTranscription> startNewFileStreamingTranscription
//            (@RequestBody() NewSpeechToTextTranscriptionDto transcription) {
//
//    @PostMapping("/transcription/streaming")
//    public Resource<SpeechToTextTranscription> startNewFileStreamingTranscription
//            (@RequestBody() NewSpeechToTextTranscriptionDto transcription) {
//
//    }
//
//    @PutMapping("/transcription/streaming/{id}")
//    public Resource<SpeechToTextTranscription> continueFileStreamingTranscription
//            (@PathVariable Long id,
//            @RequestParam(value = "file", required = true) MultipartFile file) {
//
//    }
//
//    @GetMapping("/transcription/streaming/{id}")
//    public Resource<SpeechToTextTranscription> stopFileStreamingTranscription
//            (@PathVariable Long id){
//
//    }
}
