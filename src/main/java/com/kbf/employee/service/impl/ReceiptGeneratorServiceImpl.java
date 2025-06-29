package com.kbf.employee.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kbf.employee.dto.request.SalaryReceiptDTO;
import com.kbf.employee.service.ReceiptGeneratorService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptGeneratorServiceImpl implements ReceiptGeneratorService {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final float OPACITY = 0.2f;

    @Override
    public byte[] generatePdfReceipt(SalaryReceiptDTO receipt) {
        String title = "SALARY PAYMENT RECEIPT";
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawWatermark(document, content, page);
                drawHeader(document, content, page);
                drawTitle(content, title);
                drawEmployeeInfo(content, receipt);
                drawSalaryDetails(content, receipt);

                if (receipt.getProductivitySummary() != null) {
                    drawProductivitySummary(content, receipt);
                }

                drawBarcode(document, content, receipt.getReceiptNumber());
                drawFooter(document, content, page);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException | WriterException e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void drawWatermark(PDDocument document, PDPageContentStream content, PDPage page) throws IOException {
        BufferedImage watermarkImage = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarkImage.createGraphics();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITY));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        g2d.rotate(Math.toRadians(45), watermarkImage.getWidth() / 2.0, watermarkImage.getHeight() / 2.0);
        g2d.drawString("KOMBE FARMS", 10, 100);
        g2d.dispose();

        PDImageXObject watermark = LosslessFactory.createFromImage(document, watermarkImage);

        float x = (page.getMediaBox().getWidth() - watermark.getWidth()) / 2;
        float y = (page.getMediaBox().getHeight() - watermark.getHeight()) / 2;
        content.drawImage(watermark, x, y);
    }

    private void drawHeader(PDDocument document, PDPageContentStream content, PDPage page) throws IOException {
        // Load logo using classpath resource
        PDImageXObject logo = loadImageFromResources(document, "static/images/kombe.png");
        if (logo == null) {
            throw new IOException("Logo image not found");
        }

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        float logoHeight = 80f;
        float logoWidth = logo.getWidth() * (logoHeight / logo.getHeight());

        // Draw logo at top center
        float logoX = (pageWidth - logoWidth) / 2;
        float logoY = pageHeight - MARGIN - logoHeight;
        content.drawImage(logo, logoX, logoY, logoWidth, logoHeight);

        // Draw company info text at top right
        float textX = pageWidth - MARGIN - 180;
        float textY = pageHeight - MARGIN - 20;

        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(textX, textY);
        content.showText("KOMBE FARMS LTD");
        content.setFont(PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(0, -LINE_HEIGHT);
        content.showText("P.O. Box 1234, Douala, Cameroon");
        content.newLineAtOffset(0, -LINE_HEIGHT);
        content.showText("Tel: +237 6 12 34 56 78");
        content.newLineAtOffset(0, -LINE_HEIGHT);
        content.showText("Email: info@kombefarms.com");
        content.endText();

        // Draw a separator line below the header
        float lineY = logoY - 10;
        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, lineY);
        content.lineTo(pageWidth - MARGIN, lineY);
        content.stroke();
    }

    private PDImageXObject loadImageFromResources(PDDocument document, String resourcePath) throws IOException {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            byte[] imageBytes = StreamUtils.copyToByteArray(is);
            return LosslessFactory.createFromImage(document, ImageIO.read(new ByteArrayInputStream(imageBytes)));
        } catch (Exception e) {
            return null;
        }
    }

    private void drawTitle(PDPageContentStream content, String title) throws IOException {
        float fontSize = 18f;
        PDType1Font font = PDType1Font.HELVETICA_BOLD;
        float titleWidth = font.getStringWidth(title) / 1000 * fontSize;
        float startX = (PDRectangle.A4.getWidth() - titleWidth) / 2;

        content.beginText();
        content.setNonStrokingColor(new Color(0, 51, 102)); //dark Blue
        content.setFont(font, fontSize);
        content.newLineAtOffset(startX, 640);
        content.showText(title);
        content.endText();
    }

    private void drawEmployeeInfo(PDPageContentStream content, SalaryReceiptDTO receipt) throws IOException {
        float yStart = 600;
        float blockHeight = 90;

        // Background
        content.setNonStrokingColor(new Color(245, 245, 245)); // light gray
        content.addRect(MARGIN, yStart - blockHeight, 500, blockHeight);
        content.fill();

        // Header
        content.beginText();
        content.setNonStrokingColor(new Color(0, 51, 102)); //dark Blue
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(MARGIN, yStart + 10);
        content.showText("EMPLOYEE INFORMATION");
        content.endText();

        // Body
        content.beginText();
        content.setNonStrokingColor(Color.BLACK);
        content.setFont(PDType1Font.HELVETICA, 10);
        content.setLeading(14f);
        content.newLineAtOffset(MARGIN + 10, yStart - 20);
        content.showText("Name: " + receipt.getEmployee().getName());
        content.newLine();
        content.showText("Employee ID: " + receipt.getEmployee().getId());
        content.newLine();
        content.showText("Department: " + receipt.getEmployee().getDepartment());
        content.newLine();
        content.showText("Employment Date: " + receipt.getEmployee().getEmploymentDate().format(DATE_FORMATTER));
        content.endText();
    }

    private void drawSalaryDetails(PDPageContentStream content, SalaryReceiptDTO receipt) throws IOException {
        float yStart = 480;
        float blockHeight = 100;

        content.setNonStrokingColor(new Color(240, 248, 255)); // light blue
        content.addRect(MARGIN, yStart - blockHeight + 20, 500, blockHeight);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(new Color(0, 51, 102));
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(MARGIN, yStart + 10);
        content.showText("PAYMENT DETAILS");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(Color.BLACK);
        content.setFont(PDType1Font.HELVETICA, 10);
        content.setLeading(14f);
        content.newLineAtOffset(MARGIN + 10, yStart);
        content.showText("Receipt Number: " + receipt.getReceiptNumber());
        content.newLine();
        content.showText("Payment Date: " + receipt.getSalary().getPaymentDate().format(DATE_FORMATTER));
        content.newLine();
        content.showText("Payment Reference: " + receipt.getSalary().getPaymentReference());
        content.newLine();
        content.showText("Amount: " + String.format("%,.0f XAF", receipt.getSalary().getAmount()));
        content.newLine();
        content.showText("Status: " + receipt.getSalary().getStatus());
        content.endText();
    }

    private void drawProductivitySummary(PDPageContentStream content, SalaryReceiptDTO receipt) throws IOException {
        float yStart = 360;
        float blockHeight = 90;

        content.setNonStrokingColor(new Color(255, 250, 240)); // light cream
        content.addRect(MARGIN, yStart - blockHeight + 20, 500, blockHeight);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(new Color(102, 51, 0)); // dark brown
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(MARGIN, yStart + 10);
        content.showText("PRODUCTIVITY SUMMARY");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(Color.BLACK);
        content.setFont(PDType1Font.HELVETICA, 10);
        content.setLeading(14f);
        content.newLineAtOffset(MARGIN + 10, yStart);
        content.showText("Total Expected Hours: " + receipt.getProductivitySummary().getTotalExpectedHours());
        content.newLine();
        content.showText("Total Actual Hours: " + receipt.getProductivitySummary().getTotalActualHours());
        content.newLine();
        content.showText("Overall Productivity: " +
                receipt.getProductivitySummary().getOverallProductivity().setScale(2, RoundingMode.HALF_UP) + "%");
        content.newLine();
        content.showText("Working Days: " + receipt.getProductivitySummary().getWorkingDays());
        content.endText();
    }

    private void drawBarcode(PDDocument document, PDPageContentStream content, String receiptNumber)
            throws IOException, WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode("RECEIPT:" + receiptNumber, BarcodeFormat.QR_CODE, 100, 100);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        PDImageXObject qrCode = LosslessFactory.createFromImage(document, qrImage);

        // Bottom-left corner
        float qrX = MARGIN;
        float qrY = 60;
        content.drawImage(qrCode, qrX, qrY, 60, 60);

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 8);
        content.newLineAtOffset(qrX, qrY - 10);
        content.showText("Scan to verify authenticity");
        content.endText();
    }

    private void drawFooter(PDDocument document, PDPageContentStream content, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();

        // Line separator
        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, 130);
        content.lineTo(pageWidth - MARGIN, 130);
        content.stroke();

        // Signature image (bottom-right)
        PDImageXObject signature = loadImageFromResources(document, "static/images/signature.png");
        if (signature != null) {
            float signatureHeight = 40f;
            float signatureWidth = signature.getWidth() * (signatureHeight / signature.getHeight());
            float sigX = pageWidth - MARGIN - signatureWidth;
            float sigY = 70;
            content.drawImage(signature, sigX, sigY, signatureWidth, signatureHeight);
        }

        // Signature label
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 9);
        content.newLineAtOffset(pageWidth - MARGIN - 100, 70 - 12);
        content.showText("HR Manager");
        content.endText();

        // Footer text - centered at bottom
        String line1 = "Document generated on: " + LocalDate.now().format(DATE_FORMATTER);
        PDType1Font font = PDType1Font.HELVETICA_OBLIQUE;
        float fontSize = 8f;

        float textWidth1 = font.getStringWidth(line1) / 1000 * fontSize;

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset((pageWidth - textWidth1) / 2, 60);
        content.showText(line1);
        content.endText();
    }
}