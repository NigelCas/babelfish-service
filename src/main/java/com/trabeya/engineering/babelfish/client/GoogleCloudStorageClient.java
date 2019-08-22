package com.trabeya.engineering.babelfish.client;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Storage.*;
import com.trabeya.engineering.babelfish.exceptions.GoogleCloudStorageAPIException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Data
@Slf4j
public class GoogleCloudStorageClient {

    public Page<Bucket> listBuckets(String searchPrefix, int pageSize) {
        Page<Bucket> buckets;
        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            buckets = storage.list(
                        BucketListOption.pageSize(pageSize),
                        BucketListOption.prefix(searchPrefix));
            int foundBucketCount = 0;
            for (Bucket bucket : buckets.iterateAll()) {
                // do something with the bucket
                log.info("---- Bucket Information [" + foundBucketCount + "] ---- \n" +
                        "E tag : " + bucket.getEtag() + "\n" +
                        "ID : " + bucket.getGeneratedId() + "\n" +
                        "Name : " + bucket.getName() + "\n" +
                        "URI :" + bucket.getLocation());
                ++foundBucketCount;
            }
            log.info("Buckets prefixed under \"babelfish\" found : " + foundBucketCount);
        }
        catch (Exception ex) {
            log.error("listBuckets service exception : ", ex);
            throw new GoogleCloudStorageAPIException(ex.getMessage());
        }
        return buckets;
    }


    public Page<Blob> listBlobsInBucketDirectory(String bucketName, String blobsDirectory, int pageSize) {
        Page<Blob> blobs;
        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            blobs = storage.list(
                            bucketName,
                            BlobListOption.pageSize(pageSize),
                            BlobListOption.currentDirectory(),
                            BlobListOption.prefix(blobsDirectory));
            int foundBlobCount = 0;
            for (Blob blob : blobs.iterateAll()) {
                // do something with the blob
                ++foundBlobCount;
            }
            log.info(bucketName+", Blobs prefixed under \""+blobsDirectory+"\" found : " + foundBlobCount);
        }
        catch (Exception ex) {
            log.error("listBlobsInBucketDirectory service exception : ", ex);
            throw new GoogleCloudStorageAPIException(ex.getMessage());
        }
        return blobs;
    }

    public Blob downloadBlobFromBucketDirectory(String bucketName, String blobPath) {
        Blob blob;
        try {
            log.info("Downloading "+blobPath+" from "+bucketName+"....");
            // Instantiate a Google Cloud Storage client
            Storage storage = StorageOptions.getDefaultInstance().getService();

            // Get specific file from specified bucket
            blob = storage.get(BlobId.of(bucketName, blobPath));
            if(null!=blob)
                log.info("Successfully Downloaded "+blob.getEtag());
            else
                log.error("Download Failed, file not found: "+blobPath);
        }
        catch (Exception ex) {
            log.error("getBlobInBucketDirectory service exception : ", ex);
            throw new GoogleCloudStorageAPIException(ex.getMessage());
        }
        return blob;
    }


    public Blob uploadBlobToBucketDirectory(BlobInfo blob, byte[] bytes) {
        Blob uploadedBlob;
        try {
            log.info("Uploading "+blob.getName()+" to "+blob.getBucket()+"....");
            Storage storage = StorageOptions.getDefaultInstance().getService();
            uploadedBlob = storage.create(blob, bytes);
        }
        catch (Exception ex) {
            log.error("uploadBlobToBucketDirectory service exception : ", ex);
            throw new GoogleCloudStorageAPIException(ex.getMessage());
        }
        return uploadedBlob;
    }

}
