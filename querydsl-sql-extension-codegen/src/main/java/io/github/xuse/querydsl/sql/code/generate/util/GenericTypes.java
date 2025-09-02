package io.github.xuse.querydsl.sql.code.generate.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.github.xuse.querydsl.util.ArrayUtils;
import com.github.xuse.querydsl.util.Assert;

@SuppressWarnings("rawtypes")
public class GenericTypes {
	/** Cache from Class to TypeVariable Map */
	private static final Map<Class, Reference<Map<TypeVariable, Type>>> typeVariableCache = Collections.synchronizedMap(new WeakHashMap<Class, Reference<Map<TypeVariable, Type>>>());

	/**
	 * 将所有泛型边界和泛型边界解析为边界的具体类型
	 * @param context
	 * @param genericType
	 * @return type
	 */
	public static Type resolve(Type genericType,Type context) {
		return getBoundType(genericType, context == null ? null : new ClassEx(context));
	}
	
	public static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
		// this implementation is made a little more complicated in an attempt
		// to avoid object-creation
		while (true) {
			if (toResolve instanceof TypeVariable) {
				TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
				toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
				if (toResolve == typeVariable) {
					return toResolve;
				}

			} else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
				Class<?> original = (Class<?>) toResolve;
				Type componentType = original.getComponentType();
				Type newComponentType = resolve(context, contextRawType, componentType);
				return componentType == newComponentType ? original : TypeUtils.genericArrayType(newComponentType);

			} else if (toResolve instanceof GenericArrayType) {
				GenericArrayType original = (GenericArrayType) toResolve;
				Type componentType = original.getGenericComponentType();
				Type newComponentType = resolve(context, contextRawType, componentType);
				return componentType == newComponentType ? original : TypeUtils.genericArrayType(newComponentType);

			} else if (toResolve instanceof ParameterizedType) {
				ParameterizedType original = (ParameterizedType) toResolve;
				Type ownerType = original.getOwnerType();
				Type newOwnerType = resolve(context, contextRawType, ownerType);
				boolean changed = newOwnerType != ownerType;

				Type[] args = original.getActualTypeArguments();
				for (int t = 0, length = args.length; t < length; t++) {
					Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
					if (resolvedTypeArgument != args[t]) {
						if (!changed) {
							args = args.clone();
							changed = true;
						}
						args[t] = resolvedTypeArgument;
					}
				}
				return changed ? TypeUtils.parameterizeWithOwner(newOwnerType, getRawClass(original.getRawType()), args) : original;

			} else if (toResolve instanceof WildcardType) {
				WildcardType original = (WildcardType) toResolve;
				Type[] originalLowerBound = original.getLowerBounds();
				Type[] originalUpperBound = original.getUpperBounds();

				if (originalLowerBound.length == 1) {
					Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
					if (lowerBound != originalLowerBound[0]) {
						return TypeUtils.wildcardType().withLowerBounds(lowerBound).build();
					}
				} else if (originalUpperBound.length == 1) {
					Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
					if (upperBound != originalUpperBound[0]) {
						return TypeUtils.wildcardType().withUpperBounds(upperBound).build();
					}
				}
				return original;

			} else {
				return toResolve;
			}
		}
	}

	
	/**
	 * Resolve the specified generic type against the given TypeVariable map.
	 * 
	 * @param genericType
	 *            the generic type to resolve
	 * @param typeVariableMap
	 *            the TypeVariable Map to resolved against
	 * @return the type if it resolves to a Class, or <code>Object.class</code>
	 *         otherwise
	 */
	static Class resolve2(Type genericType, Map<TypeVariable, Type> typeVariableMap) {
		Type rawType = getRawType(genericType, typeVariableMap);
		return (rawType instanceof Class ? (Class) rawType : Object.class);
	}

	/**
	 * @param type
	 * @return Returns true if this type is an array.
	 */
	public static boolean isArray(Type type) {
		return type instanceof GenericArrayType || (type instanceof Class && ((Class<?>) type).isArray());
	}

	/**
	 * 判断一个类型是否为Collection
	 * 
	 * @param type
	 * @return true if type is a collection type.
	 */
	public static boolean isCollection(Type type) {
		if (type instanceof GenericArrayType) {
			return false;
		} else if (type instanceof Class) {
			Class<?> rawType = (Class<?>) type;
			return Collection.class.isAssignableFrom(rawType);
		} else {
			return Collection.class.isAssignableFrom(getRawClass(type));
		}
	}

	/**
	 * 生成非泛型的数组class
	 * 
	 * @param componentType componentType
	 * @return class of array
	 */
	public static Class<?> newArrayClass(Type componentType) {
		return Array.newInstance(getRawClass(componentType), 0).getClass();
	}
	/**
	 * Determine the raw type for the given generic parameter type.
	 * 
	 * @param genericType
	 *            the generic type to resolve
	 * @param typeVariableMap
	 *            the TypeVariable Map to resolved against
	 * @return the resolved raw type
	 */
	static Type getRawType(Type genericType, Map<TypeVariable, Type> typeVariableMap) {
		Type resolvedType = genericType;
		if (genericType instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) genericType;
			resolvedType = typeVariableMap.get(tv);
			if (resolvedType == null) {
				resolvedType = extractBoundForTypeVariable(tv);
			}
		}
		if (resolvedType instanceof ParameterizedType) {
			return ((ParameterizedType) resolvedType).getRawType();
		} else {
			return resolvedType;
		}
	}
	
	static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
		Class<?> declaredByRaw = declaringClassOf(unknown);

		// we can't reduce this further
		if (declaredByRaw == null) {
			return unknown;
		}

		Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
		if (declaredBy instanceof ParameterizedType) {
			int index = ArrayUtils.indexOf(declaredByRaw.getTypeParameters(), unknown);
			return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
		}

		return unknown;
	}
	
	/**
	 * Returns the generic supertype for {@code supertype}. For example, given a
	 * class {@code IntegerSet}, the result for when supertype is
	 * {@code Set.class} is {@code Set<Integer>} and the result when the
	 * supertype is {@code Collection.class} is {@code Collection<Integer>}.
	 */
	static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
		if (toResolve == rawType) {
			return context;
		}

		// we skip searching through interfaces if unknown is an interface
		if (toResolve.isInterface()) {
			Class<?>[] interfaces = rawType.getInterfaces();
			for (int i = 0, length = interfaces.length; i < length; i++) {
				if (interfaces[i] == toResolve) {
					return rawType.getGenericInterfaces()[i];
				} else if (toResolve.isAssignableFrom(interfaces[i])) {
					return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
				}
			}
		}

		// check our supertypes
		if (!rawType.isInterface()) {
			while (rawType != Object.class) {
				Class<?> rawSupertype = rawType.getSuperclass();
				if (rawSupertype == toResolve) {
					return rawType.getGenericSuperclass();
				} else if (toResolve.isAssignableFrom(rawSupertype)) {
					return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
				}
				rawType = rawSupertype;
			}
		}

		// we can't resolve this further
		return toResolve;
	}
	
	/**
	 * Returns the declaring class of {@code typeVariable}, or {@code null} if
	 * it was not declared by a class.
	 */
	private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
		GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
		return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
	}

	
	public static Class<?> getRawClass(Type type) {
		if (type instanceof Class<?>) {
			// type is a normal class.
			return (Class<?>) type;

		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			// I'm not exactly sure why getRawType() returns Type instead of
			// Class.
			// Neal isn't either but suspects some pathological case related
			// to nested classes exists.
			Type rawType = parameterizedType.getRawType();
			Assert.isTrue(rawType instanceof Class);
			return (Class<?>) rawType;

		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			return Array.newInstance(getRawClass(componentType), 0).getClass();

		} else if (type instanceof TypeVariable) {
			// we could use the variable's bounds, but that won't work if there
			// are multiple.
			// having a raw type that's more general than necessary is okay
			return Object.class;

		} else if (type instanceof WildcardType) {
			return getRawClass(((WildcardType) type).getUpperBounds()[0]);

		} else {
			String className = type == null ? "null" : type.getClass().getName();
			throw new IllegalArgumentException("Expected a Class, ParameterizedType, or " + "GenericArrayType, but <"
					+ type + "> is of type " + className);
		}
	}

	/**
	 * Jiyi 编写的计算泛型边界，将泛型变量、边界描述、全部按照允许的最左边界进行计算
	 * 
	 * @param type
	 * @param cw
	 * @return type
	 */
	public static Type getBoundType(Type type, ClassEx cw) {
		if (type instanceof TypeVariable<?>) {
			TypeVariable<?> tv = (TypeVariable<?>) type;
			Type real = cw.getImplType(tv);
			if (real != null) {
				return getBoundType(real, cw);
			}
			real = tv.getBounds()[0];
			return getBoundType(real, cw);
		} else if (type instanceof WildcardType) {
			WildcardType wild = (WildcardType) type;
			return getBoundType(wild.getUpperBounds()[0], cw);
		}
		if (isImplType(type)) {
			return type;
		}
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] types = pt.getActualTypeArguments();
			for (int i = 0; i < types.length; i++) {
				types[i] = getBoundType(types[i], cw);
			}
			Class<?> raw = (Class<?>) getBoundType(pt.getRawType(), cw);
			return TypeUtils.parameterize(raw, types);
		} else if (type instanceof GenericArrayType) {
			GenericArrayType at = (GenericArrayType) type;
			return TypeUtils.genericArrayType(getBoundType(at.getGenericComponentType(), cw));
		}
		return null;
	}

	// 是否确定类型的泛型常量，还是类型不确定的泛型变量。
	private static boolean isImplType(Type type) {
		if (type instanceof Class<?>)
			return true;
		if (type instanceof GenericArrayType) {
			return isImplType(((GenericArrayType) type).getGenericComponentType());
		} else if (type instanceof ParameterizedType) {
			for (Type sub : ((ParameterizedType) type).getActualTypeArguments()) {
				if (!isImplType(sub)) {
					return false;
				}
			}
			return true;
		} else if (type instanceof TypeVariable<?>) {
			return false;
		} else if (type instanceof WildcardType) {
			//? 这个返回不太合理。
			return false;
		}
		throw new IllegalArgumentException();
	}

	public static Type getComponentType(Type type) {
		if (type instanceof GenericArrayType) {
			return ((GenericArrayType) type).getGenericComponentType();
		} else if (type instanceof Class) {
			Class<?> rawType = (Class<?>) type;
			if (rawType.isArray()) {
				return rawType.getComponentType();
			} else if (Collection.class.isAssignableFrom(rawType)) {
				// 此时泛型类型已经丢失，只能返Object
				return Object.class;
			}
		} else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			Type rawType = pType.getRawType();
			if (isCollection(rawType)) {
				return pType.getActualTypeArguments()[0];
			}
		}
		return null;
	}
	
	/**
	 * Build a mapping of {@link TypeVariable#getName TypeVariable names} to
	 * concrete {@link Class} for the specified {@link Class}. Searches all
	 * super types, enclosing types and interfaces.
	 */
	static Map<TypeVariable, Type> getTypeVariableMap(Class clazz) {
		Reference<Map<TypeVariable, Type>> ref = typeVariableCache.get(clazz);
		Map<TypeVariable, Type> typeVariableMap = (ref != null ? ref.get() : null);

		if (typeVariableMap == null) {
			typeVariableMap = new HashMap<TypeVariable, Type>();

			// interfaces
			extractTypeVariablesFromGenericInterfaces(clazz.getGenericInterfaces(), typeVariableMap);

			// super class
			Type genericType = clazz.getGenericSuperclass();
			Class type = clazz.getSuperclass();
			while (type != null && !Object.class.equals(type)) {
				if (genericType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genericType;
					populateTypeMapFromParameterizedType(pt, typeVariableMap);
				}
				extractTypeVariablesFromGenericInterfaces(type.getGenericInterfaces(), typeVariableMap);
				genericType = type.getGenericSuperclass();
				type = type.getSuperclass();
			}

			// enclosing class
			type = clazz;
			while (type.isMemberClass()) {
				genericType = type.getGenericSuperclass();
				if (genericType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genericType;
					populateTypeMapFromParameterizedType(pt, typeVariableMap);
				}
				type = type.getEnclosingClass();
			}

			typeVariableCache.put(clazz, new WeakReference<Map<TypeVariable, Type>>(typeVariableMap));
		}

		return typeVariableMap;
	}
	
	private static void extractTypeVariablesFromGenericInterfaces(Type[] genericInterfaces, Map<TypeVariable, Type> typeVariableMap) {
		for (Type genericInterface : genericInterfaces) {
			if (genericInterface instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericInterface;
				populateTypeMapFromParameterizedType(pt, typeVariableMap);
				if (pt.getRawType() instanceof Class) {
					extractTypeVariablesFromGenericInterfaces(((Class) pt.getRawType()).getGenericInterfaces(), typeVariableMap);
				}
			} else if (genericInterface instanceof Class) {
				extractTypeVariablesFromGenericInterfaces(((Class) genericInterface).getGenericInterfaces(), typeVariableMap);
			}
		}
	}
	
	/**
	 * Read the {@link TypeVariable TypeVariables} from the supplied
	 * {@link ParameterizedType} and add mappings corresponding to the
	 * {@link TypeVariable#getName TypeVariable name} -> concrete type to the
	 * supplied {@link Map}.
	 * <p>
	 * Consider this case:
	 * 
	 * <pre class="code>
	 * public interface Foo<S, T> {
	 *  ..
	 * }
	 * 
	 * public class FooImpl implements Foo<String, Integer> {
	 *  ..
	 * }
	 * </pre>
	 * 
	 * For '<code>FooImpl</code>' the following mappings would be added to the
	 * {@link Map}: {S=java.lang.String, T=java.lang.Integer}.
	 */
	private static void populateTypeMapFromParameterizedType(ParameterizedType type, Map<TypeVariable, Type> typeVariableMap) {
		if (type.getRawType() instanceof Class) {
			Type[] actualTypeArguments = type.getActualTypeArguments();
			TypeVariable[] typeVariables = ((Class) type.getRawType()).getTypeParameters();
			for (int i = 0; i < actualTypeArguments.length; i++) {
				Type actualTypeArgument = actualTypeArguments[i];
				TypeVariable variable = typeVariables[i];
				if (actualTypeArgument instanceof Class) {
					typeVariableMap.put(variable, actualTypeArgument);
				} else if (actualTypeArgument instanceof GenericArrayType) {
					typeVariableMap.put(variable, actualTypeArgument);
				} else if (actualTypeArgument instanceof ParameterizedType) {
					typeVariableMap.put(variable, actualTypeArgument);
				} else if (actualTypeArgument instanceof TypeVariable) {
					// We have a type that is parameterized at instantiation
					// time
					// the nearest match on the bridge method will be the
					// bounded type.
					TypeVariable typeVariableArgument = (TypeVariable) actualTypeArgument;
					Type resolvedType = typeVariableMap.get(typeVariableArgument);
					if (resolvedType == null) {
						resolvedType = extractBoundForTypeVariable(typeVariableArgument);
					}
					typeVariableMap.put(variable, resolvedType);
				}
			}
		}
	}

	/**
	 * Extracts the bound <code>Type</code> for a given {@link TypeVariable}.
	 */
	static Type extractBoundForTypeVariable(TypeVariable typeVariable) {
		Type[] bounds = typeVariable.getBounds();
		if (bounds.length == 0) {
			return Object.class;
		}
		Type bound = bounds[0];
		if (bound instanceof TypeVariable) {
			bound = extractBoundForTypeVariable((TypeVariable) bound);
		}
		return bound;
	}

	/**
	 * Returns the generic form of {@code supertype}. For example, if this is
	 * {@code ArrayList<String>}, this returns {@code Iterable<String>} given
	 * the input {@code Iterable.class}.
	 * 
	 * @param supertype
	 *            a superclass of, or interface implemented by, this.
	 */
	public static Type getSuperType(Type context, Class<?> contextRawType, Class<?> supertype) {
		Assert.isTrue(supertype.isAssignableFrom(contextRawType));
		return resolve(context, contextRawType, getGenericSupertype(context, contextRawType, supertype));
	}
	
	public static Type[] getMapKeyAndValueTypes(Type context, Class<?> contextRawType) {
		if (context == Properties.class) {
			return new Type[] { String.class, String.class };
		}
		Type mapType = getSuperType(context, contextRawType, Map.class);
		ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
		return mapParameterizedType.getActualTypeArguments();
	}
	
	public static Type newMapType(Type key,Type value) {
		return TypeUtils.parameterize(Map.class, key, value);
	}
	
	public static Type newListType(Type element) {
		return TypeUtils.parameterize(List.class, element);
	}
	
	public static Type newParameterize(Class base, Type... params) {
		return TypeUtils.parameterize(base, params);
	}
}
