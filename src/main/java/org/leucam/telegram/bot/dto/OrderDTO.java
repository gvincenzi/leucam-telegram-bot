package org.leucam.telegram.bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.leucam.telegram.bot.model.type.ActionType;
import org.leucam.telegram.bot.model.type.ColorType;
import org.leucam.telegram.bot.model.type.FrontBackType;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class OrderDTO implements Comparable<OrderDTO>{
    private Long orderId;
    private ActionType actionType;
    private FrontBackType frontBackType;
    private ColorType colorType;
    private Integer numberOfCopies;
    private Integer pagesPerSheet;
    private UserDTO user;
    private ProductDTO product;
    private Boolean paid = Boolean.FALSE;
    private BigDecimal amount;
    private String orderPreparationDate;
    private String orderDeliveryDate;
    @JsonIgnore
    private BigDecimal totalToPay;

    @Override
    public int compareTo(OrderDTO orderDTO) {
        return this.orderId.compareTo(orderDTO.orderId);
    }

    @Override
    public String toString() {
        return "\nID : " + orderId +
                "\nFile PDF : " + product +
                "\nTipo di ordine=" + actionType.getLabel() +
                "\nBianco e Nero o Colore=" + colorType.getLabel() +
                "\nFronte/Retro=" + frontBackType.getLabel() +
                "\nPagine per foglio=" + pagesPerSheet +
                "\nNumero di copie=" + numberOfCopies +
                (orderPreparationDate != null && orderDeliveryDate == null ? "\n\n**Ordine pronto per la consegna" : (orderDeliveryDate == null) ? "\n\n**Ordine in lavorazione**" : "") +
                (orderDeliveryDate != null && orderPreparationDate != null ? "\n\n**Ordine consegnato il " + LocalDateTime.parse(orderDeliveryDate).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : (orderPreparationDate != null) ? "\n\n**Ordine non ancora consegnato**" : "") +
                (paid ? "\n\n**Totale pagato con credito interno= " + NumberFormat.getCurrencyInstance().format(amount) : "\n\n**Quest'ordine non è ancora stato pagato**");
    }
}
