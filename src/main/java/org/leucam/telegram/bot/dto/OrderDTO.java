package org.leucam.telegram.bot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.leucam.telegram.bot.model.type.ActionType;
import org.leucam.telegram.bot.model.type.ColorType;
import org.leucam.telegram.bot.model.type.FrontBackType;

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

    @Override
    public int compareTo(OrderDTO orderDTO) {
        return this.orderId.compareTo(orderDTO.orderId);
    }

    @Override
    public String toString() {
        return "\nID : " + orderId +
                "\nProdotto : " + product +
                "\nTipo di ordine=" + actionType.getLabel() +
                "\nBianco e Nero o Colore=" + colorType.getLabel() +
                "\nFronte/Retro=" + frontBackType.getLabel() +
                "\nPagine per foglio=" + pagesPerSheet +
                "\nNumero di copie=" + numberOfCopies +
                (paid ? "" : "\n\n**Quest'ordine non è ancora stato pagato**");
    }
}
