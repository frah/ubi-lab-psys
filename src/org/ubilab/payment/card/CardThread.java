package org.ubilab.payment.card;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.smartcardio.TerminalFactory;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.CardException;

/**
 * 定期的にカードを読み込みカードのGUIDを得る
 * @author atsushi-o
 */
public class CardThread implements Runnable {
    private CardTerminal terminal;
    private List<CardAvailableEventListener> listeners;
    private volatile Thread cardThread;
    private volatile boolean suspendFlag;

    private static final int WAIT_TIME;
    private static final byte[] MSG_ID;
    private static final Logger LOG;
    static {
        WAIT_TIME = 1000;
        MSG_ID = new byte[]{(byte)0xFF, (byte)0xCA, (byte)0x00, (byte)0x00, (byte)0x00};
        LOG = Logger.getLogger(CardThread.class.getName());
    }

    /**
     * コンストラクタ
     * @throws IOException カードターミナルの取得に失敗
     */
    public CardThread() throws IOException {
        try {
            // Gets the first available terminal
            terminal = TerminalFactory.getDefault().terminals().list().get(0);
        } catch (CardException ex) {
            throw new IOException(ex);
        }

        listeners = new ArrayList<CardAvailableEventListener>();
        suspendFlag = false;
    }

    /**
     * イベントリスナを登録する
     * @param listener 登録するイベントリスナ
     */
    public void addListener(CardAvailableEventListener listener) {
        listeners.add(listener);
    }

    /**
     * イベントリスナの登録解除をする
     * @param listener 登録解除するイベントリスナ
     */
    public void removeListener(CardAvailableEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * カード読み取りスレッドを再開する
     */
    public synchronized void resume() {
        LOG.info("Thread resumed.");
        suspendFlag = false;
        if (!suspendFlag) notify();
    }

    /**
     * スレッドが一時停止中かどうかを返す
     * @return スレッドが一時停止中かどうか
     */
    public boolean isSuspend() {
        return this.suspendFlag;
    }

    /**
     * スレッドを開始する
     */
    public void start() {
        cardThread =  new Thread(this);
        cardThread.start();
    }

    /**
     * スレッドを停止する
     */
    public void stop() {
        cardThread = null;
    }

    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        while(cardThread == thisThread) {
            try {
                Thread.currentThread().sleep(1000);

                if (suspendFlag) {
                    LOG.info("Thread suspended.");
                    synchronized(this) {
                        while (suspendFlag) wait();
                    }
                }
            } catch (InterruptedException ex) {}

            Card card = null;
            try {
                if (terminal.waitForCardPresent(WAIT_TIME) == true) {
                    LOG.fine("Card available.");
                    card = terminal.connect("T=1");
                    CardChannel channel = card.getBasicChannel();
                    ResponseAPDU res = channel.transmit(new CommandAPDU(MSG_ID));

                    StringBuilder sb = new StringBuilder();
                    for (byte b : res.getData()) {
                        String hexValue = Integer.toHexString(b & 0xff).toUpperCase();
                        if (hexValue.length() == 1) {
                            hexValue = "0" + hexValue;
                        }
                        sb.append(hexValue);
                    }

                    // カードID読み取り成功時はイベントを発生させてスレッドを一時停止
                    if (sb.length() != 0) {
                        suspendFlag = true;
                        for (CardAvailableEventListener listener : listeners) {
                            listener.cardAvailable(new CardAvailableEvent(this, sb.toString()));
                        }
                    }

                    terminal.waitForCardAbsent(0);
                }
            } catch (CardException ex) {
                LOG.log(Level.WARNING, "Failed to read card ID.", ex);
            } finally {
                try {
                    if (card != null) card.disconnect(true);
                } catch (CardException ex) {}
            }
        }
    }
}
