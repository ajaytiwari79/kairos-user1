package com.kairos.response.dto.web.client;

import com.kairos.persistence.model.user.client.ClientTemporaryAddress;

/**
 * Created by prabjot on 13/11/17.
 */
public class ClientTemporaryAddressDTO {
    private Long clientId;
    private ClientTemporaryAddress clientTemporaryAddress;

    public ClientTemporaryAddressDTO(){
        //default constructor
    }

    public ClientTemporaryAddressDTO(Long clientId) {
        this.clientId = clientId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public ClientTemporaryAddress getClientTemporaryAddress() {
        return clientTemporaryAddress;
    }

    public void setClientTemporaryAddress(ClientTemporaryAddress clientTemporaryAddress) {
        this.clientTemporaryAddress = clientTemporaryAddress;
    }
}
