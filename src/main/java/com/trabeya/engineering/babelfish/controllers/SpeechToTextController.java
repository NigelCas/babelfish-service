package com.trabeya.engineering.babelfish.controllers;

import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechContext;
import com.google.cloud.storage.Blob;
import com.trabeya.engineering.babelfish.client.gcp.GoogleSpeechToTextV1Client;
import com.trabeya.engineering.babelfish.client.web.scrapper.GcpSpeechToTextLanguageSupportPage;
import com.trabeya.engineering.babelfish.controllers.assemblers.SpeechToTextSynthesisResourceAssembler;
import com.trabeya.engineering.babelfish.controllers.dtos.NewSpeechToTexV1TranscriptionRequest;
import com.trabeya.engineering.babelfish.controllers.dtos.NewSpeechToTextRemoteTranscriptionRequest;
import com.trabeya.engineering.babelfish.controllers.dtos.SpeechToTextSupportedAudioLanguagesResponse;
import com.trabeya.engineering.babelfish.exceptions.AudioDataValidationFailedException;
import com.trabeya.engineering.babelfish.exceptions.AudioFileMetaDataException;
import com.trabeya.engineering.babelfish.exceptions.BabelFishServiceSystemException;
import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisNotFoundException;
import com.trabeya.engineering.babelfish.model.AudioFileMetaData;
import com.trabeya.engineering.babelfish.model.SpeechToTextTranscription;
import com.trabeya.engineering.babelfish.model.Status;
import com.trabeya.engineering.babelfish.queue.dto.TranscriptionDto;
import com.trabeya.engineering.babelfish.repository.AudioFileMetaDataRepository;
import com.trabeya.engineering.babelfish.repository.SpeechToTextTranscriptionRepository;
import com.trabeya.engineering.babelfish.service.AsyncCloudStorageService;
import com.trabeya.engineering.babelfish.util.AudioFileMetaDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import com.trabeya.engineering.babelfish.controllers.websocket.dtos.NewRealTimeSpeechToTextV1TranscriptionDto;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RequestMapping("/speech_to_text")
@RestController
@Slf4j
public class SpeechToTextController {

    @Autowired
    private SpeechToTextSynthesisResourceAssembler speechToTextSynthesisResourceAssembler;

    @Autowired
    private AsyncCloudStorageService asyncCloudStorageService;

    @Autowired
    private GoogleSpeechToTextV1Client googleSpeechToTextV1Client;

    @Autowired
    private SpeechToTextTranscriptionRepository speechToTextTranscriptionRepository;

    @Autowired
    private AudioFileMetaDataRepository audioFileMetaDataRepository;

    @Autowired
    private GcpSpeechToTextLanguageSupportPage gcpSpeechToTextLanguageSupportPage;

    @GetMapping("/transcription/support/audio/languages")
    public Resources<Resource<SpeechToTextSupportedAudioLanguagesResponse>> gcpSpeechToTextSupportLanguages() {
        List<Resource<SpeechToTextSupportedAudioLanguagesResponse>> languages = new ArrayList<>();
        try {
            for (SpeechToTextSupportedAudioLanguagesResponse language :
                    gcpSpeechToTextLanguageSupportPage.getSupportedLanguageList()) {
                Resource<SpeechToTextSupportedAudioLanguagesResponse> languageResource = new Resource<>(language);
                languages.add(languageResource);
            }
        }
        catch(Exception ex) {
            throw new BabelFishServiceSystemException(ex.getMessage());
        }
        return new Resources<>(languages);
    }

    @PostMapping("/realtime/transcription/start")
    public ResponseEntity<TranscriptionDto> startRealTimeSpeechToTextTranscription (
            @RequestBody @Validated NewRealTimeSpeechToTextV1TranscriptionDto dto) {
        log.info("Service - startSpeechToTextTranscription() - params - {},{},{},{}",
                dto.getTargetAudioEncoding(),dto.getAudioLanguageCode(),dto.getSampleRate(),
                dto.getAudioStreamingModel());
        googleSpeechToTextV1Client.setNewRealTimeSpeechToTextV1TranscriptionDto(dto);
        log.info("Service - startSpeechToTextTranscription() completed ");
        // stopWatch = new StopWatch("Realtime transcription stopwatch - 1 minute");
        return ResponseEntity.ok(new TranscriptionDto("0","Speech to Text Transcription Started"));
    }


