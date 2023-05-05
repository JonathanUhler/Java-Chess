// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// CRC.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;



// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class CRC
//
// A class to handle crc generation, application, and verification for network safety
//
public class CRC {

	public static final int SIZE = 8;
	

	// ====================================================================================================
	// public static int crc32
	//
	// Generates a 4 byte crc from the input string
	// Adapted from C routine: https://rosettacode.org/wiki/CRC-32#Library
	//
	// Arguments--
	//
	//  str: the string to generate a crc for
	//
	// Returns--
	//
	//  A 4 byte checksum as an int that is assumed to be unsigned
	//
	public static int crc32(String str) {
		int crc = 0;
		int[] table = new int[256];
		int rem = 0;
		int octet = 0;

		for (int i = 0; i < table.length; i++) {
			rem = i;
			for (int j = 0; j < 8; j++) {
				if ((rem & 1) != 0) {
					rem >>>= 1; // 3 ">" for unsigned shift
					rem ^= 0xedb88320;
				}
				else
					rem >>>= 1; // 3 ">" for unsigned shift
			}
			table[i] = rem;
		}

		crc = ~crc;
		for (int i = 0; i < str.length(); i++) {
			char p = str.substring(i).charAt(0);
			octet = (int) p;
			crc = (crc >>> 8) ^ table[(crc & 0xff) ^ octet];
		}  
		return ~crc;
	}
	// end: public static int crc32


	// ====================================================================================================
	// public static String get
	//
	// Generates a 4 byte crc as a string
	//
	// Arguments--
	//
	//  str: the string to generate a crc for
	//
	// Returns--
	//
	//  A 4 byte checksum as a byte array
	//
	public static String get(String str) {
		int crc32 = CRC.crc32(str);

		/*
		// Get each byte of the crc integer in little endian format, then convert to char
		// and append to a string
		String crc32str = "";
		for (int i = 0; i < 4; i++) {
			char c = (char) (crc32 & 0xff);
			crc32str += c;
			crc32 >>>= 8;
		}

		return crc32str;
		*/

		String hexString = Integer.toHexString(crc32);
		return String.format("%1$" + CRC.SIZE + "s", hexString).replace(" ", "0");
	}
	// end: public static String get


	// ====================================================================================================
	// public static boolean check
	//
	// Checks for a valid crc on a string, asumming the last four characters of the string, when converted
	// to a 4 byte word, represent the crc value for the remainder of the string.
	//
	// Arguments--
	//
	//  str: the string to check the crc of
	//
	// Returns--
	//
	//  Whether the given crc included with "str" matched the generated crc
	//
	public static boolean check(String str) {
		if (str.length() < CRC.SIZE) {
			Log.stdlog(Log.ERROR, "CRC", "check called with an invalid string (too short)");
			Log.stdlog(Log.ERROR, "CRC", "\t" + str);
			return false;
		}

		String given = str.substring(str.length() - CRC.SIZE);
		String raw = str.substring(0, str.length() - CRC.SIZE);
		String gen = CRC.get(raw);

		if (given.length() != CRC.SIZE || gen.length() != CRC.SIZE) {
			Log.stdlog(Log.ERROR, "CRC", "given crc and/or generated crc have invalid length");
			Log.stdlog(Log.ERROR, "CRC", "\tgiven: " + given);
			Log.stdlog(Log.ERROR, "CRC", "\tgen:   " + gen);
			return false;
		}

		boolean passed = gen.equals(given);
		if (!passed) {
			Log.stdlog(Log.ERROR, "CRC", "CRC check failed, given was not equal to generated");
			Log.stdlog(Log.ERROR, "CRC", "\tfull msg: " + str);
			Log.stdlog(Log.ERROR, "CRC", "\tgiven crc: " + given);
			Log.stdlog(Log.ERROR, "CRC", "\tgen crc:   " + gen);
		}
		return passed;
	}
	// end: public static boolean check
	
}
// end: public class CRC