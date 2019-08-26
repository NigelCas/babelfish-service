package com.trabeya.engineering.babelfish.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.Voice;
import com.trabeya.engineering.babelfish.client.GoogleTextToSpeechClient;
import com.trabeya.engineering.babelfish.controllers.dtos.NewTextToSpeechSynthesisDto;
import com.trabeya.engineering.babelfish.controllers.dtos.TextToSpeechSupportedVoiceDto;
import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisFailedException;
import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisNotFoundException;
import com.trabeya.engineering.babelfish.model.AudioFileMetaData;
import com.trabeya.engineering.babelfish.model.Status;
import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesisDeviceProfile;
import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesis;
import com.trabeya.engineering.babelfish.repository.AudioFileMetaDataRepository;
import com.trabeya.engineering.babelfish.repository.TextToSpeechSynthesisRepository;
import com.trabeya.engineering.babelfish.service.CloudStorageService;
import com.trabeya.engineering.babelfish.util.AudioFileMetaDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@SuppressWarnings("WeakerAccess")
@RequestMapping("/babelfish/text_to_speech")
@RestController
@Slf4j
public class TextToSpeechController {

    @Autowired
    private AudioFileMetaDataRepository audioFileMetaDataRepository;

    @Autowired
    private TextToSpeechSynthesisRepository synthesisRepository;

    @Autowired
    private GoogleTextToSpeechClient textToSpeechClient;

    @Autowired
    private CloudStorageService cloudStorageService;

    private static final String attachment  = "attachment; filename=\"";

