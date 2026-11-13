package com.ecozap.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "stations")
public class Station {

    @Id
    private String id;

    private String name;
    private String type;
    private String address;
    private Double latitude;
    private Double longitude;
}