package com.comp3820.course.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    private static final int QR_CODE_SIZE = 300;

    /**
     * Generate QR Code image from text content
     * @param text The text/URL to encode in the QR code
     * @return PNG image bytes
     */
    public byte[] generateQRCodeImage(String text) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);  // Reduce white space around QR code

        BitMatrix bitMatrix = qrCodeWriter.encode(
            text, 
            BarcodeFormat.QR_CODE, 
            QR_CODE_SIZE, 
            QR_CODE_SIZE, 
            hints
        );

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);

        return baos.toByteArray();
    }

    /**
     * Generate QR Code with custom size
     * @param text The text/URL to encode
     * @param size The size of the QR code in pixels
     * @return PNG image bytes
     */
    public byte[] generateQRCodeImage(String text, int size) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(
            text, 
            BarcodeFormat.QR_CODE, 
            size, 
            size, 
            hints
        );

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);

        return baos.toByteArray();
    }
}
