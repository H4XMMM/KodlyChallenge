package com.dws.challenge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDTO {

  @NotNull
  @NotEmpty
  private String accountIdFrom;

  @NotNull
  @NotEmpty
  private String accountIdTo;

  @NotNull
  @Positive(message = "Must transfer a positive non-zero amount.")
  private BigDecimal amount;

}
