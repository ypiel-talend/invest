package org.ypiel.invest;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.ypiel.invest.loan.LinkedLoanEntry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
public class LinkedEntry extends Entry {

    private final String name;
    private final Integer id;

    private LinkedEntry previous;
    private LinkedEntry next;

    public LinkedEntry(final String name, final LocalDate date, final BigDecimal amount, final String summary) {
        super(date, amount, summary);

        this.id = 0;
        this.name = name;
        this.previous = null;
    }

    public LinkedEntry(final LinkedEntry previous, final BigDecimal amount, final String summary) {
        super(previous.isFirst() ? previous.getDate() : previous.getDate().plusMonths(1), amount, summary + " #" + (previous.getId() + 1));

        this.setPrevious(previous);
        this.previous.setNext(this);
        this.id = previous.getId() + 1;
        this.name = previous.getName();
    }


    public boolean isFirst() {
        return this.previous == null;
    }

    public boolean isLast() {
        return this.next == null;
    }

    public void setNext(final LinkedEntry next) {
        if (this.next != null) {
            throw new IllegalArgumentException("Next LinkedLoanEntry is already set. Can't be replaced.");
        }

        this.next = next;
    }

    public void removeNext(){
        this.next = null;
    }

    public void removePrevious(){
        this.previous = null;
    }

    /**
     *
     * @return The total number of element in the linked list.
     */
    public Integer size() {
        return this.getLast().getId() + 1;
    }


    public LinkedEntry getFirst() {
        if (this.isFirst()) {
            return this;
        }

        return this.getPrevious().getFirst();
    }

    public LinkedEntry getLast() {
        if (this.isLast()) {
            return this;
        }

        return this.getNext().getLast();
    }

}
