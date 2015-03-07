package br.ufrj.ppgi.huffmansequential;

public class BitUtility {
	
	public static void setBit(byte[] byteArray, int pos, boolean s) {
		int byteIndex = pos / Defines.bitsCodification;
		pos = 7 - (pos % Defines.bitsCodification);
		
		if (s) {
			byteArray[byteIndex] |= 1 << pos;
		}
		else {
			byteArray[byteIndex] &= ~(1 << pos);
		}
	}

	public static boolean checkBit(byte[] byteArray, int pos) {
		int byteIndex = pos / Defines.bitsCodification;
		pos = 7 - (pos % Defines.bitsCodification);
		
		int bit = byteArray[byteIndex] & (1 << pos);
		if (bit > 0) {
			return true;
		}

		return false;
	}

}
