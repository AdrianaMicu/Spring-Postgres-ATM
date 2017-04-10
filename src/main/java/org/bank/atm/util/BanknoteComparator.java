package org.bank.atm.util;

import java.util.Comparator;

public class BanknoteComparator implements Comparator<Banknote> {
	
	@Override
    public int compare(Banknote banknote1, Banknote banknote2) {
        if (banknote1.getValue() < banknote2.getValue()) {
            return 1;
        } else if (banknote1.getValue() > banknote2.getValue()) {
            return -1;
        }
        return 0;
    }
}
