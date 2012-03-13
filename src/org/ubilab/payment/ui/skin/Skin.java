package org.ubilab.payment.ui.skin;

import javax.swing.JPanel;
import java.awt.Color;

/**
 *
 * @author atsushi-o
 */
public interface Skin {
    public JPanel getBackground(int width, int height);
    public Color getTextColor();
}
