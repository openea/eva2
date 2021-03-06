package eva2.tools.chart2d;

import java.awt.*;

public class DBorder extends Insets {

    /**
     *
     */
    private static final long serialVersionUID = -1324717830324606364L;

    public DBorder() {
        this(0, 0, 0, 0);
    }

    public DBorder(int top, int left, int bottom, int right) {
        super(top, left, bottom, right);
    }

    public boolean insert(DBorder b) {
        boolean changed = false;
        if (b.top > top) {
            top = b.top;
            changed = true;
        }
        if (b.bottom > bottom) {
            bottom = b.bottom;
            changed = true;
        }
        if (b.left > left) {
            left = b.left;
            changed = true;
        }
        if (b.right > right) {
            right = b.right;
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DBorder) {
            return super.equals(o);
        }
        return false;
    }

    @Override
    public String toString() {
        return "eva2.tools.chart2d.DBorder[top=" + top + ",left=" + left
                + ",bottom=" + bottom + ",right=" + right + "]";
    }
}