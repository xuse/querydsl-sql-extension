package com.github.xuse.querydsl.entity;

import com.github.xuse.querydsl.types.AbstractBase64EncryptStringType;
import com.google.common.base.Charsets;

public class AESEncryptedField extends AbstractBase64EncryptStringType {

	@Override
	protected byte[] encrypt(String value) {
		return value.getBytes(Charsets.UTF_8);
	}

	@Override
	protected String decrypt(byte[] value) {
		return new String(value,Charsets.UTF_8);
	}

}
