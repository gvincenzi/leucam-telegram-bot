package org.leucam.telegram.bot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.leucam.telegram.bot.model.type.ActionType;
import org.leucam.telegram.bot.model.type.ColorType;
import org.leucam.telegram.bot.model.type.FrontBackType;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "leucam_action")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String name;
    @Column
    private String fileId;
    @Column
    private String filePath;
    @Column
    private ActionType actionType;
    @Column
    private FrontBackType frontBackType;
    @Column
    private ColorType colorType;
    @Column
    private Integer telegramUserId;
    @Column
    private Integer numberOfCopies;
    @Column
    private Integer pagesPerSheet;
    @Column
    private Boolean inProgress = Boolean.TRUE;

    @Override
    public String toString() {
        return "Ordine in corso" +
                "\nNome del file='" + name + '\'' +
                "\nTipo di ordine=" + actionType.getLabel() +
                "\nBianco e Nero o Colore=" + (colorType != null ? colorType.getLabel() : "NON DEFINITO") +
                "\nFronte/Retro=" + (frontBackType != null ? frontBackType.getLabel() : "NON DEFINITO") +
                "\nPagine per foglio=" + (pagesPerSheet != null ? String.valueOf(pagesPerSheet) : "NON DEFINITO") +
                "\nNumero di copie=" + (numberOfCopies != null ? String.valueOf(numberOfCopies) : "NON DEFINITO") +
                '\n';
    }
}
