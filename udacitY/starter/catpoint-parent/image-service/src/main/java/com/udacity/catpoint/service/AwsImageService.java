package com.udacity.catpoint.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Image Recognition Service that can identify cats. Requires aws credentials to be entered in config.properties to work.
 */
public class AwsImageService implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(AwsImageService.class);
    private static RekognitionClient rekognitionClient;

    // Static initializer for thread-safe initialization
    static {
        initializeRekognitionClient();
    }

    private static synchronized void initializeRekognitionClient() {
        if (rekognitionClient != null) {
            return; // Already initialized
        }

        Properties props = new Properties();
        try (InputStream is = AwsImageService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
                String awsId = props.getProperty("aws.id");
                String awsSecret = props.getProperty("aws.secret");
                String awsRegion = props.getProperty("aws.region");

                if (awsId != null && awsSecret != null && awsRegion != null) {
                    AwsCredentials awsCredentials = AwsBasicCredentials.create(awsId, awsSecret);
                    rekognitionClient = RekognitionClient.builder()
                            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                            .region(Region.of(awsRegion))
                            .build();
                    log.info("AWS Rekognition client initialized successfully");
                } else {
                    log.error("Missing AWS credentials in config.properties");
                }
            } else {
                log.error("config.properties file not found");
            }
        } catch (IOException ioe) {
            log.error("Unable to initialize AWS Rekognition", ioe);
        }
    }

    public AwsImageService() {
        // Constructor can be empty or do instance-specific initialization
        // The static client is already initialized
    }

    @Override
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold) {
        if (rekognitionClient == null) {
            log.error("AWS Rekognition client not initialized");
            return false;
        }

        Image awsImage = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", os);
            awsImage = Image.builder().bytes(SdkBytes.fromByteArray(os.toByteArray())).build();
        } catch (IOException ioe) {
            log.error("Error building image byte array", ioe);
            return false;
        }

        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                .image(awsImage)
                .minConfidence(confidenceThreshhold)
                .build();

        try {
            DetectLabelsResponse response = rekognitionClient.detectLabels(detectLabelsRequest);
            logLabelsForFun(response);
            return response.labels().stream()
                    .filter(l -> l.name().toLowerCase(Locale.ROOT).contains("cat"))  // FIX: Added Locale.ROOT
                    .findFirst()
                    .isPresent();
        } catch (Exception e) {
            log.error("Error detecting labels with AWS Rekognition", e);
            return false;
        }
    }

    private void logLabelsForFun(DetectLabelsResponse response) {
        log.info(response.labels().stream()
                .map(label -> String.format("%s(%.1f%%)", label.name(), label.confidence()))
                .collect(Collectors.joining(", ")));
    }
}