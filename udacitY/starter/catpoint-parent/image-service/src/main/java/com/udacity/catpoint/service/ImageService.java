// image-service/src/main/java/com/udacity/catpoint/service/ImageService.java
package com.udacity.catpoint.service;

import java.awt.image.BufferedImage;

public interface ImageService {
    boolean imageContainsCat(BufferedImage image, float confidenceThreshold);
}