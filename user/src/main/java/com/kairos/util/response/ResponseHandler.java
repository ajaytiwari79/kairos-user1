package com.kairos.util.response;

import com.kairos.service.locale.LocaleService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by prabjot on 9/20/16.
 */
public final class ResponseHandler {
    private ResponseHandler() {
    }

    public static ResponseEntity<Map<String, Object>> generateResponse(HttpStatus status, boolean isSuccess, Object responseObj) {
        // Get Time as per UTC format
        long dateTime = new DateTime(DateTimeZone.UTC).getMillis();

        Map<String, Object> map = new HashMap<String, Object>(4);
        map.put("status", status.value());
        map.put("isSuccess", isSuccess);
        map.put("data", responseObj);

        map.put("time_stamp", dateTime);
        return new ResponseEntity<Map<String, Object>>(map, status);

    }

    public static ResponseEntity<String> generateResponse(JSONObject jsonObject) {
        return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.UNAUTHORIZED);
    }


}
