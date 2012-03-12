package org.ubilab.payment;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ubilab.payment.card.*;

/**
 * Ubi-lab会計システムメインクラス
 * @author atsushi-o
 */
public class UbiLabPaymentSystem implements Runnable, CardAvailableEventListener {
    private CardThread card;
    private String loginCardID;

    private volatile Thread running;

    private static final Logger LOG;
    static {
        LOG = Logger.getLogger(UbiLabPaymentSystem.class.getName());
    }

    /**
     * コンストラクタ
     */
    public UbiLabPaymentSystem() {
        //new mainWindow().setVisible(true);
        try {
            card = new CardThread();
            card.addListener(this);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "カードリーダの初期化に失敗しました", ex);
            System.exit(1);
        }

        loginCardID = "";
    }

    /**
     * Ubi-lab会計システムを開始する
     */
    public void start() {
        card.start();

        running = new Thread(this);
        running.start();
    }

    /**
     * 全スレッド停止（＝終了）
     */
    public void stop() {
        running = null;
        card.stop();
    }

    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        while(running == thisThread) {
            if (!loginCardID.equals("")) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                card.resume();
            }
            try {Thread.sleep(1000);}catch(Exception e) {}
        }

    }

    @Override
    public void cardAvailable(CardAvailableEvent ev) {
        LOG.log(Level.INFO, "Card Available: {0}", ev.getUid());
        loginCardID = ev.getUid();
    }


    public static void main(String[] args) {
        new UbiLabPaymentSystem().start();
    }
}
