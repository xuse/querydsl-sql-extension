package io.github.xuse.querydsl.sql.extension.code.generate.ast;

import java.io.File;

public interface JavaUnitParser {
	JavaUnit parse(File file,String charset);
}
