package org.ubilab.payment.ui.skin;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author atsushi-o
 */
public class BlackSpiral implements Skin {

    @Override
    public JPanel getBackground(int width, int height) {
        JPanel ret = new JPanel() {
            private final ImageIcon bgimage = new ImageIcon(getClass().getResource("./resources/black_skin.gif"));

            @Override
            public void paintComponent(Graphics g) {
                setOpaque(false);
                Dimension d = getSize();
                int w = bgimage.getIconWidth();
                int h = bgimage.getIconHeight();
                for (int i = 0; i*w < d.width; i++) {
                    for (int j = 0; j*h < d.height; j++) {
                        g.drawImage(bgimage.getImage(), i*w, j*h, w, h, null);
                    }
                }
                super.paintComponent(g);
            }
        };

        ret.setSize(width, height);
        return ret;
    }

    @Override
    public Color getTextColor() {
        return new Color(255, 255, 255);
    }
}
