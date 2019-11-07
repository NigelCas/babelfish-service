package com.trabeya.engineering.babelfish.client;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.storage.Blob;
import com.trabeya.engineering.babelfish.client.gcp.GoogleCloudStorageClient;
import com.trabeya.engineering.babelfish.client.gcp.GoogleSpeechToTextV1Client;
import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@SpringBootTest
@Slf4j
public class GoogleSpeechToTextV1ClientTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private GoogleCloudStorageClient googleCloudStorageClient;
    @Autowired
    private GoogleSpeechToTextV1Client googleSpeechToTextV1Client;


    @BeforeSuite
    public void setupAll() {
    }

    @BeforeMethod
    public void setUp() {

    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testStreamingRealtimeRecognizeLocalV1Init() {

//        Blob sampleAudio = googleCloudStorageClient.downloadBlobFromBucketDirectory
//                ("cloud-samples-tests", "speech/brooklyn.flac");
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        googleSpeechToTextV1Client.streamingRealtimeRecognizeLocalV1Init(
//                RecognitionConfig.AudioEncoding.FLAC,
//                "en-US",
//                16000,
//                SpeechToTextStreamingModel.DEFAULT,
//                false,
//                false);
//        byte[] blobBytes = sampleAudio.getContent();
//        String base64BlobString = Base64.getEncoder().encodeToString(blobBytes);
//        byte[] decodedByte = Base64.getDecoder().decode(base64BlobString);
//        googleSpeechToTextV1Client.streamingRealtimeRecognizeLocalV1Transmit(decodedByte);
//        try {
//            Thread.sleep(20000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        googleSpeechToTextV1Client.streamingRealtimeRecognizeLocalV1Complete();
    }

    @Test(dependsOnMethods = { "testStreamingRealtimeRecognizeLocalV1Init" })
    public void testStreamingRealtimeRecognizeLocalV1Transmit() {

    }

    @Test(dependsOnMethods = { "testStreamingRealtimeRecognizeLocalV1Transmit" })
    public void testStreamingRealtimeRecognizeLocalV1Complete() {

    }

    @Test
    public void testStreamingRecognizeLocalV1() {
//        Blob sampleAudio = googleCloudStorageClient.downloadBlobFromBucketDirectory
//                ("cloud-samples-tests", "speech/brooklyn.flac");
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Path path = Paths.get("/home/nigel/textToSpeech/test.flac");
        byte[] data = null;
        try {
            data = Files.readAllBytes(path);
            googleSpeechToTextV1Client.syncRecognizeFile(data,
                    RecognitionConfig.AudioEncoding.FLAC,
                    16000,
                    "en-US",
                    SpeechToTextStreamingModel.DEFAULT,
                    false,
                    false);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ApiStreamObserver<StreamingRecognizeRequest> requestObserver =
//                googleSpeechToTextV1Client.streamingRecognizeLocalV1Init(
//                RecognitionConfig.AudioEncoding.FLAC,
//                "en-US",
//                16000,
//                SpeechToTextStreamingModel.DEFAULT
//        );
////        byte[] blobBytes = sampleAudio.getContent();
////        String base64BlobString = Base64.getEncoder().encodeToString(blobBytes);
////        byte[] decodedByte = Base64.getDecoder().decode(base64BlobString);
//
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        String transcript = googleSpeechToTextV1Client.streamingRecognizeLocalTranscript(requestObserver);
//        log.info(transcript);
    }


}