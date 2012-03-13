package org.ubilab.payment.ui.skin;

import java.awt.Color;
import javax.swing.JPanel;

/**
 * デフォルトスキン
 * @author atsushi-o
 */
public class DefaultSkin implements Skin {
    @Override
    public JPanel getBackground(int width, int height) {
        JPanel ret = new GradientPanel(new java.awt.Color(143, 215, 69), new java.awt.Color(84,127,37));
        ret.setSize(width, height);
        return ret;
    }

    @Override
    public Color getTextColor() {
        return new Color(0, 0, 0);
    }
}
