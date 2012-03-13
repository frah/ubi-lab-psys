package org.ubilab.payment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ubilab.payment.card.*;

/**
 * Ubi-lab会計システムメインクラス
 * @author atsushi-o
 */
public class UbiLabPaymentSystem implements Runnable, CardAvailableEventListener {
    private Properties settings;
    private mainWindow window;
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
        loginCardID = "";

        loadSettings();
        init();
        window = new mainWindow();
        window.setVisible(true);
    }

    /**
     * 設定読み込み
     */
    private void loadSettings() {
        settings = new Properties();
        try {
            FileInputStream in = new FileInputStream("config.xml");
            settings.loadFromXML(in);
        } catch (IOException ex) {
            if (ex.getClass() == FileNotFoundException.class) {
                LOG.log(Level.INFO, "Setting file not found.", ex);
            } else {
                LOG.log(Level.WARNING, "Setting load error.", ex);
            }

            // Set default settings
            settings.setProperty("DBHost", "127.0.0.1");
            settings.setProperty("DBUser", "ubilab_payment");
            settings.setProperty("DBPass", "ubilab_payment");
            settings.setProperty("twitterOAuthToken", "");
            settings.setProperty("twitterOAuthSecret", "");
        }
    }

    private void init() {
        try {
            card = new CardThread();
            card.addListener(this);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "カードリーダの初期化に失敗しました", ex);
            System.exit(1);
        }
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
                loginCardID = "";
            }
            try {Thread.sleep(1000);}catch(Exception e) {}
        }

    }

    @Override
    public void cardAvailable(CardAvailableEvent ev) {
        LOG.log(Level.INFO, "Card Available: {0}", ev.getUid());
        loginCardID = ev.getUid();
        window.setSkin("org.ubilab.payment.ui.skin.BlackSpiral");
    }


    public static void main(String[] args) {
        new UbiLabPaymentSystem().start();
    }
}
