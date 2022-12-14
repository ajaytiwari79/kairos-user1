package com.kairos.dto.user.country;

import com.kairos.commons.utils.TranslationUtil;
import com.kairos.dto.TranslationInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static com.kairos.commons.utils.ObjectUtils.isNullOrElse;

@Getter
@Setter
public class LevelDTO {
    private Long id;
    private String name;
    private String description;
    private boolean isEnabled = true;
    private Map<String,String> translatedNames = new HashMap<>();
    private Map<String,String> translatedDescriptions = new HashMap<>();
    private Map<String, TranslationInfo> translations = new HashMap<>();

    public Map<String, TranslationInfo> getTranslatedData() {
        Map<String, TranslationInfo> infoMap=new HashMap<>();
        translatedNames = isNullOrElse(translatedNames,new HashMap<>());
        translatedNames.forEach((k,v)-> infoMap.put(k,new TranslationInfo(v,translatedDescriptions.get(k))));
        return infoMap;
    }

    public String getName() {
        return TranslationUtil.getName(translations,name);
    }

    public String getDescription() {
        return TranslationUtil.getDescription(translations,description);
    }


}
