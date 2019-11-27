package com.trabeya.engineering.babelfish.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.Voice;
import com.trabeya.engineering.babelfish.client.gcp.GoogleTextToSpeechClient;
import com.trabeya.engineering.babelfish.controllers.dtos.NewSsmlToSpeechSynthesisRequest;
import com.trabeya.engineering.babelfish.controllers.dtos.NewTextToSpeechSynthesisRequest;
import com.trabeya.engineering.babelfish.controllers.dtos.TextToSpeechSupportedVoicesResponse;
import com.trabeya.engineering.babelfish.exceptions.SsmlToSpeechSynthesisFailedException;
import com.trabeya.engineering.babelfish.exceptions.SsmlToSpeechSynthesisSsmlInvalidException;
import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisFailedException;
import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisNotFoundException;
import com.trabeya.engineering.babelfish.model.*;
import com.trabeya.engineering.babelfish.repository.AudioFileMetaDataRepository;
import com.trabeya.engineering.babelfish.repository.TextToSpeechSynthesisRepository;
import com.trabeya.engineering.babelfish.service.AsyncCloudStorageService;
import com.trabeya.engineering.babelfish.util.AudioFileMetaDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequestMapping("/text_to_speech")
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
    private AsyncCloudStorageService asyncCloudStorageService;

    private static final String attachment  = "attachment; filename=\"";

    @PostMapping("/test/text")
    public ResponseEntity<byte[]> testText(@RequestBody String input) {
        byte[] resource = textToSpeechClient.synthesizeSpeechV1(
                input, false,"en-US-Standard-C", "en-US",
                SsmlVoiceGender.FEMALE, AudioEncoding.OGG_OPUS,
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
    public CollectionModel<EntityModel<TextToSpeechSupportedVoicesResponse>> getAllSupportedV1Voices() {
        List<EntityModel<TextToSpeechSupportedVoicesResponse>> voices = new ArrayList<>();
        for (Voice voice : textToSpeechClient.getSupportedVoiceList()) {
            TextToSpeechSupportedVoicesResponse supportedVoice = new TextToSpeechSupportedVoicesResponse();
            supportedVoice.setLanguageCodes(voice.getLanguageCodesList().toArray());
            supportedVoice.setName(voice.getName());
            supportedVoice.setSsmlGender(voice.getSsmlGender());
            supportedVoice.setNaturalSampleRateHertz(voice.getNaturalSampleRateHertz());
            EntityModel<TextToSpeechSupportedVoicesResponse> languageResource = new EntityModel<>(supportedVoice);
            voices.add(languageResource);
        }
        return new CollectionModel<>(voices);
    }

    @GetMapping("/synthesization/{id}")
    public EntityModel<TextToSpeechSynthesis> getTextToSpeechSynthesization(@PathVariable Long id) {
        TextToSpeechSynthesis synthesization = synthesisRepository.findById(id)
                .orElseThrow(() -> new TextToSpeechSynthesisNotFoundException(id));
        return new EntityModel<>(synthesization,
                linkTo(methodOn(TextToSpeechController.class).getTextToSpeechSynthesization(id)).withSelfRel(),
                linkTo(methodOn(TextToSpeechController.class).getAllTextToSpeechSynthesizations())
                        .withRel("text-to-speech-synthesizations"));
    }


    @GetMapping("/synthesizations")
    public CollectionModel<EntityModel<TextToSpeechSynthesis>> getAllTextToSpeechSynthesizations() {
        List<EntityModel<TextToSpeechSynthesis>> synthesizations = synthesisRepository.findAll().stream()
                .map(synthesization -> new EntityModel<>(synthesization,
                        linkTo(methodOn(TextToSpeechController.class)
                                .getTextToSpeechSynthesization(synthesization.getId())).withSelfRel(),
                        linkTo(methodOn(TextToSpeechController.class)
                                .getAllTextToSpeechSynthesizations()).withRel("text-to-speech-synthesizations")))
                .collect(Collectors.toList());
        return new CollectionModel<>(synthesizations,
                linkTo(methodOn(TextToSpeechController.class).getAllTextToSpeechSynthesizations()).withSelfRel());
    }

    @PostMapping("/synthesizations/ssml")
    public ResponseEntity<byte[]> newSynthesizationSSML(
            @ModelAttribute @Validated NewSsmlToSpeechSynthesisRequest synthesization,
            @RequestParam(value = "file", required = true) MultipartFile file) {
        ResponseEntity<byte[]> responseEntity;
        TextToSpeechSynthesis inProgressSynthesis = new TextToSpeechSynthesis();
        inProgressSynthesis.setInputDataType(TextToSpeechTextType.SSML);
        inProgressSynthesis.setStatus(Status.IN_PROGRESS);
        byte[] audioResource = new byte[0];
        try {
            String ssml = new String(file.getBytes());
            if(!ssml.trim().isEmpty()) {
                inProgressSynthesis.setInputData(ssml);
                // Initial Pending state save
                BeanUtils.copyProperties(synthesization, inProgressSynthesis);
                inProgressSynthesis = synthesisRepository.save(inProgressSynthesis);
                String byteUnit = " Bytes";
                if ((null != synthesization.getAudioSpeakingRate())
                        && (null == synthesization.getPitch())) {
                    audioResource = ssmlToSpeechSynthesizerService(ssml, synthesization,inProgressSynthesis,
                            synthesization.getAudioSpeakingRate().doubleValue(), 0.0);
                    inProgressSynthesis.setStatus(Status.COMPLETED);
                    log.info("Output audio(custom user speaking rate) byte length :"
                            + audioResource.length + byteUnit);
                } else if ((null != synthesization.getPitch())
                        && (null == synthesization.getAudioSpeakingRate())) {
                    audioResource = ssmlToSpeechSynthesizerService(ssml, synthesization,inProgressSynthesis,
                            1.0, synthesization.getPitch().doubleValue());
                    inProgressSynthesis.setStatus(Status.COMPLETED);
                    log.info("Output audio(custom user pitch) byte length :"
                            + audioResource.length + byteUnit);
                } else if ((null != synthesization.getPitch())
                        && (null != synthesization.getAudioSpeakingRate())) {
                    audioResource = ssmlToSpeechSynthesizerService(ssml, synthesization,inProgressSynthesis,
                            synthesization.getAudioSpeakingRate().doubleValue(),
                            synthesization.getPitch().doubleValue());
                    inProgressSynthesis.setStatus(Status.COMPLETED);
                    log.info("Output audio(custom user pitch & speaking rate) byte length :"
                            + audioResource.length + byteUnit);
                } else {
                    audioResource = ssmlToSpeechSynthesizerService(ssml, synthesization, inProgressSynthesis,
                            1.0, 0);
                    inProgressSynthesis.setStatus(Status.COMPLETED);
                    log.info("Output audio(default pitch & speaking rate) byte length :"
                            + audioResource.length + byteUnit);
                }
            }
            else {
                log.error("POST /babelfish/synthesis/text_to_speech/synthesizations/ssml " +
                        "input ssml error : "+ssml);
                throw new SsmlToSpeechSynthesisSsmlInvalidException("Input SSML File Invalid!");
            }

            // Get Audio metadata
            inProgressSynthesis.setDetectedAudioMetaData(
                    audioFileMetaDataRepository.save(
                            AudioFileMetaDataUtil.listAudioMetaDataFromBytes(audioResource)));

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
                throw new SsmlToSpeechSynthesisFailedException(synthesization);
            }

            // Commit changes to storage before Async processes
            synthesisRepository.save(inProgressSynthesis);

            // Save audio to cloud storage
            asyncCloudStorageService.uploadSynthesisOutputToBucket(
                    inProgressSynthesis, filename, fileContentType, audioResource);

            // Save Model to cloud storage
            String modelName = fileUUID+".json";
            String modelContentType = "application/json";
            asyncCloudStorageService.uploadSynthesisInputsToBucket(
                    inProgressSynthesis, modelName,
                    modelContentType, new ObjectMapper().writeValueAsBytes(inProgressSynthesis));

        }
        catch (Exception ex) {
            log.error("POST /babelfish/synthesis/text_to_speech/synthesizations service error", ex);
            inProgressSynthesis.setStatus(Status.FAILED);
            synthesisRepository.save(inProgressSynthesis);
            throw new SsmlToSpeechSynthesisFailedException(synthesization);
        }
        return responseEntity;

    }


    @PostMapping("/synthesizations")
    public ResponseEntity<byte[]> newSynthesization(@RequestBody @Validated NewTextToSpeechSynthesisRequest synthesization) {
        ResponseEntity<byte[]> responseEntity;
        TextToSpeechSynthesis inProgressSynthesis = new TextToSpeechSynthesis();
        inProgressSynthesis.setInputDataType(TextToSpeechTextType.TEXT);
        inProgressSynthesis.setStatus(Status.IN_PROGRESS);
        byte[] audioResource;
        try{
            // Initial Pending state save
            BeanUtils.copyProperties(synthesization, inProgressSynthesis);
            inProgressSynthesis = synthesisRepository.save(inProgressSynthesis);
            String byteUnit = " Bytes";

            //
            if( (null!=synthesization.getAudioSpeakingRate())
                && (null==synthesization.getPitch())) {
                audioResource = textToSpeechSynthesizerService(synthesization,inProgressSynthesis,
                        synthesization.getAudioSpeakingRate().doubleValue(),0.0);
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(custom user speaking rate) byte length :"
                        +audioResource.length+byteUnit);
            }
            else if( (null!=synthesization.getPitch())
                    && (null==synthesization.getAudioSpeakingRate())) {
                audioResource = textToSpeechSynthesizerService(synthesization,inProgressSynthesis,
                        1.0,synthesization.getPitch().doubleValue());
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(custom user pitch) byte length :"
                        +audioResource.length+byteUnit);
            }
            else if( (null!=synthesization.getPitch())
                    && (null!=synthesization.getAudioSpeakingRate())) {
                audioResource = textToSpeechSynthesizerService(synthesization,inProgressSynthesis,
                        synthesization.getAudioSpeakingRate().doubleValue(),
                        synthesization.getPitch().doubleValue());
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(custom user pitch & speaking rate) byte length :"
                        +audioResource.length+byteUnit);
            }
            else {
                audioResource = textToSpeechSynthesizerService(synthesization,inProgressSynthesis,
                        1.0, 0);
                inProgressSynthesis.setStatus(Status.COMPLETED);
                log.info("Output audio(default pitch & speaking rate) byte length :"
                        +audioResource.length+byteUnit);
            }

            // Get Audio metadata
            inProgressSynthesis.setDetectedAudioMetaData(
                    audioFileMetaDataRepository.save(
                            AudioFileMetaDataUtil.listAudioMetaDataFromBytes(audioResource)));

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
            asyncCloudStorageService.uploadSynthesisOutputToBucket(
                    inProgressSynthesis, filename, fileContentType, audioResource);

            // Save Model to cloud storage
            String modelName = fileUUID+".json";
            String modelContentType = "application/json";
            asyncCloudStorageService.uploadSynthesisInputsToBucket(
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

    private byte[] textToSpeechSynthesizerService(NewTextToSpeechSynthesisRequest synthesization,
                                                  TextToSpeechSynthesis inProgressSynthesis,
                                                  double speakingRate, double pitch) {
        byte[] responseSpeech;
        boolean voiceNamePresent = false;
        if(null!=synthesization.getVoiceLanguageName() &&
                !synthesization.getVoiceLanguageName().trim().isEmpty()) {
            for (Voice voice : textToSpeechClient.getSupportedVoiceList()) {
                if (voice.getName().equalsIgnoreCase(synthesization.getVoiceLanguageName())) {
                    voiceNamePresent = true;
                    break;
                }
            }
        }
        if(voiceNamePresent) {
            responseSpeech = textToSpeechClient.synthesizeSpeechV1(
                    synthesization.getInputData(),
                    false,
                    synthesization.getVoiceLanguageName(),
                    synthesization.getVoiceLanguageCode(),
                    synthesization.getVoiceGender(),
                    synthesization.getAudioEncoding(),
                    synthesization.getAudioDeviceProfile(),
                    speakingRate, pitch);
            inProgressSynthesis.setVoiceLanguageName(synthesization.getVoiceLanguageName());
        }
        else {
            responseSpeech = textToSpeechClient.synthesizeSpeechV1(
                    synthesization.getInputData(),
                    false,
                    null,
                    synthesization.getVoiceLanguageCode(),
                    synthesization.getVoiceGender(),
                    synthesization.getAudioEncoding(),
                    synthesization.getAudioDeviceProfile(),
                    speakingRate, pitch);
        }
        return responseSpeech;
    }

    private byte[] ssmlToSpeechSynthesizerService(String ssml, NewSsmlToSpeechSynthesisRequest synthesization,
                                                  TextToSpeechSynthesis inProgressSynthesis,
                                                  double speakingRate, double pitch) {
        byte[] responseSpeech;
        boolean voiceNamePresent = false;
        if(null!=synthesization.getVoiceLanguageName()) {
            for (Voice voice : textToSpeechClient.getSupportedVoiceList()) {
                if (voice.getName().equalsIgnoreCase(synthesization.getVoiceLanguageName())) {
                    voiceNamePresent = true;
                    break;
                }
            }
        }
        if(voiceNamePresent) {
            responseSpeech = textToSpeechClient.synthesizeSpeechV1(
                    ssml,
                    true,
                    synthesization.getVoiceLanguageName(),
                    synthesization.getVoiceLanguageCode(),
                    synthesization.getVoiceGender(),
                    synthesization.getAudioEncoding(),
                    synthesization.getAudioDeviceProfile(),
                    speakingRate, pitch);
            inProgressSynthesis.setVoiceLanguageName(synthesization.getVoiceLanguageName());
        }
        else {
            responseSpeech = textToSpeechClient.synthesizeSpeechV1(
                    ssml,
                    true,
                    null,
                    synthesization.getVoiceLanguageCode(),
                    synthesization.getVoiceGender(),
                    synthesization.getAudioEncoding(),
                    synthesization.getAudioDeviceProfile(),
                    speakingRate, pitch);
        }
        return responseSpeech;
    }

}
