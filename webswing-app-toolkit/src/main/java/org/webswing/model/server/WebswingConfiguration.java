package org.webswing.model.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WebswingConfiguration implements Serializable {

    private static final long serialVersionUID = 7439369522912189703L;

    private List<SwingApplicationDescriptor> applications = new ArrayList<SwingApplicationDescriptor>();

    public void setApplications(List<SwingApplicationDescriptor> applications) {
        this.applications = applications;
    }

    public List<SwingApplicationDescriptor> getApplications() {
        return applications;
    }

}