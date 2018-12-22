package com.android.launcher3;

public class CellAndSpan {
        int x, y;
        int spanX, spanY;
		 int pos;

        public CellAndSpan() {
        }

        public void copy(CellAndSpan copy) {
            copy.x = x;
            copy.y = y;
            copy.spanX = spanX;
            copy.spanY = spanY;
            copy.pos = pos;
        }

        public CellAndSpan(int x, int y, int spanX, int spanY,int pos) {
            this.x = x;
            this.y = y;
            this.spanX = spanX;
            this.spanY = spanY;
            this.pos = pos;
        }

        public String toString() {
            return "(" + x + ", " + y + ": " + spanX + ", " + spanY + ")";
        }

    }