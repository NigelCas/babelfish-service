package com.trabeya.engineering.babelfish.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.trabeya.engineering.babelfish.client.GoogleCloudStorageClient;
import com.trabeya.engineering.babelfish.exceptions.GoogleCloudStorageFailedException;
import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesis;
import com.trabeya.engineering.babelfish.repository.TextToSpeechSynthesisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.trabeya.engineering.babelfish.Application.*;

@Service
@Slf4j
public class CloudStorageService {

    @Autowired
    private TextToSpeechSynthesisRepository synthesisRepository;

    @Autowired
    private GoogleCloudStorageClient cloudStorageClient;

    @Async("threadPoolTaskExecutor")
    public void uploadSynthesisOutputToBucket
            (TextToSpeechSynthesis synthesis, String fileUri, String contentType, byte[] fileContent) {
        try {
            CompletableFuture<Blob> futureFile =
                CompletableFuture.supplyAsync(() ->
                    uploadToCloudStorageBucket
                    (GOOGLE_TEXT_TO_SPEECH_OUTPUT_URI+fileUri, contentType, fileContent)
            );

            futureFile.thenApply(blob -> {
                log.info("Successfully uploaded synthesized audio, access link: " + blob.getMediaLink());
                synthesis.setAudioFilename(blob.getName());
                synthesis.setAudioFileUri(blob.getMediaLink());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synthesisRepository.save(synthesis);
                return null;
            });
        }
        catch (Exception ex) {
            log.error("uploadToCloudStorageBucket service error: ", ex);
            throw new GoogleCloudStorageFailedException("Google cloud storage upload failed : "
                    +ex.getMessage());
        }
    }

    @Async("threadPoolTaskExecutor")
    public void uploadSynthesisInputsToBucket
            (TextToSpeechSynthesis synthesis, String fileName, String contentType, byte[] fileContent) {
        try {
            CompletableFuture<Blob> futureModel =
                CompletableFuture.supplyAsync(() ->
                    uploadToCloudStorageBucket
                    (GOOGLE_TEXT_TO_SPEECH_INPUT_URI+fileName, contentType, fileContent)
                );

            futureModel.thenApply(blob -> {
                log.info("Successfully uploaded pre synthesis text, access link: "+blob.getMediaLink());
                synthesis.setRemoteModelFilename(blob.getName());
                synthesis.setRemoteModelUri(blob.getMediaLink());
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synthesisRepository.save(synthesis);
                return null;
            });
        }
        catch (Exception ex) {
            log.error("uploadSynthesisInputsToBucket service error: ", ex);
            throw new GoogleCloudStorageFailedException("Google cloud storage upload failed : "
                    +ex.getMessage());
        }
    }




    private Blob uploadToCloudStorageBucket(String fileUri, String contentType, byte[] fileContent) {
        BlobId blobId = BlobId.of(GOOGLE_CLOUD_STORAGE_BUCKET_NAME, fileUri);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        return cloudStorageClient.uploadBlobToBucketDirectory(blobInfo, fileContent);
    }


}
