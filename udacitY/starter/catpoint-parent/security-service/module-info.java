module com.udacity.catpoint.security.service {
    exports com.udacity.catpoint.application;
    exports com.udacity.catpoint.data;
    exports com.udacity.catpoint.service;

    requires com.udacity.catpoint.image.service;
    requires com.miglayout.miglayout.swing;
    requires com.google.gson;
    requires com.google.common;
    requires org.slf4j;
    requires java.desktop;

    opens com.udacity.catpoint.data to com.google.gson;
    opens com.udacity.catpoint.service to org.junit.jupiter, org.mockito;
}