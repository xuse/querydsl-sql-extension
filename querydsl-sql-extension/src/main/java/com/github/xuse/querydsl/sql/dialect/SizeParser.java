package com.github.xuse.querydsl.sql.dialect;

public interface SizeParser {
	default int size(int size, int digits) {
		return size;
	}

	default int digits(int size, int digits) {
		return digits;
	}

	public static final SizeParser DEFAULT = new SizeParser() {
	};

	public static final SizeParser MYSQL_TIMESTAMP = new SizeParser() {
		@Override
		public int size(int size, int digits) {
			if (size > 19) {
				return size - 20;
			} else {
				return size - 19;
			}
		}
	};

	public static final SizeParser MYSQL_TIME = new SizeParser() {
		@Override
		public int size(int size, int digits) {
			if (size > 9) {
				return size - 9;
			} else {
				return size - 8;
			}
		}
	};
	
	
	public static final SizeParser TIME_DIGIT_AS_SIZE = new SizeParser() {
		@Override
		public int size(int size, int digits) {
			return digits;
		}
	};
}
