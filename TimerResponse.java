package com.lseg.ipps.solutions.tpl.model;

import java.util.Map;

public record TimerResponse(long remainingTime, Map<String, Object> data) {
}
