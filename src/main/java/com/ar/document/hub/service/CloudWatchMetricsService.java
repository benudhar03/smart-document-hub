package com.ar.document.hub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;

@Service
@Slf4j
public class CloudWatchMetricsService {
    
    @Autowired
    private CloudWatchClient cloudWatchClient;
    
    public void publishMetric(String metricName, double value, String unit) {
        try {
            MetricDatum datum = MetricDatum.builder()
                    .metricName(metricName)
                    .value(value)
                    .unit(unit)
                    .timestamp(Instant.now())
                    .build();
            
            PutMetricDataRequest request = PutMetricDataRequest.builder()
                    .namespace("SmartDocumentHub")
                    .metricData(datum)
                    .build();
            
            cloudWatchClient.putMetricData(request);
            log.info("Metric published: {} = {}", metricName, value);
            
        } catch (Exception e) {
            log.error("Error publishing metric to CloudWatch", e);
        }
    }
    
    public void recordDocumentUpload(String userId) {
        publishMetric("DocumentUploads", 1, "Count");
        
        MetricDatum userDatum = MetricDatum.builder()
                .metricName("UserDocumentUploads")
                .value(1.0)
                .unit("Count")
                .dimensions(Dimension.builder()
                        .name("UserId")
                        .value(userId)
                        .build())
                .build();
        
        PutMetricDataRequest request = PutMetricDataRequest.builder()
                .namespace("SmartDocumentHub")
                .metricData(userDatum)
                .build();
        
        cloudWatchClient.putMetricData(request);
    }
    
    public void recordSearchOperation(String queryType) {
        MetricDatum datum = MetricDatum.builder()
                .metricName("SearchOperations")
                .value(1.0)
                .unit("Count")
                .dimensions(Dimension.builder()
                        .name("QueryType")
                        .value(queryType)
                        .build())
                .build();
        
        PutMetricDataRequest request = PutMetricDataRequest.builder()
                .namespace("SmartDocumentHub")
                .metricData(datum)
                .build();
        
        cloudWatchClient.putMetricData(request);
    }
}