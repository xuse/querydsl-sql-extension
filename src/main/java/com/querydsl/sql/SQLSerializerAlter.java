package com.querydsl.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.types.Null;

/**
 * 扩展了官方的SQLSerializer的一个行为。
 * <hr>
 * 官方SQLSerializer，针对多值的常量Collection，会转换为 (?,?,?)形式。
 * 但是在实现一些数据库的变长函数时，如下所示——
 * <code>
 * MYSQL的
 * <li>JSON_CONTAINS_PATH(json_doc, one_or_all, path[, path] ...)</li>
 * <li>Oracle的TRANSLATE(text,char1, char2 ....)</li>
 * </code>
 * 此时，两侧强制加上的括号导致上述函数表达式无法正常生成。
 * <p />
 * 为此，扩展SQL序列化实现，当包装的常量为Object[]时，展开为不带括号的逗号分隔表达式形式。
 * @author jiyi
 *
 */
public class SQLSerializerAlter extends SQLSerializer {

	public SQLSerializerAlter(Configuration conf, boolean dml) {
		super(conf, dml);
	}

	public SQLSerializerAlter(Configuration conf) {
		super(conf);
	}

	@SuppressWarnings({"rawtypes"})
	@Override
	public void visitConstant(Object constant) {
		//新增支持的常量表达形式，如果是对象数组，就转成不带小括号的多值。
		String leftBucket="(";
		String rightBucket=")";
		if(constant instanceof Object[]) {
			constant = Arrays.asList((Object[])constant);
			leftBucket=rightBucket="";
		}
		if (useLiterals) {
			if (constant instanceof Collection) {
				append(leftBucket);
				Iterator iter=((Collection) constant).iterator();
				if(iter.hasNext()) {
					append(configuration.asLiteral(iter.next()));
					while (iter.hasNext()) {
						append(COMMA);
						append(configuration.asLiteral(iter.next()));
					}
				}
				append(rightBucket);
			} else {
				append(configuration.asLiteral(constant));
			}
		} else if (constant instanceof Collection) {
			append(leftBucket);
			Iterator iter=((Collection) constant).iterator();
			if(iter.hasNext()) {
				serializeConstant(constants.size() + 1, null);
				constants.add(iter.next());
				//FIXME 官方代码。这个好像是一个修补BUG的逻辑，不知道具体什么时候会发生。我怀疑这是没用的代码。
				if (constantPaths.size() < constants.size()) {
					constantPaths.add(null);
				}
				while (iter.hasNext()) {
					append(COMMA);
					serializeConstant(constants.size() + 1, null);
					constants.add(iter.next());
				}
			}
			append(rightBucket);
			int size = ((Collection) constant).size() - 1;
			Path<?> lastPath = constantPaths.peekLast();
			for (int i = 0; i < size; i++) {
				constantPaths.add(lastPath);
			}
		} else {
			if (stage == Stage.SELECT && !Null.class.isInstance(constant)
					&& configuration.getTemplates().isWrapSelectParameters()) {
				String typeName = configuration.getTypeNameForCast(constant.getClass());
				Expression type = Expressions.constant(typeName);
				super.visitOperation(constant.getClass(), SQLOps.CAST, Arrays.<Expression<?>>asList(Q, type));
			} else {
				serializeConstant(constants.size() + 1, null);
			}
			constants.add(constant);
			if (constantPaths.size() < constants.size()) {
				constantPaths.add(null);
			}
		}
	}

}
