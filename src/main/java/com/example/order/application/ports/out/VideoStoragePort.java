package com.example.order.application.ports.out;

import java.io.File;

// application/ports/output/VideoStoragePort.java
public interface VideoStoragePort {
    byte[] download(String fileName);
    String uploadZip(String fileName, File file);
}