    @GetMapping("/transcription/{id}")
    public Resource<SpeechToTextTranscription> getTextToSpeechTranscription(@PathVariable Long id) {
        SpeechToTextTranscription transcription = null;
        try {
        transcription = speechToTextTranscriptionRepository.findById(id)
                .orElseThrow(() -> new TextToSpeechSynthesisNotFoundException(id));
        }
        catch(Exception ex) {
            throw new BabelFishServiceSystemException(ex.getMessage());
        }
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
            (@ModelAttribute @Validated NewSpeechToTextRemoteTranscriptionRequest transcription) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/transcription/v1/short/upload")
    public ResponseEntity<Resource<SpeechToTextTranscription>> startNewLocalShortAudioTranscription
            (@RequestParam(value = "file", required = true) MultipartFile file,
             @ModelAttribute @Validated NewSpeechToTexV1TranscriptionRequest transcription) {
        ResponseEntity<Resource<SpeechToTextTranscription>> transcriptionResponse = null;
        try {
            SpeechToTextTranscription speechToTextTranscription = new SpeechToTextTranscription();
            speechToTextTranscription.setStatus(Status.IN_PROGRESS);
            BeanUtils.copyProperties(transcription, speechToTextTranscription);
            String fileUUID = UUID.randomUUID().toString();

            speechToTextTranscriptionRepository.save(speechToTextTranscription);
            speechToTextTranscription.setInputFilename(fileUUID);
            byte[] inputAudio = file.getBytes();
            RecognitionConfig config = validateLocalFile(inputAudio, speechToTextTranscription);
            //TODO commit audio file to storage
            speechToTextTranscription.setOutputTranscription(
                    googleSpeechToTextV1Client.syncRecognizeFileV1(inputAudio, config));

            // TODO commit model file to storage
            // return state of translation is committed to DB
            Resource<SpeechToTextTranscription> resource
                    = speechToTextSynthesisResourceAssembler.toResource(
                    speechToTextTranscriptionRepository.save(speechToTextTranscription));

            transcriptionResponse = ResponseEntity
                    .created(new URI(resource.getId().expand().getHref()))
                    .body(resource);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return transcriptionResponse;
    }

    @PostMapping("/transcription/long")
    public Resource<SpeechToTextTranscription> startNewLongAudioTranscription
            (@RequestBody() NewSpeechToTextRemoteTranscriptionRequest transcription) {
        throw new UnsupportedOperationException();
    }

//    @PostMapping("/transcription/streaming/upload")
//    public Resource<SpeechToTextTranscription> startNewFileStreamingTranscription
//            (@RequestBody() NewSpeechToTexV1TranscriptionRequest transcription) {
//
//    @PostMapping("/transcription/streaming")
//    public Resource<SpeechToTextTranscription> startNewFileStreamingTranscription
//            (@RequestBody() NewSpeechToTexV1TranscriptionRequest transcription) {
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

    private RecognitionConfig validateLocalFile(byte[] localAudioData,
                                                SpeechToTextTranscription transcriptionRequest) {
        try {
            RecognitionConfig.Builder recognitionBuilder = RecognitionConfig.newBuilder();
            AudioFileMetaData detectedMetaData = audioFileMetaDataRepository.save(
                    AudioFileMetaDataUtil.listAudioMetaDataFromBytes(localAudioData));
            transcriptionRequest.setDetectedAudioMetaData(detectedMetaData);

            log.info("---------------- Audio Validation Start ----------------");
            log.info("Validating Gcs downloaded audio | contentType: " + detectedMetaData.getContentType());

            // validate audio file track length
            if ((detectedMetaData.getTrackLengthIso().compareTo(
                    LocalTime.parse("00:01:00", DateTimeFormatter.ISO_TIME)) > 0)) {
                throw new AudioDataValidationFailedException("Audio track length exceeds Api limits");
            } else {
                log.info("Audio track length within Api limits");
            }

            // language code
            recognitionBuilder.setLanguageCode(transcriptionRequest.getAudioLanguageCode());

            // validate content types for max accuracy LINEAR16 or FLAC
            if (detectedMetaData.getContentType().contains("wav")) {
                recognitionBuilder.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16);
                transcriptionRequest.setAudioEncoding(RecognitionConfig.AudioEncoding.LINEAR16);
                transcriptionRequest.setInputFilename(transcriptionRequest.getInputFilename() + ".wav");
            } else if (detectedMetaData.getContentType().contains("flac")) {
                transcriptionRequest.setAudioEncoding(RecognitionConfig.AudioEncoding.FLAC);
                recognitionBuilder.setEncoding(RecognitionConfig.AudioEncoding.FLAC);
                transcriptionRequest.setInputFilename(transcriptionRequest.getInputFilename() + ".flac");
            }
            // TODO for other supported types
            else {
                throw new AudioDataValidationFailedException(
                        "Not supported content type :" + detectedMetaData.getContentType());
            }

            // validate sample rate for all content types
            if (Integer.parseInt(detectedMetaData.getSampleRateHz()) >= 8000
                    && Integer.parseInt(detectedMetaData.getSampleRateHz()) <= 48000) {
                if (Integer.parseInt(detectedMetaData.getSampleRateHz()) == 16000) {
                    log.info("Audio sample rate optimal at: " + 16000);
                    recognitionBuilder.setSampleRateHertz(16000);
                    transcriptionRequest.setSampleRate(16000);
                }
                log.info("Audio sample rate not optimal at: "
                        + Integer.parseInt(detectedMetaData.getSampleRateHz()));
                recognitionBuilder.setSampleRateHertz(
                        Integer.parseInt(detectedMetaData.getSampleRateHz()));
                transcriptionRequest.setSampleRate(Integer.parseInt(detectedMetaData.getSampleRateHz()));
            } else {
                throw new AudioDataValidationFailedException("Audio sample rate not valid");
            }

            // validate channel output
            if (transcriptionRequest.isEnableSeparateRecognitionPerChannel()) {
                if (Integer.parseInt(detectedMetaData.getChannels()) > 1) {
                    recognitionBuilder.setEnableSeparateRecognitionPerChannel(true);
                    switch (detectedMetaData.getContentType()) {
                        case "audio/wave":
                        case "audio/wav":
                        case "audio/vnd.wave":
                        case "audio/flac":
                            if (Integer.parseInt(detectedMetaData.getChannels()) >= 1
                                    && Integer.parseInt(detectedMetaData.getChannels()) <= 8) {
                                log.info(Integer.parseInt(detectedMetaData.getChannels()) + " channel | " +
                                        detectedMetaData.getContentType() +
                                        " SeparateRecognitionPerChannel enabled");
                                recognitionBuilder.setAudioChannelCount(
                                        Integer.parseInt(detectedMetaData.getChannels()));
                            } else {
                                throw new AudioDataValidationFailedException(
                                        "Audio SeparateRecognitionPerChannel disabled, Invalid channels");
                            }
                            break;
                        // TODO Valid values for OGG_OPUS are '1'-'254'.
                        // TODO Valid value for MULAW, AMR, AMR_WB and SPEEX_WITH_HEADER_BYTE is only 1.
                        default:
                            throw new AudioDataValidationFailedException(
                                    "Audio SeparateRecognitionPerChannel disabled");
                    }
                } else {
                    throw new AudioDataValidationFailedException(
                            "Unable to perform SeparateRecognitionPerChannel, channels <= 1 ");
                }
            } else {
                log.info("Audio SeparateRecognitionPerChannel disabled. defaults to one channel (mono)");
            }

            // validate profanity
            if (transcriptionRequest.isProfanityFilterEnabled()) {
                log.info("Audio ProfanityFilter enabled");
                recognitionBuilder.setProfanityFilter(true);
            } else {
                log.info("Audio ProfanityFilter disabled");
                recognitionBuilder.setProfanityFilter(false);
            }

            // validate speech contexts
//            if ((null != transcriptionRequest.getSpeechContextsHints()) &&
//                    (transcriptionRequest.getSpeechContextsHints().split(",").length > 0)) {
//                // Add helpful speech hints to increase accuracy
//
//                SpeechContext sc = SpeechContext.newBuilder()
//                        .addAllPhrases(Arrays.asList(
//                                transcriptionRequest.getSpeechContextsHints().split(",")))
//                        .build();
//                recognitionBuilder.setSpeechContexts(0, sc);
//                log.info("Audio Speech Context enabled, phrases :"
//                        + transcriptionRequest.getSpeechContextsHints());
//            } else {
//                log.info("Audio Speech Context disabled");
//            }

            // validate recognition model
            if (null != transcriptionRequest.getAudioStreamingModel()
                    && !transcriptionRequest.getAudioStreamingModel().toString().trim().isEmpty()) {
                recognitionBuilder.setModel(transcriptionRequest.getAudioStreamingModel()
                        .toString().toLowerCase());
                log.info("Audio Speech model set to :" + transcriptionRequest.getAudioStreamingModel().name());
            } else {
                log.info("Audio Speech model not specified");
            }

            // validate using enhanced models
            if (transcriptionRequest.isEnhancedEnabled()) {
                log.info("Audio enhanced model use enabled");
                recognitionBuilder.setUseEnhanced(true);
            } else {
                log.info("Audio enhanced model use disabled");
                recognitionBuilder.setUseEnhanced(false);
            }
            log.info("---------------- Audio Validation Finish ----------------");
            return recognitionBuilder.build();
        } catch (AudioFileMetaDataException audEx) {
            log.error("Unable to read audio metadata from Gcs download", audEx);
            throw new AudioDataValidationFailedException(audEx.getMessage());
        } catch (Exception unEx) {
            log.error("isGcsRemoteFileValid Unknown error : ", unEx);
            throw new AudioDataValidationFailedException(unEx.getMessage());
        }
    }


    private void validateGcsRemoteFile(NewSpeechToTextRemoteTranscriptionRequest transcriptionRequest,
                                       RecognitionConfig config) {
        try {
            RecognitionConfig.Builder recognitionBuilder = RecognitionConfig.newBuilder();

            // Downloads from cloud storage and blocks till download is completed/failed
            Blob downloadedBlob = (Blob) CompletableFuture.anyOf(
                    asyncCloudStorageService.downloadTranscriptionInputFromBucket(
                            transcriptionRequest.getRemoteURI())).join();

            AudioFileMetaData detectedMetaData =
                    AudioFileMetaDataUtil.listAudioMetaDataFromBytes(downloadedBlob.getContent());

            if (null != detectedMetaData) {
                log.info("Validating Gcs downloaded audio | contentType: " + detectedMetaData.getContentType());

                // validate audio file track length
                if ((detectedMetaData.getTrackLengthIso().compareTo(
                        LocalTime.parse("05:20:00", DateTimeFormatter.ISO_TIME)) > 0)) {
                    throw new AudioDataValidationFailedException("Audio track length exceeds Api limits");
                } else {
                    log.info("Audio track length within Api limits");
                }


                // validate content types for max accuracy LINEAR16 or FLAC
                if (detectedMetaData.getContentType().contains("wav")) {
                    recognitionBuilder.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16);
                } else if (detectedMetaData.getContentType().contains("flac")) {
                    recognitionBuilder.setEncoding(RecognitionConfig.AudioEncoding.FLAC);
                }
                // TODO for other supported types
                else {
                    throw new AudioDataValidationFailedException(
                            "Not supported content type :" + detectedMetaData.getContentType());
                }

                // validate sample rate for all content types
                if (Integer.parseInt(detectedMetaData.getBitRateKbps()) >= 8000
                        && Integer.parseInt(detectedMetaData.getBitRateKbps()) <= 48000) {
                    if (Integer.parseInt(detectedMetaData.getBitRateKbps()) == 16000) {
                        log.info("Audio sample rate optimal at: " + 16000);
                        recognitionBuilder.setSampleRateHertz(16000);
                    }
                    log.info("Audio sample rate not optimal at: "
                            + Integer.parseInt(detectedMetaData.getBitRateKbps()));
                    recognitionBuilder.setSampleRateHertz(
                            Integer.parseInt(detectedMetaData.getBitRateKbps()));
                } else {
                    throw new AudioDataValidationFailedException("Audio sample rate not valid");
                }

                // validate channel output
                if (transcriptionRequest.isEnableSeparateRecognitionPerChannel()) {
                    if (Integer.parseInt(detectedMetaData.getChannels()) > 1) {
                        recognitionBuilder.setEnableSeparateRecognitionPerChannel(true);
                        switch (detectedMetaData.getContentType()) {
                            case "audio/wave":
                            case "audio/wav":
                            case "audio/vnd.wave":
                            case "audio/flac":
                                if (Integer.parseInt(detectedMetaData.getChannels()) >= 1
                                        && Integer.parseInt(detectedMetaData.getChannels()) <= 8) {
                                    log.info(Integer.parseInt(detectedMetaData.getChannels()) + " channel | " +
                                            detectedMetaData.getContentType() +
                                            " SeparateRecognitionPerChannel enabled");
                                    recognitionBuilder.setAudioChannelCount(
                                            Integer.parseInt(detectedMetaData.getChannels()));
                                } else {
                                    throw new AudioDataValidationFailedException(
                                            "Audio SeparateRecognitionPerChannel disabled, Invalid channels");
                                }
                                break;
                            // TODO Valid values for OGG_OPUS are '1'-'254'.
                            // TODO Valid value for MULAW, AMR, AMR_WB and SPEEX_WITH_HEADER_BYTE is only 1.
                            default:
                                throw new AudioDataValidationFailedException(
                                        "Audio SeparateRecognitionPerChannel disabled");
                        }
                    } else {
                        throw new AudioDataValidationFailedException(
                                "Unable to perform SeparateRecognitionPerChannel, channels <= 1 ");
                    }
                } else {
                    log.info("Audio SeparateRecognitionPerChannel disabled. defaults to one channel (mono)");
                }

                // validate profanity
                if (transcriptionRequest.isProfanityFilterEnabled()) {
                    log.info("Audio ProfanityFilter enabled");
                    recognitionBuilder.setProfanityFilter(true);
                } else {
                    log.info("Audio ProfanityFilter disabled");
                    recognitionBuilder.setProfanityFilter(false);
                }

                // validate speech contexts
                if ((null != transcriptionRequest.getSpeechContextsHints()) &&
                        (transcriptionRequest.getSpeechContextsHints().size() > 0)) {

                    // TODO speech context boost
                    SpeechContext sc = SpeechContext.newBuilder()
                            .addAllPhrases(transcriptionRequest.getSpeechContextsHints())
                            .build();
                    recognitionBuilder.setSpeechContexts(0, sc);
                    log.info("Audio Speech Context enabled, phrases :"
                            + Arrays.toString(transcriptionRequest.getSpeechContextsHints().toArray()));
                } else {
                    log.info("Audio Speech Context disabled");
                }

                // validate recognition model
                if (null != transcriptionRequest.getAudioStreamingModel()
                        && !transcriptionRequest.getAudioStreamingModel().toString().trim().isEmpty()) {
                    recognitionBuilder.setModel(transcriptionRequest.getAudioStreamingModel()
                            .toString().toLowerCase());
                    log.info("Audio Speech model set to :" + transcriptionRequest.getAudioStreamingModel().name());
                } else {
                    log.info("Audio Speech model not specified");
                }

                // validate using enhanced models
                if (transcriptionRequest.isEnhancedEnabled()) {
                    log.info("Audio enhanced model use enabled");
                    recognitionBuilder.setUseEnhanced(true);
                } else {
                    log.info("Audio enhanced model use disabled");
                    recognitionBuilder.setUseEnhanced(false);
                }
            } else {
                String errorLog = "Unable to validate Audio File";
                log.error(errorLog);
                throw new AudioDataValidationFailedException(errorLog);
            }

        } catch (CompletionException ex) {
            log.error("Unable to complete Gcs download", ex);
            throw new AudioDataValidationFailedException(ex.getMessage());
        } catch (AudioFileMetaDataException audEx) {
            log.error("Unable to read audio metadata from Gcs download", audEx);
            throw new AudioDataValidationFailedException(audEx.getMessage());
        } catch (Exception unEx) {
            log.error("isGcsRemoteFileValid Unknown error : ", unEx);
            throw new AudioDataValidationFailedException(unEx.getMessage());
        }
    }


}


