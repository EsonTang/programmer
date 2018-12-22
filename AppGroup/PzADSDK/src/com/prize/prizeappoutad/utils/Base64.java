package com.prize.prizeappoutad.utils;

public class Base64 {
	private static final byte[] decodingTable;
	private static final byte[] encodingTable;

	static {
		byte[] arrayOfByte = new byte[64];
		arrayOfByte[0] = 65;
		arrayOfByte[1] = 66;
		arrayOfByte[2] = 67;
		arrayOfByte[3] = 68;
		arrayOfByte[4] = 69;
		arrayOfByte[5] = 70;
		arrayOfByte[6] = 71;
		arrayOfByte[7] = 72;
		arrayOfByte[8] = 73;
		arrayOfByte[9] = 74;
		arrayOfByte[10] = 75;
		arrayOfByte[11] = 76;
		arrayOfByte[12] = 77;
		arrayOfByte[13] = 78;
		arrayOfByte[14] = 79;
		arrayOfByte[15] = 80;
		arrayOfByte[16] = 81;
		arrayOfByte[17] = 82;
		arrayOfByte[18] = 83;
		arrayOfByte[19] = 84;
		arrayOfByte[20] = 85;
		arrayOfByte[21] = 86;
		arrayOfByte[22] = 87;
		arrayOfByte[23] = 88;
		arrayOfByte[24] = 89;
		arrayOfByte[25] = 90;
		arrayOfByte[26] = 97;
		arrayOfByte[27] = 98;
		arrayOfByte[28] = 99;
		arrayOfByte[29] = 100;
		arrayOfByte[30] = 101;
		arrayOfByte[31] = 102;
		arrayOfByte[32] = 103;
		arrayOfByte[33] = 104;
		arrayOfByte[34] = 105;
		arrayOfByte[35] = 106;
		arrayOfByte[36] = 107;
		arrayOfByte[37] = 108;
		arrayOfByte[38] = 109;
		arrayOfByte[39] = 110;
		arrayOfByte[40] = 111;
		arrayOfByte[41] = 112;
		arrayOfByte[42] = 113;
		arrayOfByte[43] = 114;
		arrayOfByte[44] = 115;
		arrayOfByte[45] = 116;
		arrayOfByte[46] = 117;
		arrayOfByte[47] = 118;
		arrayOfByte[48] = 119;
		arrayOfByte[49] = 120;
		arrayOfByte[50] = 121;
		arrayOfByte[51] = 122;
		arrayOfByte[52] = 48;
		arrayOfByte[53] = 49;
		arrayOfByte[54] = 50;
		arrayOfByte[55] = 51;
		arrayOfByte[56] = 52;
		arrayOfByte[57] = 53;
		arrayOfByte[58] = 54;
		arrayOfByte[59] = 55;
		arrayOfByte[60] = 56;
		arrayOfByte[61] = 57;
		arrayOfByte[62] = 43;
		arrayOfByte[63] = 47;
		encodingTable = arrayOfByte;
		decodingTable = new byte[''];
		for (int i = 0; i < 128; i++)
			decodingTable[i] = -1;
		// for (i = 65; i <= 90; i++)
		for (int i = 65; i <= 90; i++)
			decodingTable[i] = (byte) (i - 65);
		// for (i = 97; i <= 122; i++)
		for (int i = 97; i <= 122; i++)
			decodingTable[i] = (byte) (26 + (i - 97));
		// for (i = 48; i <= 57; i++)
		for (int i = 48; i <= 57; i++)
			decodingTable[i] = (byte) (52 + (i - 48));
		decodingTable[43] = 62;
		decodingTable[47] = 63;
	}

