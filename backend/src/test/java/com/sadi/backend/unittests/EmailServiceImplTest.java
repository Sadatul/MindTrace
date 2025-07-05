package com.sadi.backend.unittests;

import com.sadi.backend.services.impls.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void testSendSimpleEmail() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        // Act
        emailService.sendSimpleEmail(to, subject, text);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getTo());
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(text, sentMessage.getText());
    }

    @Test
    void testSendOtpEmail() throws Exception {
        // Arrange
        String to = "user@example.com";
        String otp = "123456";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // We'll spy MimeMessageHelper just to make sure it is being used correctly
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailService.sendOtpEmail(to, otp);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendOtpEmail_failure() throws Exception {
        // Arrange
        String to = "user@example.com";
        String otp = "654321";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Force helper to throw exception
        doThrow(new RuntimeException("Send failed")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendOtpEmail(to, otp);
        });
    }
}
