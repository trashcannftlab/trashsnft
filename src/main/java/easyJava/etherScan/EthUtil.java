package easyJava.etherScan;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

public class EthUtil {

	public static String random() {
		
		BigInteger salt = new BigInteger(256, new Random());
		
		return "0x" + salt.toString(16);
	}
	
	public static String bigIntToHex(BigInteger bigInteger) {
		
		return "0x"+bigInteger.toString(16);
		
	}
	
	
	public static BigInteger hexToBigInt(String value) {
		
		if(value.startsWith("0x")) {
			return new BigInteger(value.substring(2), 16);
		}
		
		return new BigInteger(value, 16);
		
	}
	
	
	public static byte[] hexToBytes(String value) {
		
		if(value.startsWith("0x")) {
			value = value.substring(2);
		}
		
		int len = value.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
	                             + Character.digit(value.charAt(i+1), 16));
	    }
	    return data;
		
	}
	
	
	
	public static BigDecimal bigIntToDecimal(BigInteger bigInt, int digits) {
		
		BigDecimal big = new BigDecimal(10);		
		big = big.pow(digits);
		
		return (new BigDecimal(bigInt)).divide(big);
		
	}
	
	public static BigInteger decimalToBigInt( BigDecimal decimal, int digits) {
		
		BigDecimal big = new BigDecimal(10);		
		big = big.pow(digits);
		
		return decimal.multiply(big).toBigInteger();
		
	}
	
	
	public static BigDecimal random(BigDecimal from , BigDecimal to) {
		
		int fromScale = from.stripTrailingZeros().scale();
		int toScale = to.stripTrailingZeros().scale();
		
		int scale = Math.max(fromScale, toScale);
				
		Random random = new Random();
		
		BigDecimal randomDecimal = to.subtract(from).multiply(BigDecimal.valueOf(random.nextDouble())).add(from);
		
		randomDecimal = randomDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
		
		return randomDecimal;
		
	}
	
	
}
