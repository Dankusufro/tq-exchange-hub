package com.tq.exchangehub.dto;

import com.tq.exchangehub.entity.AbuseReportAction;
import jakarta.validation.constraints.NotNull;

public class AbuseReportActionRequest {

    @NotNull(message = "La acción es obligatoria")
    private AbuseReportAction action;

    public AbuseReportAction getAction() {
        return action;
    }

    public void setAction(AbuseReportAction action) {
        this.action = action;
    }
}
