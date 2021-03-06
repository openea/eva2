package eva2.gui.plot;

import eva2.tools.Pair;
import eva2.tools.StringTools;
import eva2.tools.chart2d.SlimRect;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * A class representing the legend of a plot. It is created from a list of
 * GraphPointSets as used in FunctionArea. Painting is done in FunctionArea. As
 * an alternative, an own frame could be created.
 *
 * @author mkron, draeger
 */
public class GraphPointSetLegend {
    SortedSet<Pair<String, Color>> legendEntries;

    /**
     * @author draeger
     */
    private static class PairComp implements Comparator<Pair<String, Color>> {

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Pair<String, Color> o1, Pair<String, Color> o2) {
            int comp = o1.car().compareTo(o2.car());
            // Same text; let us see if the color is also identical.
            return comp == 0 ? comp = Integer.valueOf(o1.cdr().getRGB())
                    .compareTo(o2.cdr().getRGB()) : comp;
        }

    }

    private static final PairComp comparator = new PairComp();

    /**
     * A constructor which may enumerate the point sets.
     *
     * @param pointSetContainer the set of point sets to be shown.
     * @param appendIndex       if true, the string entries are enumerated according to the index
     */
    public GraphPointSetLegend(List<GraphPointSet> pointSetContainer, boolean appendIndex) {
        legendEntries = new TreeSet<>(comparator);
        for (int i = 0; i < pointSetContainer.size(); i++) {
            GraphPointSet pointset = pointSetContainer.get(i);
            if (pointset.getPointSet().getSize() > 0) {
                String entryStr;
                if (appendIndex) {
                    entryStr = StringTools.expandPrefixZeros(i, pointSetContainer.size() - 1) + ": " + pointset.getInfoString();
                } else {
                    entryStr = pointset.getInfoString();
                }
                legendEntries.add(new Pair<>(entryStr, pointset.getColor()));
            }
        }
    }

    /**
     * A constructor without enumeration.
     *
     * @param pointSetContainer the set of point sets to be shown.
     */
    public GraphPointSetLegend(List<GraphPointSet> pointSetContainer) {
        this(pointSetContainer, false);
    }

    /**
     * Add the legend labels to a container.
     *
     * @param comp
     */
    public void addToContainer(JComponent comp) {
        for (Pair<String, Color> legendEntry : legendEntries) {
            JLabel label = new JLabel(legendEntry.head);
            label.setForeground(legendEntry.tail);
            comp.add(label);
        }
    }

    /**
     * @param bgCol
     * @param pointSetContainer
     * @return
     */
    public static JPanel makeLegendPanel(Color bgCol,
                                         ArrayList<GraphPointSet> pointSetContainer) {
        JPanel pan = new JPanel();
        pan.setBackground(bgCol);
        pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
        GraphPointSetLegend lBox = new GraphPointSetLegend(pointSetContainer);
        lBox.addToContainer(pan);
        return pan;
    }

    /**
     * @param bgCol
     * @param pointSetContainer
     * @return
     */
    public static JFrame makeLegendFrame(Color bgCol,
                                         ArrayList<GraphPointSet> pointSetContainer) {
        JFrame frame = new JFrame("Legend");
        // LegendBox lBox = new LegendBox(bgCol, pointSetContainer);
        frame.add(makeLegendPanel(bgCol, pointSetContainer));
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    /**
     * @param component
     */
    public void paintIn(JComponent component) {
        Graphics g = component.getGraphics();
        FontMetrics fm = g.getFontMetrics();
        int yOffs = 5 + fm.getHeight();
        int xOffs = 0;
        Color origCol = g.getColor();

        for (Pair<String, Color> legendEntry : legendEntries) {
            g.setColor(legendEntry.tail);
            Rectangle2D rect = fm.getStringBounds(legendEntry.head, g);
            xOffs = (int) (component.getWidth() - rect.getWidth() - 5);
            g.drawString(legendEntry.head, xOffs, yOffs);
            yOffs += (5 + rect.getHeight());
        }
        g.setColor(origCol);
    }

    /**
     * Called from FunctionArea
     * @see FunctionArea#paint
     */
    public void paintIn(Graphics g, SlimRect rect) {
        paintIn(g, (int) rect.getX(), (int) rect.getY(), (int) rect.getX() + (int) rect.getWidth());
    }

    /**
     * @param g
     * @param x
     * @param y
     * @param maxX
     */
    private void paintIn(Graphics g, int x, int y, int maxX) {
        FontMetrics fm = g.getFontMetrics();
        double fontHeightSum = 0.0;
        // Padding is used for in-box padding and out-box positioning
        int padding = 5;
        // yOffs used as bottom-left position for text line
        double yOffs = padding + y + fm.getHeight();
        int minX = Integer.MAX_VALUE;
        Color origCol = g.getColor();

        // Draw bounding box
        for (Pair<String, Color> legendEntry : legendEntries) {
            Rectangle2D stringBounds = fm.getStringBounds(legendEntry.head, g);
            minX = Math.min(minX, (int) (maxX - stringBounds.getWidth() - 20));
            // Compute the combined height + padding of each line
            fontHeightSum += stringBounds.getHeight() + padding;
        }

        //
        int boxHeight = (int)(fontHeightSum + padding);
        int boxWidth = maxX - minX + 20 - padding;
        g.setColor(Color.WHITE);
        g.fillRect(minX - 20, padding + y, boxWidth, boxHeight);
        g.setColor(Color.BLACK);
        g.drawRect(minX - 20, padding + y, boxWidth, boxHeight);

        // avoid that an entry with identical label and color occurs multiple
        // times.
        for (Pair<String, Color> legendEntry : legendEntries) {
            g.setColor(legendEntry.tail);
            Rectangle2D stringBounds = fm.getStringBounds(legendEntry.head, g);
            g.drawString(legendEntry.head, minX + 5, (int)yOffs);
            g.drawLine(minX - 15, (int)(yOffs - stringBounds.getHeight()/2) + 1, minX, (int)(yOffs - stringBounds.getHeight()/2) + 1);
            yOffs += (padding + stringBounds.getHeight());
        }
        g.setColor(origCol);
    }
}
