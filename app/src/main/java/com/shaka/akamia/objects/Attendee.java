/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     Attendee
 * Purpose:     Attendee object
 * Created by:  John Hou
 * Created on:  9/22/2015
 */
package com.shaka.akamia.objects;


import java.io.Serializable;

public class Attendee implements Serializable {
    String displayName;
    String email;
    String responseStatus;
    boolean resource;
    boolean self;
    boolean organizer;

    public Attendee() {
    }

    public String getDisplayName() {
        return (displayName == null? "" : displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return (email == null? "" : email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResponseStatus() {
        return (responseStatus == null? "" : responseStatus);
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public boolean isResource() {
        return resource;
    }

    public void setResource(boolean resource) {
        this.resource = resource;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public boolean isOrganizer() {
        return organizer;
    }

    public void setOrganizer(boolean organizer) {
        this.organizer = organizer;
    }
}