	public static byte[] decode(String paramString) {
		String str = discardNonBase64Chars(paramString);
		byte[] arrayOfByte;
		if (str.charAt(-2 + str.length()) != '=') {
			if (str.charAt(-1 + str.length()) != '=')
				arrayOfByte = new byte[3 * (str.length() / 4)];
			else
				arrayOfByte = new byte[2 + 3 * (-1 + str.length() / 4)];
		} else
			arrayOfByte = new byte[1 + 3 * (-1 + str.length() / 4)];
		int n = 0;
		int m;
		int j;
		int k;
		for (int i1 = 0; n < -4 + str.length(); i1 += 3) {
			int i2 = decodingTable[str.charAt(n)];
			m = decodingTable[str.charAt(n + 1)];
			j = decodingTable[str.charAt(n + 2)];
			k = decodingTable[str.charAt(n + 3)];
			arrayOfByte[i1] = (byte) (i2 << 2 | m >> 4);
			arrayOfByte[(i1 + 1)] = (byte) (m << 4 | j >> 2);
			arrayOfByte[(i1 + 2)] = (byte) (k | j << 6);
			n += 4;
		}
		int i;
		if (str.charAt(-2 + str.length()) != '=') {
			if (str.charAt(-1 + str.length()) != '=') {
				m = decodingTable[str.charAt(-4 + str.length())];
				j = decodingTable[str.charAt(-3 + str.length())];
				k = decodingTable[str.charAt(-2 + str.length())];
				i = decodingTable[str.charAt(-1 + str.length())];
				arrayOfByte[(-3 + arrayOfByte.length)] = (byte) (m << 2 | j >> 4);
				arrayOfByte[(-2 + arrayOfByte.length)] = (byte) (j << 4 | k >> 2);
				arrayOfByte[(-1 + arrayOfByte.length)] = (byte) (i | k << 6);
			} else {
				// j = decodingTable[i.charAt(-4 + i.length())];
				// k = decodingTable[i.charAt(-3 + i.length())];
				// i = decodingTable[i.charAt(-2 + i.length())];
				j = decodingTable[str.charAt(-4 + str.length())];
				k = decodingTable[str.charAt(-3 + str.length())];
				i = decodingTable[str.charAt(-2 + str.length())];
				arrayOfByte[(-2 + arrayOfByte.length)] = (byte) (j << 2 | k >> 4);
				arrayOfByte[(-1 + arrayOfByte.length)] = (byte) (k << 4 | i >> 2);
			}
		} else {
			// j = decodingTable[i.charAt(-4 + i.length())];
			// i = decodingTable[i.charAt(-3 + i.length())];
			j = decodingTable[str.charAt(-4 + str.length())];
			i = decodingTable[str.charAt(-3 + str.length())];
			arrayOfByte[(-1 + arrayOfByte.length)] = (byte) (j << 2 | i >> 4);
		}
		return arrayOfByte;
	}

	public static byte[] decode(byte[] paramArrayOfByte) {
		byte[] arrayOfByte2 = discardNonBase64Bytes(paramArrayOfByte);
		byte[] arrayOfByte1;
		if (arrayOfByte2[(-2 + arrayOfByte2.length)] != 61) {
			if (arrayOfByte2[(-1 + arrayOfByte2.length)] != 61)
				arrayOfByte1 = new byte[3 * (arrayOfByte2.length / 4)];
			else
				arrayOfByte1 = new byte[2 + 3 * (-1 + arrayOfByte2.length / 4)];
		} else
			arrayOfByte1 = new byte[1 + 3 * (-1 + arrayOfByte2.length / 4)];
		int m = 0;
		int j;
		int k;
		for (int i2 = 0; m < -4 + arrayOfByte2.length; i2 += 3) {
			j = decodingTable[arrayOfByte2[m]];
			int i1 = decodingTable[arrayOfByte2[(m + 1)]];
			k = decodingTable[arrayOfByte2[(m + 2)]];
			int n = decodingTable[arrayOfByte2[(m + 3)]];
			arrayOfByte1[i2] = (byte) (j << 2 | i1 >> 4);
			arrayOfByte1[(i2 + 1)] = (byte) (i1 << 4 | k >> 2);
			arrayOfByte1[(i2 + 2)] = (byte) (n | k << 6);
			m += 4;
		}
		int i;
		if (arrayOfByte2[(-2 + arrayOfByte2.length)] != 61) {
			if (arrayOfByte2[(-1 + arrayOfByte2.length)] != 61) {
				m = decodingTable[arrayOfByte2[(-4 + arrayOfByte2.length)]];
				k = decodingTable[arrayOfByte2[(-3 + arrayOfByte2.length)]];
				j = decodingTable[arrayOfByte2[(-2 + arrayOfByte2.length)]];
				i = decodingTable[arrayOfByte2[(-1 + arrayOfByte2.length)]];
				arrayOfByte1[(-3 + arrayOfByte1.length)] = (byte) (m << 2 | k >> 4);
				arrayOfByte1[(-2 + arrayOfByte1.length)] = (byte) (k << 4 | j >> 2);
				arrayOfByte1[(-1 + arrayOfByte1.length)] = (byte) (i | j << 6);
			} else {
				// k = decodingTable[i[(-4 + i.length)]];
				// j = decodingTable[i[(-3 + i.length)]];
				// i = decodingTable[i[(-2 + i.length)]];
				k = decodingTable[arrayOfByte2[(-4 + arrayOfByte2.length)]];
				j = decodingTable[arrayOfByte2[(-3 + arrayOfByte2.length)]];
				i = decodingTable[arrayOfByte2[(-2 + arrayOfByte2.length)]];
				arrayOfByte1[(-2 + arrayOfByte1.length)] = (byte) (k << 2 | j >> 4);
				arrayOfByte1[(-1 + arrayOfByte1.length)] = (byte) (j << 4 | i >> 2);
			}
		} else {
			// j = decodingTable[i[(-4 + i.length)]];
			// i = decodingTable[i[(-3 + i.length)]];
			j = decodingTable[arrayOfByte2[(-4 + arrayOfByte2.length)]];
			i = decodingTable[arrayOfByte2[(-3 + arrayOfByte2.length)]];
			arrayOfByte1[(-1 + arrayOfByte1.length)] = (byte) (j << 2 | i >> 4);
		}
		return arrayOfByte1;
	}

