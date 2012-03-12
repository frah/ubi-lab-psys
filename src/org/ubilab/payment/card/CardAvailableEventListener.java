package org.ubilab.payment.card;

/**
 * カードを検出した際にイベントを受け取る
 * @author atsushi-o
 */
public interface CardAvailableEventListener extends java.util.EventListener {
    public void cardAvailable(CardAvailableEvent ev);
}
