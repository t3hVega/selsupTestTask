package ru.selsup.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Класс для работы с API честного знака
 */
public class CrptApi {
    private static String targetUri = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    Logger logger = Logger.getLogger(CrptApi.class.getName());
    private final HttpClient httpClient;
    private final TimeUnit timeUnit;
    private final long duration;
    private final int requestLimit;
    private final Semaphore requestSemaphore;
    private final ScheduledExecutorService scheduler;
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Конструктор нашего класса, в него подается HTTP-клиент, ед-ца времени,
     * длительность окна приема запросов и кол-во обрабатываемых запросов
     * @param httpClient
     * @param timeUnit
     * @param duration
     * @param requestLimit
     */
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
        scheduler.scheduleWithFixedDelay(() -> requestSemaphore.release(requestLimit - requestSemaphore.availablePermits()),
                0, duration, timeUnit);
    }

    /**
     * Метод для создания документа
     * @param document
     * @param signature
     * @return
     */
    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        String jsonDocument = convertDocumentToJson(document);
        HttpRequest httpRequest = generateHttpRequest(jsonDocument, signature);
        requestSemaphore.acquire();
        sendRequest(httpRequest);
    }

    /**
     * Метод для конвертации документа в JSON
     * @param document
     * @return String
     * @throws JsonProcessingException
     */
    private String convertDocumentToJson(Document document) throws JsonProcessingException {
        return objectMapper.writeValueAsString(document);
    }

    /**
     * Метод для генерации HTTP-запроса
     * @param jsonDocument
     * @param signature
     * @return HttpRequest
     */
    private HttpRequest generateHttpRequest(String jsonDocument, String signature) {
        return HttpRequest.newBuilder()
                .uri(URI.create(targetUri))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
                .build();
    }

    /**
     * Метод для подачи семафору новых запросов
     * @param httpRequest
     * @return
     */
    private void sendRequest (HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info("Запрос успешно обработан");
        } else {
            logger.warning("Возникла ошибка при отправлении запроса, код ошибки: " + response.statusCode());
        }
    }

    /**
     * Класс документа
     */
    public static class Document {
        private Description description;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private List<Product> products;
        private String regDate;
        private String regNumber;

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

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
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

    /**
     * Класс описания документа
     */
    public static class Description{
        private String participantInn;

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    /**
     * Класс продукта документа
     */
    public static class Product{
        private String certificateDocument;
        private String certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;

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