	private static byte[] discardNonBase64Bytes(byte[] paramArrayOfByte) {
		byte[] arrayOfByte1 = new byte[paramArrayOfByte.length];
		int i = 0;
		for (int j = 0; j < paramArrayOfByte.length; j++) {
			if (!isValidBase64Byte(paramArrayOfByte[j]))
				continue;
			int k = i + 1;
			arrayOfByte1[i] = paramArrayOfByte[j];
			i = k;
		}
		byte[] arrayOfByte2 = new byte[i];
		System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, i);
		return arrayOfByte2;
	}

	private static String discardNonBase64Chars(String paramString) {
		StringBuffer localStringBuffer = new StringBuffer();
		int j = paramString.length();
		for (int i = 0; i < j; i++) {
			if (!isValidBase64Byte((byte) paramString.charAt(i)))
				continue;
			localStringBuffer.append(paramString.charAt(i));
		}
		return localStringBuffer.toString();
	}

	public static byte[] encode(byte[] paramArrayOfByte) {
		int i1 = paramArrayOfByte.length % 3;
		byte[] arrayOfByte;
		if (i1 != 0)
			arrayOfByte = new byte[4 * (1 + paramArrayOfByte.length / 3)];
		else
			arrayOfByte = new byte[4 * paramArrayOfByte.length / 3];
		int k = paramArrayOfByte.length - i1;
		int i2 = 0;
		int j;
		int i;
		for (int m = 0; i2 < k; m += 4) {
			j = 0xFF & paramArrayOfByte[i2];
			int n = 0xFF & paramArrayOfByte[(i2 + 1)];
			i = 0xFF & paramArrayOfByte[(i2 + 2)];
			arrayOfByte[m] = encodingTable[(0x3F & j >>> 2)];
			arrayOfByte[(m + 1)] = encodingTable[(0x3F & (j << 4 | n >>> 4))];
			arrayOfByte[(m + 2)] = encodingTable[(0x3F & (n << 2 | i >>> 6))];
			arrayOfByte[(m + 3)] = encodingTable[(i & 0x3F)];
			i2 += 3;
		}
		switch (i1) {
		case 1:
			j = 0xFF & paramArrayOfByte[(-1 + paramArrayOfByte.length)];
			i = 0x3F & j >>> 2;
			j = 0x3F & j << 4;
			arrayOfByte[(-4 + arrayOfByte.length)] = encodingTable[i];
			arrayOfByte[(-3 + arrayOfByte.length)] = encodingTable[j];
			arrayOfByte[(-2 + arrayOfByte.length)] = 61;
			arrayOfByte[(-1 + arrayOfByte.length)] = 61;
			break;
		case 2:
			k = 0xFF & paramArrayOfByte[(-2 + paramArrayOfByte.length)];
			j = 0xFF & paramArrayOfByte[(-1 + paramArrayOfByte.length)];
			i = 0x3F & k >>> 2;
			k = 0x3F & (k << 4 | j >>> 4);
			j = 0x3F & j << 2;
			arrayOfByte[(-4 + arrayOfByte.length)] = encodingTable[i];
			arrayOfByte[(-3 + arrayOfByte.length)] = encodingTable[k];
			arrayOfByte[(-2 + arrayOfByte.length)] = encodingTable[j];
			arrayOfByte[(-1 + arrayOfByte.length)] = 61;
		case 0:
		}
		return arrayOfByte;
	}

	private static boolean isValidBase64Byte(byte paramByte) {
		boolean i = true;
		if (paramByte != 61)
			if ((paramByte >= 0) && (paramByte < 128)) {
				if (decodingTable[paramByte] == -1)
					i = false;
			} else
				i = false;
		return i;
	}

	public static void main(String[] paramArrayOfString) {
		byte[] arrayOfByte = encode("中华人民共和国".getBytes());
	}
}