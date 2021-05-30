package org.ypiel.invest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.ypiel.invest.loan.LoanLinkedEntry;

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

    /**
     * In months.
     */
    private int period;

    public LinkedEntry(final String name, final LocalDate date, final BigDecimal amount, final String summary, boolean isDebit, int period) {
        super(date, amount, summary, isDebit);

        this.id = 0;
        this.name = name;
        this.previous = null;
        this.period = period;
    }

    public LinkedEntry(final LinkedEntry previous, final BigDecimal amount, final String summary, boolean isDebit) {
        super(previous.isFirst() ? previous.getDate() : previous.getDate().plusMonths(previous.getPeriod()), amount, summary + " #" + (previous.getId() + 1), isDebit);

        this.setPrevious(previous);
        this.previous.setNext(this);
        this.id = previous.getId() + 1;
        this.name = previous.getName();
        this.period = previous.getPeriod();
    }

    public List<LinkedEntry> asList() {
        List<LinkedEntry> l = new ArrayList<>();
        return this.getLast()._asList(l);
    }

    public List<LinkedEntry> _asList(List<LinkedEntry> l) {
        if (this.isFirst()) {
            l.add(this);
            return l;
        }
        this.getPrevious()._asList(l).add(this);
        return l;
    }

    public boolean isFirst() {
        return this.previous == null;
    }

    public boolean isLast() {
        return this.next == null;
    }

    public List<LinkedEntry> getNextFurtherAsList(int further) {
        List<LinkedEntry> list = new ArrayList<>();
        LinkedEntry current = this;
        list.add(current);
        for (int i = 1; i < further; i++) {
            if (current.isLast()) {
                return list;
            }
            current = current.getNext();
            list.add(current);
        }

        return list;
    }

    public void setNext(final LinkedEntry next) {
        if (this.next != null) {
            throw new IllegalArgumentException("Next LinkedLoanEntry is already set. Can't be replaced.");
        }

        this.next = next;
    }

    public LinkedEntry removeNext() {
        this.next = null;
        return this;
    }

    public LinkedEntry removePrevious() {
        this.previous = null;
        return this;
    }

    public Integer size() {
        return this.getLast()._size();
    }

    /**
     * @return The total number of element in the linked list.
     */
    public Integer _size() {
        if (this.isFirst()) {
            return 1;
        }
        return this.getPrevious()._size() + 1;
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
