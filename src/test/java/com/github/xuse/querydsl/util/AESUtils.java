package com.github.xuse.querydsl.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 128-bit AESUtils/ECB/PKCS5Padding 加解密
 */
public final class AESUtils {

	private static final Logger LOG = LoggerFactory.getLogger(AESUtils.class);

	/** AES加密参数VI */
	private static byte[] VI = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 0, 0, 0, 0, 0, 0, 0, 0 };

	private static KeyGenerator keyGenerator;

	static {
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
		} catch (NoSuchAlgorithmException ex) {
			LOG.error("AESUtils.static{}", ex);
		}
	}

	/**
	 * 加密
	 *
	 * @param data 待加密数据
	 * @param key  密钥
	 * @return 加密后数据
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		Key k = new SecretKeySpec(key, "AES");
		// 对于待加密解密的数据的填充方式：NoPadding、PKCS5Padding、SSL3Padding，默认填充方式为，PKCS5Padding
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec iv = new IvParameterSpec(VI);
		cipher.init(Cipher.ENCRYPT_MODE, k, iv);
		return cipher.doFinal(data);
	}

	/**
	 * 解密
	 *
	 * @param data 待解密的数据
	 * @param key  密钥
	 * @return 解密后的数据
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] decrypt(byte[] data, byte[] key) throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

//    	@brief AES 加密 对齐方式：PKCS5Padding ;加密模式：cbc128;vi:16字节长数组，初始化0-7：01234567

		Key k = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CTR/ISO10126Padding");
		IvParameterSpec iv = new IvParameterSpec(VI);
		cipher.init(Cipher.DECRYPT_MODE, k, iv);
		return cipher.doFinal(data);
	}

	public static byte[] decrypt111(byte[] data, int offset, int length, byte[] key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException {
		Key k = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/OFB/ISO10126Padding");
		IvParameterSpec iv = new IvParameterSpec(VI);
		cipher.init(Cipher.DECRYPT_MODE, k, iv);
		return cipher.doFinal(data, offset, length);
	}

//	public static void main(String[] args) throws Exception {
//		String text = "1358818200588182";
//		printSec(text);
//		printSec("135");
//		printSec("1358");
//		printSec("13588");
//		printSec("13588182105");
//	}
//
//	private static void printSec(String soruce) throws InvalidKeyException, IllegalBlockSizeException,
//			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
//		String key = "8807123456785998";
//		byte[] data = encrypt(soruce.getBytes(), key.getBytes());
//		System.out.println(JefBase64.encode(data));
//	}

	//加密字段检索方案
	//内存映射法（不适合）
	//MySQL DES函数,DES_ENCRYPT函数加密，使用DES_DECRYPT函数解密.（不支持FBI，性能差）
	//本地H2索引映射法（依然有明文落地，且容易出现数据一致性问题）
	//本地Lucene索引法（基于二元或三元分次加密，本地容易出现数据一致性问题。）
	//三元元分词加密保存法。(使用专用索引表：会出现Join，匹配固定3字符时为等值查询)
	//三元分词加密保存法。(使用扩展字段：字段较长，需要使用LIKE %key%，数据较大性能依然不佳。优点：匹配4、5等字符时可一次查询。)
	
	
	
	
}
