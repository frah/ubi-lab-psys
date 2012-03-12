package org.ubilab.payment.card;

/**
 * カードが検出された際に発生するイベントオブジェクト
 * @author atsushi-o
 */
public class CardAvailableEvent extends java.util.EventObject {
    private final String uid;

    public CardAvailableEvent(Object src, String uid) {
        super(src);
        this.uid = uid;
    }

    public String getUid() {
        return this.uid;
    }
}
