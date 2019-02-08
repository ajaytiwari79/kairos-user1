package com.kairos.dto.user.access_page;

import java.util.List;

public class KPIAccessPageDTO {
    private String name;
    private String moduleId;
    private boolean read;
    private boolean write;
    private boolean active;
    private List<KPIAccessPageDTO> child;
    private boolean enable;
    private boolean defaultTab;

    public KPIAccessPageDTO(){

    }

    public KPIAccessPageDTO(String name, String moduleId){
        this.name = name;
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public List<KPIAccessPageDTO> getChild() {
        return child;
    }

    public void setChild(List<KPIAccessPageDTO> child) {
        this.child = child;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isDefaultTab() {
        return defaultTab;
    }

    public void setDefaultTab(boolean defaultTab) {
        this.defaultTab = defaultTab;
    }
}
