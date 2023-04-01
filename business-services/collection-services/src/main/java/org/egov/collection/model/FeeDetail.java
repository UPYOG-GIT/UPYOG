package org.egov.collection.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.egov.collection.model.enums.InstrumentStatusEnum;
import org.egov.collection.model.enums.PaymentModeEnum;
import org.egov.collection.model.enums.PaymentStatusEnum;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class FeeDetail {

    @NotNull
    @JsonProperty("srNo")
    private Integer srNo;

	
    @NotNull
    @Size(max=64)
    @JsonProperty("chargesTypeName")
    private String chargesTypeName;

    @NotNull
    @JsonProperty("amount")
    private BigDecimal amount;

    




}
