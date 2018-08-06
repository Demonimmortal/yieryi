package com.idleroom.web.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "sequence")
public class SequenceId implements Serializable {
    @Id
    public   String id;
    public   long  seqId;
    public   String collName;
}
