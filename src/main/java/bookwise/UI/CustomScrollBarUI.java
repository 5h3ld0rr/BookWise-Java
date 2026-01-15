package bookwise.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class CustomScrollBarUI extends BasicScrollBarUI {

    @Override
    protected void configureScrollBarColors() {
        this.trackColor = Color.WHITE;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(Color.WHITE);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color color;
        if (isDragging) {
            color = new Color(170, 170, 170);
        } else if (isThumbRollover()) {
            color = new Color(190, 190, 190);
        } else {
            color = new Color(220, 220, 220);
        }
        g2.setColor(color);

        int padding = 3; 
        int arc = 8; 

        g2.fillRoundRect(thumbBounds.x + padding, thumbBounds.y + padding, 
                         thumbBounds.width - (padding * 2), thumbBounds.height - (padding * 2), 
                         arc, arc);

        g2.dispose();
    }

    @Override
    protected javax.swing.JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected javax.swing.JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private javax.swing.JButton createZeroButton() {
        javax.swing.JButton button = new javax.swing.JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }
}
