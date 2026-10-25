package com.transitassist.transitassist.entity;

public enum StopRequestStatus {
    /** Saved in our database. This is the ONLY status the prototype ever produces. */
    PENDING,
    /** Reserved for a future driver app. Nothing in this prototype sets it. */
    ACKNOWLEDGED
}
