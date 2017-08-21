package com.kairos.persistence.model.user.access_permission;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.kairos.persistence.model.deserializer.XmlDeserializer;

/**
 * Created by prabjot on 5/1/17.
 */
@JsonDeserialize(using = XmlDeserializer.class)
@JacksonXmlRootElement(localName = "page")
public class Tab {

    @JacksonXmlProperty(localName = "moduleId", isAttribute = true)
    public String moduleId;

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    public String name;

    @JacksonXmlProperty(localName = "isModule", isAttribute = true)
    public boolean isModule;

    public String getModuleId() {
        return moduleId;
    }

    public String getName() {
        return name;
    }

    public boolean isModule() {
        return isModule;
    }

    public List<Tab> getSubPages() {
        return subPages;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setModule(boolean module) {
        isModule = module;
    }

    public void setSubPages(List<Tab> subPages) {
        this.subPages = subPages;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "page")
    public List<Tab> subPages;

    public Tab(String id, String name, boolean isModule,List<Tab> children) {
        this.moduleId = id;
        this.name = name;
        this.isModule  = isModule;
        this.subPages = Optional.fromNullable(children).or(Lists.<Tab>newArrayList());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", moduleId)
                .add("subPages", subPages)
                .toString();
    }

    public Tab() {
    }
}
