package com.be.service;

public interface EmailService {
    void sendVerificationCode(String toEmail, String code);
    void sendForgotPasswordCode(String toEmail, String code);
}