    @PostMapping("/test/text")
    public ResponseEntity<byte[]> testText(@RequestBody String input) {
        byte[] resource = textToSpeechClient.synthesizeTextV1(
                input, "en-US", SsmlVoiceGender.FEMALE, AudioEncoding.OGG_OPUS,
                TextToSpeechSynthesisDeviceProfile.HANDSET_CLASS_DEVICE, 1.0, 0);
        log.info("test output audio byte length :"+resource.length);

        //since the resulting audio is a .
        String filename = "test_result.opus";
        String contentType = "audio/ogg";
        String[] subType = contentType.split(Pattern.quote("/"));
        return ResponseEntity.ok()
                .contentType(new MediaType(subType[0],subType[1]))
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment + filename + "\"")
                .body(resource);
    }

    @GetMapping("/support/voices")
    public Resources<Resource<TextToSpeechSupportedVoiceDto>> getAllSupportedV1Voices() {
        List<Resource<TextToSpeechSupportedVoiceDto>> voices = new ArrayList<>();
        for (Voice voice : textToSpeechClient.listAllSupportedVoicesV1()) {
            TextToSpeechSupportedVoiceDto supportedVoice = new TextToSpeechSupportedVoiceDto();
            supportedVoice.setLanguageCodes(voice.getLanguageCodesList().toArray());
            supportedVoice.setName(voice.getName());
            supportedVoice.setSsmlGender(voice.getSsmlGender());
            supportedVoice.setNaturalSampleRateHertz(voice.getNaturalSampleRateHertz());
            Resource<TextToSpeechSupportedVoiceDto> languageResource = new Resource<>(supportedVoice);
            voices.add(languageResource);
        }
        return new Resources<>(voices);
    }

    @GetMapping("/synthesization/{id}")
    public Resource<TextToSpeechSynthesis> getTextToSpeechSynthesization(@PathVariable Long id) {
        TextToSpeechSynthesis synthesization = synthesisRepository.findById(id)
                .orElseThrow(() -> new TextToSpeechSynthesisNotFoundException(id));
        return new Resource<>(synthesization,
                linkTo(methodOn(TextToSpeechController.class).getTextToSpeechSynthesization(id)).withSelfRel(),
                linkTo(methodOn(TextToSpeechController.class).getAllTextToSpeechSynthesizations())
                        .withRel("text-to-speech-synthesizations"));
    }


    @GetMapping("/synthesizations")
    public Resources<Resource<TextToSpeechSynthesis>> getAllTextToSpeechSynthesizations() {
        List<Resource<TextToSpeechSynthesis>> synthesizations = synthesisRepository.findAll().stream()
                .map(synthesization -> new Resource<>(synthesization,
                        linkTo(methodOn(TextToSpeechController.class)
                                .getTextToSpeechSynthesization(synthesization.getId())).withSelfRel(),
                        linkTo(methodOn(TextToSpeechController.class)
                                .getAllTextToSpeechSynthesizations()).withRel("text-to-speech-synthesizations")))
                .collect(Collectors.toList());
        return new Resources<>(synthesizations,
                linkTo(methodOn(TextToSpeechController.class).getAllTextToSpeechSynthesizations()).withSelfRel());
    }

    @PostMapping("/synthesizations")
    public ResponseEntity<byte[]> newSynthesization(@RequestBody @Valid NewTextToSpeechSynthesisDto synthesization) {
        ResponseEntity<byte[]> responseEntity;
        TextToSpeechSynthesis inProgressSynthesis = new TextToSpeechSynthesis();
        inProgressSynthesis.setStatus(Status.IN_PROGRESS);
        byte[] audioResource = new byte[0];
        try{
            // Initial Pending state save
            BeanUtils.copyProperties(synthesization, inProgressSynthesis);
            inProgressSynthesis = synthesisRepository.save(inProgressSynthesis);

            if( (null!=synthesization.getAudioSpeakingRate())
                && (null==synthesization.getPitch())) {
                    audioResource = textToSpeechClient.synthesizeTextV1(
                        synthesization.getInputText(),
                        synthesization.getVoiceLanguageCode(),
                        synthesization.getVoiceGender(),
                        synthesization.getAudioEncoding(),
                        synthesization.getAudioDeviceProfile(),
                        synthesization.getAudioSpeakingRate().doubleValue(), 0);
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(custom user speaking rate) byte length :"
                        +audioResource.length+" Bytes");
            }
        else if( (null!=synthesization.getPitch())
                    && (null==synthesization.getAudioSpeakingRate())) {
                audioResource = textToSpeechClient.synthesizeTextV1(
                        synthesization.getInputText(),
                        synthesization.getVoiceLanguageCode(),
                        synthesization.getVoiceGender(),
                        synthesization.getAudioEncoding(),
                        synthesization.getAudioDeviceProfile(),
                        1.0, synthesization.getPitch().doubleValue());
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(custom user pitch) byte length :"
                        +audioResource.length+" Bytes");
        }
        else if( (null!=synthesization.getPitch())
                    && (null!=synthesization.getAudioSpeakingRate())) {
                audioResource = textToSpeechClient.synthesizeTextV1(
                        synthesization.getInputText(),
                        synthesization.getVoiceLanguageCode(),
                        synthesization.getVoiceGender(),
                        synthesization.getAudioEncoding(),
                        synthesization.getAudioDeviceProfile(),
                        synthesization.getAudioSpeakingRate().doubleValue(),
                        synthesization.getPitch().doubleValue());
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(custom user pitch & speaking rate) byte length :"
                        +audioResource.length+" Bytes");
        }
        else {
                audioResource = textToSpeechClient.synthesizeTextV1(
                        synthesization.getInputText(),
                        synthesization.getVoiceLanguageCode(),
                        synthesization.getVoiceGender(),
                        synthesization.getAudioEncoding(),
                        synthesization.getAudioDeviceProfile(),
                        1.0, 0);
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(default pitch & speaking rate) byte length :"
                        +audioResource.length+" Bytes");
            }

            // Get Audio metadata
            inProgressSynthesis.setDetectedAudioMetaData(
                    audioFileMetaDataRepository.save(getAudioMetaDataFromFile(audioResource)));

            String fileUUID = UUID.randomUUID().toString();
            String filename ="";
            String fileContentType ="";
            if(synthesization.getAudioEncoding().equals(AudioEncoding.LINEAR16)) {
                filename = fileUUID+".wav";
                fileContentType = "audio/wav";

                // Return output audio file
                String[] subType = fileContentType.split(Pattern.quote("/"));
                responseEntity =  ResponseEntity
                        .created(linkTo(methodOn(TextToSpeechController.class)
                                .getTextToSpeechSynthesization(inProgressSynthesis.getId())).toUri())
                        .contentType(new MediaType(subType[0],subType[1]))
                        .header(HttpHeaders.CONTENT_DISPOSITION, attachment
                                + filename + "\"")
                        .body(audioResource);
            }
            else if(synthesization.getAudioEncoding().equals(AudioEncoding.MP3)) {
                filename = fileUUID+".mp3";
                fileContentType = "audio/mpeg";

                // Return output audio file
                String[] subType = fileContentType.split(Pattern.quote("/"));
                responseEntity =  ResponseEntity
                        .created(linkTo(methodOn(TextToSpeechController.class)
                                .getTextToSpeechSynthesization(inProgressSynthesis.getId())).toUri())
                        .contentType(new MediaType(subType[0],subType[1]))
                        .header(HttpHeaders.CONTENT_DISPOSITION, attachment
                                + filename + "\"")
                        .body(audioResource);
            }
            else if(synthesization.getAudioEncoding().equals(AudioEncoding.OGG_OPUS)) {
                filename = fileUUID+".opus";
                fileContentType = "audio/ogg";

                // Return output audio file
                String[] subType = fileContentType.split(Pattern.quote("/"));
                responseEntity =  ResponseEntity
                        .created(linkTo(methodOn(TextToSpeechController.class)
                                .getTextToSpeechSynthesization(inProgressSynthesis.getId())).toUri())
                        .contentType(new MediaType(subType[0],subType[1]))
                        .header(HttpHeaders.CONTENT_DISPOSITION, attachment
                                + filename + "\"")
                        .body(audioResource);
            }
            else {
                log.error("POST /babelfish/synthesis/text_to_speech/synthesizations " +
                        "audio encoding error : "+synthesization.getAudioEncoding().toString());
                inProgressSynthesis.setStatus(Status.FAILED);
                throw new TextToSpeechSynthesisFailedException(synthesization);
            }

            // Commit changes to storage before Async processes
            synthesisRepository.save(inProgressSynthesis);

            // Save audio to cloud storage
            cloudStorageService.uploadSynthesisOutputToBucket(
                    inProgressSynthesis, filename, fileContentType, audioResource);

            // Save Model to cloud storage
            String modelName = fileUUID+".json";
            String modelContentType = "application/json";
            cloudStorageService.uploadSynthesisInputsToBucket(
                            inProgressSynthesis, modelName,
                    modelContentType, new ObjectMapper().writeValueAsBytes(inProgressSynthesis));

        }
        catch (Exception ex) {
            log.error("POST /babelfish/synthesis/text_to_speech/synthesizations service error", ex);
            inProgressSynthesis.setStatus(Status.FAILED);
            synthesisRepository.save(inProgressSynthesis);
            throw new TextToSpeechSynthesisFailedException(synthesization);
        }
        return responseEntity;
    }

    private AudioFileMetaData getAudioMetaDataFromFile(byte[] fileContent) {
        AudioFileMetaData modelMetaData = new AudioFileMetaData();
        AudioFileMetaDataUtil.listAudioMetaDataFromBytes(fileContent, modelMetaData);
        return modelMetaData;
    }


}
