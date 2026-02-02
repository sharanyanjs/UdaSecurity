module com.udacity.catpoint.image.service {
    exports com.udacity.catpoint.service;

    requires software.amazon.awssdk.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    requires org.slf4j;
    requires java.desktop;

    opens com.udacity.catpoint.service to software.amazon.awssdk.core;
}