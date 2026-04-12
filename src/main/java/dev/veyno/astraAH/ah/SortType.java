package dev.veyno.astraAH.ah;

public enum SortType {
    NAME_A_Z(0),
    NAME_Z_A(1),
    PRICE_H_L(2),
    PRICE_L_H(3),
    PRICE_PER_PIECE_H_L(4),
    PRICE_PER_PICE_L_H(5);

    int index;

    SortType(int index){
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
