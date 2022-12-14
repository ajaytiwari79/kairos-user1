package com.kairos.persistence.model.data_inventory.asset;

import com.kairos.enums.gdpr.AssetAssessor;
import com.kairos.persistence.model.common.BaseEntity;
import com.kairos.persistence.model.embeddables.ManagingOrganization;
import com.kairos.persistence.model.embeddables.Staff;
import com.kairos.persistence.model.master_data.default_asset_setting.*;
import com.kairos.response.dto.data_inventory.AssetBasicResponseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Entity
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "assetWithProcessingActivity",
                classes = @ConstructorResult(
                        targetClass = AssetBasicResponseDTO.class,
                        columns = {
                                @ColumnResult(name = "id"),
                                @ColumnResult(name = "name"),
                                @ColumnResult(name = "processingActivityId", type= BigInteger.class),
                                @ColumnResult(name = "processingActivityName", type=String.class),
                                @ColumnResult(name = "subProcessingActivity", type = boolean.class),
                                @ColumnResult(name = "parentProcessingActivityId",type = BigInteger.class),
                                @ColumnResult(name = "parentProcessingActivityName",type = String.class)
                        }
                )
        )
})
@NamedNativeQueries({
        @NamedNativeQuery(name = "getAllAssetRelatedProcessingActivityData",resultSetMapping = "assetWithProcessingActivity",resultClass = AssetBasicResponseDTO.class,
                query = " select AST.id as id,AST.name  as name,PA.id as processingActivityId , PA.name as processingActivityName , PA.is_sub_processing_activity as subProcessingActivity , PPA.id as parentProcessingActivityId ,PPA.name as parentProcessingActivityName from asset AST" +
                        " left join processing_activity_assets PAA on PAA.assets_id=AST.id " +
                        " left join processing_activity PA on PA.id = PAA.processing_activity_id " +
                        " left join processing_activity PPA on PA.processing_activity_id = PPA.id" +
                        " where AST.organization_id = ?1 and AST.deleted = false and PA.id is not null")
})
@Getter
@Setter
@NoArgsConstructor
public class Asset extends BaseEntity {

    @NotBlank(message = "error.message.name.notNull.orEmpty")
    @Pattern(message = "error.message.name.special.character.notAllowed", regexp = "^[a-zA-Z0-9\\s]+$")
    private String name;
    @NotBlank(message = "error.message.description.notNull.orEmpty")
    private String description;
    private Long countryId;
    private String hostingLocation;
    @Embedded
    private ManagingOrganization managingDepartment;
    @Embedded
    private Staff assetOwner;
    @ManyToMany(fetch = FetchType.LAZY)
    private List<StorageFormat> storageFormats  = new ArrayList<>();
    @ManyToMany(fetch = FetchType.LAZY)
    private List<OrganizationalSecurityMeasure> orgSecurityMeasures  = new ArrayList<>();
    @ManyToMany(fetch = FetchType.LAZY)
    private List<TechnicalSecurityMeasure> technicalSecurityMeasures  = new ArrayList<>();
    @OneToOne
    private HostingProvider hostingProvider;
    @OneToOne
    private HostingType hostingType;
    @OneToOne
    private DataDisposal dataDisposal;
    @OneToOne
    private AssetType assetType;
    @OneToOne
    private AssetType subAssetType;
    private Integer dataRetentionPeriod;
    @NotNull(message = "error.message.status.notnull")
    private boolean active=true;
    private boolean suggested;
    private AssetAssessor assetAssessor;
    private Long organizationId;


    public Asset(String name, String description, String hostingLocation, ManagingOrganization managingDepartment, Staff assetOwner) {
        this.name = name;
        this.description = description;
        this.hostingLocation=hostingLocation;
        this.assetOwner=assetOwner;
        this.managingDepartment=managingDepartment;
    }


    public Asset(String name, String description, boolean active) {
        this.name = name;
        this.description = description;
        this.active = active;
    }
}


