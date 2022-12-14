package com.kairos.controller.glide_time;
/*
 *Created By Pavan on 20/10/18
 *
 */

import com.kairos.dto.activity.glide_time.GlideTimeSettingsDTO;
import com.kairos.service.glide_time.GlideTimeSettingsService;
import com.kairos.utils.response.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_V1;
import static com.kairos.constants.ApiConstants.COUNTRY_GLIDE_TIME_SETTINGS;

@RestController
@RequestMapping(API_V1)
public class GlideTimeSettingsController {

    @Inject
    private GlideTimeSettingsService glideTimeSettingsService;

    @PutMapping(value = COUNTRY_GLIDE_TIME_SETTINGS)
    public ResponseEntity<Map<String,Object>> saveGlideTimeSettings(@PathVariable Long countryId, @RequestBody GlideTimeSettingsDTO glideTimeSettingsDTO){
        return ResponseHandler.generateResponse(HttpStatus.OK,true, glideTimeSettingsService.saveGlideTimeSettings(countryId,glideTimeSettingsDTO));
    }

    @GetMapping(value = COUNTRY_GLIDE_TIME_SETTINGS)
    public ResponseEntity<Map<String,Object>> getGlideTimeSettings(@PathVariable Long countryId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true, glideTimeSettingsService.getGlideTimeSettings(countryId));
    }
}
