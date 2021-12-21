package part2.common;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class Tile implements Comparable<Tile>, Serializable {
    private final ImageIcon wrapper;
    private final int originalPosition;
    private int currentPosition;

    public Tile(final Image image, final int originalPosition, final int currentPosition) {
        super();
        this.wrapper = new ImageIcon(image);
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    public Image getImage() {
        return wrapper.getImage();
    }

    public boolean isInRightPlace() {
        return currentPosition == originalPosition;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(final int newPosition) {
        currentPosition = newPosition;
    }

    @Override
    public int compareTo(Tile other) {
        return Integer.compare(this.currentPosition, other.currentPosition);
    }
}
