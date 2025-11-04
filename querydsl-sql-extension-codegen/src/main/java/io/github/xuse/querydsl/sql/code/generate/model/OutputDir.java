package io.github.xuse.querydsl.sql.code.generate.model;

public enum OutputDir {
	DIR_TARGET("target/generated-sources/"), 
	DIR_MAIN("src/main/java/"),
	DIR_TEST("src/test/java/");

	public final String path;

	private OutputDir(String path) {
		this.path = path;
	}
}
