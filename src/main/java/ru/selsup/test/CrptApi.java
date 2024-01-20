package ru.selsup.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CrptApi {
    private static String targetUri = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private static String selfUri = "http://localhost:8080";
    Logger logger = Logger.getLogger(CrptApi.class.getName());
    private boolean isLimitReached = false;
    private boolean isProcessingActive = false;
    private int numOfRequests;
    private final HttpClient httpClient;
    private final TimeUnit timeUnit;
    private final long duration;
    private final int requestLimit;
    private final Semaphore requestSemaphore;
    private final ScheduledExecutorService scheduler;
    private Document document = new Document();
    ObjectMapper objectMapper = new ObjectMapper();

    public CrptApi(HttpClient httpClient, TimeUnit timeUnit, long duration, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Указано недопустимое число максимальных запросов.");
        }
        this.httpClient = httpClient;
        this.timeUnit = timeUnit;
        this.duration = duration;
        this.requestLimit = requestLimit;
        this.requestSemaphore = new Semaphore(requestLimit, true);
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    private String convertDocumentToJson(Document document) throws JsonProcessingException {
        return objectMapper.writeValueAsString(document);
    }

    private HttpRequest generateHttpRequest(String jsonDocument, String signature) {
        return HttpRequest.newBuilder()
                .uri(URI.create(selfUri))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
                .build();
    }

    private void sendRequest (HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info("Запрос успешно обработан");
        } else {
            logger.warning("Возникла ошибка при отправлении запроса, код ошибки: " + response.statusCode());
        }
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        String jsonDocument = convertDocumentToJson(document);
        HttpRequest httpRequest = generateHttpRequest(jsonDocument, signature);
        if (!isProcessingActive) {
            isProcessingActive = true;
            signaller();
        }
        numOfRequests++;
        if (numOfRequests == requestLimit) {
            isLimitReached = true;
        }
        requestSemaphore.acquire();
        sendRequest(httpRequest);
    }

    private void signaller() {
        isProcessingActive = true;
        scheduler.scheduleWithFixedDelay(() -> {
                    isProcessingActive = false;
                    requestSemaphore.release(requestLimit - requestSemaphore.availablePermits());
                },
                duration, duration, timeUnit);
    }

    public Document getDocument() {
        return document;
    }

    public static class Document {
        private Description description = new Description();
        private String docId = "string";
        private String docStatus = "string";
        private String docType = "LP_INTRODUCE_GOODS";
        private boolean importRequest = true;
        private String ownerInn = "string";
        private String participantInn = "string";
        private String producerInn = "string";
        private String productionDate  = "2020-01-23";;
        private String productionType = "string";
        private ArrayList<Product> products = new ArrayList<>() {{
            new Product();
        }};
        private String regDate = "2020-01-23";
        private String regNumber = "string";

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public ArrayList<Product> getProducts() {
            return products;
        }

        public void setProducts(ArrayList<Product> products) {
            this.products = products;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }
    }

    public static class Description{
        private String participantInn = "string";

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    public static class Product{
        private String certificateDocument = "string";
        private String certificateDocumentDate = "2020-01-23";
        private String certificateDocumentNumber = "string";
        private String ownerInn = "string";
        private String producerInn = "string";
        private String productionDate = "2020-01-23";
        private String tnvedCode = "string";
        private String uitCode = "string";
        private String uituCode = "string";

        public String getCertificateDocument() {
            return certificateDocument;
        }

        public void setCertificateDocument(String certificateDocument) {
            this.certificateDocument = certificateDocument;
        }

        public String getCertificateDocumentDate() {
            return certificateDocumentDate;
        }

        public void setCertificateDocumentDate(String certificateDocumentDate) {
            this.certificateDocumentDate = certificateDocumentDate;
        }

        public String getCertificateDocumentNumber() {
            return certificateDocumentNumber;
        }

        public void setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificateDocumentNumber = certificateDocumentNumber;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getTnvedCode() {
            return tnvedCode;
        }

        public void setTnvedCode(String tnvedCode) {
            this.tnvedCode = tnvedCode;
        }

        public String getUitCode() {
            return uitCode;
        }

        public void setUitCode(String uitCode) {
            this.uitCode = uitCode;
        }

        public String getUituCode() {
            return uituCode;
        }

        public void setUituCode(String uituCode) {
            this.uituCode = uituCode;
        }
    }

}
