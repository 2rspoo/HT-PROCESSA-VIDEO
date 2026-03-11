package com.example.order.application.ports.out;

public interface NotificationPort {
    void sendNotification(String pedidoId, String status, String s3Url);
}