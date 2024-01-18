package ru.selsup.test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CrptApi {
    Logger logger = Logger.getLogger(CrptApi.class.getName());
    private ArrayList<Document> documents = new ArrayList<>();
    private LinkedList<HttpRequest> queue = new LinkedList<>();
    private boolean isProcessingActive;
    private boolean isLimitReached = false;
    private int numOfRequests;
    private final HttpClient httpClient;
    private final TimeUnit timeUnit;
    private final long duration;
    private final int requestLimit;
    ObjectMapper objectMapper = new ObjectMapper();
    private Document document = new Document();

    public CrptApi(HttpClient httpClient, TimeUnit timeUnit, long duration, int requestLimit) {
        this.httpClient = httpClient;
        this.timeUnit = timeUnit;
        this.duration = duration;
        this.requestLimit = requestLimit;
    }

    public void processRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
        if (!isProcessingActive) {
            initializeProcessing();
        }
        if(isProcessingActive && numOfRequests < requestLimit) {
            placeDocumentFromRequest(httpRequest);
        }
        if (numOfRequests == requestLimit) isLimitReached = true;
        numOfRequests++;
        if (isLimitReached) {
            queue.add(httpRequest);
        }
        logger.info("Кол-во документов: " + documents.size() + "\n" +
                "Кол-во запросов: " + numOfRequests + "\n" +
                "Запросов в очереди: " + queue.size());
    }

    private void placeDocumentFromRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String documentContents = response.body();
        Document document = objectMapper.readValue(documentContents, Document.class);
        documents.add(document);
    }

    private void initializeProcessing() {
        logger.info("Процесс начался");
        long start = System.currentTimeMillis();
        long end = start + timeUnit.toMillis(duration);
        numOfRequests = 0;
        isProcessingActive = true;
        isLimitReached = false;
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    if (System.currentTimeMillis() > end) {
                        isProcessingActive = false;
                        Thread.currentThread().interrupt();
                    }
                } catch (InterruptedException e) {
                    if (System.currentTimeMillis() > end) {
                        isProcessingActive = false;
                        logger.info("Процесс завершился");
                        if (!queue.isEmpty()) {
                            try {
                                int queueSize = queue.size();
                                for (int i = 0; i < queueSize; i++) {
                                    HttpRequest firstInQueue = queue.getFirst();
                                    queue.removeFirst();
                                    processRequest(firstInQueue);
                                }
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();
    }

    public Document getDocument() {
        return document;
    }

    static class Document {

        static class Description{
            private String participantInn = "string";

            public String getParticipantInn() {
                return participantInn;
            }

            public void setParticipantInn(String participantInn) {
                this.participantInn = participantInn;
            }
        }

         static class Product{
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

}
