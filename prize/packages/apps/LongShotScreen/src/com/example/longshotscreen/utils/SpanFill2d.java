package com.example.longshotscreen.utils;

import android.graphics.Point;
import java.util.Stack;

public class SpanFill2d {
	private int M_BOUND_COLOR = 0;
	protected ImageMap2d bmp;
	protected ImageMap2d bmpOper;
	private Stack<Span> container = new Stack();
	protected int count = 0;
	public FlagMap2d flagsMap;
	private int mBottom;
	private int mLeft;
	private int mRight;
	private int mTop;

	private void CheckRange(int paramInt1, int paramInt2, int paramInt3,
			int paramInt4) {
		for (int i = paramInt1; i <= paramInt2; ++i) {
			if ((this.flagsMap.GetFlagOn(i, paramInt3) != 0)
					|| (IncludePredicate(i, paramInt3)))
				continue;
			int j = i;
			int k;
			for (k = i + 1; (k <= paramInt2)
					&& (this.flagsMap.GetFlagOn(k, paramInt3) == 0)
					&& (!IncludePredicate(k, paramInt3)); ++k)
				;
			int l = k - 1;
			Span localSpan = new Span();
			localSpan.XLeft = j;
			localSpan.XRight = l;
			localSpan.Y = paramInt3;
			if ((j == paramInt1) && (l == paramInt2))
				;
			for (localSpan.Extended = 4;; localSpan.Extended = 3)
				while (true) {
					this.container.push(localSpan);
					i = l + 1;

					localSpan.ParentDirection = paramInt4;
					for (int i1 = j; i1 <= l; ++i1) {
						this.flagsMap.SetFlagOn(i1, paramInt3, (byte) 0);
						Process(this.bmpOper, i1, paramInt3, 0);
					}
					if (l == paramInt2)
						localSpan.Extended = 2;
					if (j != paramInt1)
						break;
					localSpan.Extended = 1;
				}
		}
	}

	private boolean IncludePredicate(int paramInt1, int paramInt2) {
		boolean i = false;
		if ((!this.bmp.IsBitmapRecycle())
				&& (this.bmp.GetPixel(paramInt1, paramInt2) == this.M_BOUND_COLOR)) {
			i = true;
		}
		return i;
	}

	public void ExcuteSpanFill(ImageMap2d paramImageMap2d1,
			ImageMap2d paramImageMap2d2, Point paramPoint) throws Exception {
		if (!(paramImageMap2d1.mWidth == paramImageMap2d2.mWidth)
				&& (paramImageMap2d1.mHeight == paramImageMap2d2.mHeight))
			return;
		this.bmp = paramImageMap2d1;
		this.bmpOper = paramImageMap2d2;
		this.flagsMap = new FlagMap2d(paramImageMap2d1.mWidth,
				paramImageMap2d1.mHeight);
		Process(this.bmpOper, paramPoint.x, paramPoint.y, 0);
		this.flagsMap.SetFlagOn(paramPoint.x, paramPoint.y, (byte) 1);
		Span localSpan1 = new Span();
		localSpan1.XLeft = paramPoint.x;
		localSpan1.XRight = paramPoint.x;
		localSpan1.Y = paramPoint.y;
		localSpan1.ParentDirection = 5;
		localSpan1.Extended = 4;
		this.container.push(localSpan1);
		if (!this.container.empty()) {
			Span localSpan2 = (Span) this.container.pop();
			if (localSpan2.Extended == 3) {
				if (localSpan2.ParentDirection == 2) {
					if (-1 + localSpan2.Y >= this.mTop)
						CheckRange(localSpan2.XLeft, localSpan2.XRight, -1
								+ localSpan2.Y, 2);
				}
				if (localSpan2.ParentDirection == 1) {
					if (1 + localSpan2.Y < this.mBottom)
						CheckRange(localSpan2.XLeft, localSpan2.XRight,
								1 + localSpan2.Y, 1);
				}
				// throw new Exception();
			}
			if (localSpan2.Extended == 4) {
				int k = FindXLeft(localSpan2.XLeft, localSpan2.Y);
				int l = FindXRight(localSpan2.XRight, localSpan2.Y);
				if (localSpan2.ParentDirection == 2) {
					if (-1 + localSpan2.Y >= this.mTop)
						CheckRange(k, l, -1 + localSpan2.Y, 2);
					if (1 + localSpan2.Y < this.mBottom) {
						if (k != localSpan2.XLeft)
							CheckRange(k, localSpan2.XLeft, 1 + localSpan2.Y, 1);
					}
					if (localSpan2.XRight != l)
						CheckRange(localSpan2.XRight, l, 1 + localSpan2.Y, 1);
				}
				if (localSpan2.ParentDirection == 1) {
					if (1 + localSpan2.Y < this.mBottom)
						CheckRange(k, l, 1 + localSpan2.Y, 1);
					if (-1 + localSpan2.Y >= this.mTop) {
						if (k != localSpan2.XLeft)
							CheckRange(k, localSpan2.XLeft, -1 + localSpan2.Y,
									2);
					}
					if (localSpan2.XRight != l)
						CheckRange(localSpan2.XRight, l, -1 + localSpan2.Y, 2);
				}
				if (localSpan2.ParentDirection == 5) {
					if (1 + localSpan2.Y < this.mBottom)
						CheckRange(k, l, 1 + localSpan2.Y, 1);
					if (-1 + localSpan2.Y >= this.mTop)
						CheckRange(k, l, -1 + localSpan2.Y, 2);
				}
				// throw new Exception();
			}
			if (localSpan2.Extended == 1) {
				int j = FindXLeft(localSpan2.XLeft, localSpan2.Y);
				if (localSpan2.ParentDirection == 2) {
					if (-1 + localSpan2.Y >= this.mTop)
						CheckRange(j, localSpan2.XRight, -1 + localSpan2.Y, 2);
					if ((1 + localSpan2.Y < this.mBottom)
							&& (j != localSpan2.XLeft))
						CheckRange(j, localSpan2.XLeft, 1 + localSpan2.Y, 1);
				}
				if (localSpan2.ParentDirection == 1) {
					if (1 + localSpan2.Y < this.mBottom)
						CheckRange(j, localSpan2.XRight, 1 + localSpan2.Y, 1);
					if ((-1 + localSpan2.Y >= this.mTop)
							&& (j != localSpan2.XLeft))
						CheckRange(j, localSpan2.XLeft, -1 + localSpan2.Y, 2);
				}
				// throw new Exception();
			}
			if (localSpan2.Extended == 2) {
				int i = FindXRight(localSpan2.XRight, localSpan2.Y);
				if (localSpan2.ParentDirection == 2) {
					if (-1 + localSpan2.Y >= this.mTop)
						CheckRange(localSpan2.XLeft, i, -1 + localSpan2.Y, 2);
					if ((1 + localSpan2.Y < this.mBottom)
							&& (localSpan2.XRight != i))
						CheckRange(localSpan2.XRight, i, 1 + localSpan2.Y, 1);
				}
				if (localSpan2.ParentDirection != 1)
					throw new Exception();
				if (1 + localSpan2.Y < this.mBottom)
					CheckRange(localSpan2.XLeft, i, 1 + localSpan2.Y, 1);
				if ((-1 + localSpan2.Y > this.mTop) && (localSpan2.XRight != i))
					CheckRange(localSpan2.XRight, i, -1 + localSpan2.Y, 2);
			}
		}
	}

