package com.kairos.cta;

import com.kairos.user.agreement.cta.CTARuleTemplateType;

public class Test {
    public static void main (String ...arg){

        for (Enum<?> enumValue : CTARuleTemplateType.values()) {
           System.out.println(enumValue);
        }
    }
}