	protected int FindXLeft(int paramInt1, int paramInt2) {
		for (int i = paramInt1 - 1;; --i) {
			if (((i > -1 + this.mLeft) && (1 == this.flagsMap.GetFlagOn(i,
					paramInt2))) || (IncludePredicate(i, paramInt2)))
				return i + 1;
			this.flagsMap.SetFlagOn(i, paramInt2, (byte) 1);
			Process(this.bmpOper, i, paramInt2, 0);
		}
	}

	protected int FindXRight(int paramInt1, int paramInt2) {
		for (int i = paramInt1 + 1;; ++i) {
			if (((i < this.mRight) && (1 == this.flagsMap.GetFlagOn(i,
					paramInt2))) || (IncludePredicate(i, paramInt2)))
				return i - 1;
			this.flagsMap.SetFlagOn(i, paramInt2, (byte) 1);
			Process(this.bmpOper, i, paramInt2, 0);
		}
	}

	void Process(ImageMap2d paramImageMap2d, int paramInt1, int paramInt2,
			int paramInt3) {
		this.count = (1 + this.count);
		if ((paramImageMap2d == null) || (paramImageMap2d.IsBitmapRecycle()))
			return;
		paramImageMap2d.SetPixel(paramInt1, paramInt2, paramInt3);
	}

	public void setBoundColor(int paramInt) {
		this.M_BOUND_COLOR = paramInt;
	}

	public void setRang(int paramInt1, int paramInt2, int paramInt3,
			int paramInt4) {
		this.mLeft = paramInt1;
		this.mTop = paramInt2;
		this.mRight = paramInt3;
		this.mBottom = paramInt4;
	}

	public class FlagMap2d {
		public byte[] flags;
		public int height;
		public int width;

		public FlagMap2d(int paramInt1, int arg3) {
			this.width = paramInt1;
			int i = 0;
			this.height = i;
			this.flags = new byte[paramInt1 * i];
		}

		public byte GetFlagOn(int paramInt1, int paramInt2) {
			return this.flags[(paramInt1 + paramInt2 * this.width)];
		}

		public void SetFlagOn(int paramInt1, int paramInt2, byte paramByte) {
			this.flags[(paramInt1 + paramInt2 * this.width)] = paramByte;
		}
	}

	public class Span {
		public int Extended;
		public int ParentDirection;
		public int XLeft;
		public int XRight;
		public int Y;

		public Span() {
		}
	}
}
