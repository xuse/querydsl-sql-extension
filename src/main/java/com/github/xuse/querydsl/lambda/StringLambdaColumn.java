package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;

public interface StringLambdaColumn<B> extends LambdaColumn<B, String>{
    default StringExpression mixin(){
    	return (StringExpression) PathCache.getPath(this);
    }
    
	   /**
     * Create a {@code concat(this, str)} expression
     *
     * <p>Get the concatenation of this and str</p>
     *
     * @param str string to append
     * @return this + str
     */
    default StringExpression append(Expression<String> str) {
        return mixin().append(str);
    }

    /**
     * Create a {@code concat(this, str)} expression
     *
     * <p>Get the concatenation of this and str</p>
     *
     * @param str string to append
     * @return this + str
     */
    default StringExpression append(String str) {
    	return mixin().append(str);
    }

    /**
     * Create a {@code this.charAt(i)} expression
     *
     * <p>Get the character at the given index</p>
     *
     * @param i zero based index
     * @return this.charAt(i)
     * @see java.lang.String#charAt(int)
     */
    default SimpleExpression<Character> charAt(Expression<Integer> i) {
    	return mixin().charAt(i);
    }

    /**
     * Create a {@code this.charAt(i)} expression
     *
     * <p>Get the character at the given index</p>
     *
     * @param i zero based index
     * @return this.charAt(i)
     * @see java.lang.String#charAt(int)
     */
    default SimpleExpression<Character> charAt(int i) {
    	return mixin().charAt(i);
    }

    /**
     * Create a {@code concat(this, str)} expression
     *
     * <p>Get the concatenation of this and str</p>
     *
     * @param str string to append
     * @return this + str
     */
    default StringExpression concat(Expression<String> str) {
    	return mixin().concat(str);
    }

    /**
     * Create a {@code concat(this, str)} expression
     *
     * <p>Get the concatenation of this and str</p>
     *
     * @param str string to append
     * @return this + str
     */
    default StringExpression concat(String str) {
    	return mixin().concat(str);
    }

    /**
     * Create a {@code this.contains(str)} expression
     *
     * <p>Returns true if the given String is contained</p>
     *
     * @param str string
     * @return this.contains(str)
     * @see java.lang.String#contains(CharSequence)
     */
    default BooleanExpression contains(Expression<String> str) {
        return mixin().contains(str);
    }

    /**
     * Create a {@code this.contains(str)} expression
     *
     * <p>Returns true if the given String is contained</p>
     *
     * @param str string
     * @return this.contains(str)
     * @see java.lang.String#contains(CharSequence)
     */
    default BooleanExpression contains(String str) {
    	return mixin().contains(str);
    }

    /**
     * Create a {@code this.containsIgnoreCase(str)} expression
     *
     * <p>Returns true if the given String is contained, compare case insensitively</p>
     *
     * @param str string
     * @return this.containsIgnoreCase(str) expression
     */
    default BooleanExpression containsIgnoreCase(Expression<String> str) {
    	return mixin().containsIgnoreCase(str);
    }

    /**
     * Create a {@code this.containsIgnoreCase(str)} expression
     *
     * <p>Returns true if the given String is contained, compare case insensitively</p>
     *
     * @param str string
     * @return this.containsIgnoreCase(str)
     */
    default BooleanExpression containsIgnoreCase(String str) {
    	return mixin().containsIgnoreCase(str);
    }

    /**
     * Create a {@code this.endsWith(str)} expression
     *
     * <p>Returns true if this ends with str</p>
     *
     * @param str string
     * @return this.endsWith(str)
     * @see java.lang.String#endsWith(String)
     */
    default BooleanExpression endsWith(Expression<String> str) {
    	return mixin().endsWith(str);
    }

    /**
     * Create a {@code this.endsWithIgnoreCase(str)} expression
     *
     * <p>Returns true if this ends with str, compares case insensitively</p>
     *
     * @param str string
     * @return this.endsWithIgnoreCase(str)
     */
    default BooleanExpression endsWithIgnoreCase(Expression<String> str) {
    	return mixin().endsWithIgnoreCase(str);
    }

    /**
     * Create a {@code this.endsWith(str)} expression
     *
     * <p>Returns true if this ends with str</p>
     *
     * @param str string
     * @return this.endsWith(str)
     * @see java.lang.String#endsWith(String)
     */
    default BooleanExpression endsWith(String str) {
    	return mixin().endsWith(str);
    }

    /**
     * Create a {@code this.endsWithIgnoreCase(str)} expression
     *
     * <p>Returns true if this ends with str, compares case insensitively</p>
     *
     * @param str string
     * @return this.endsWithIgnoreCase(str)
     */
    default BooleanExpression endsWithIgnoreCase(String str) {
    	return mixin().endsWithIgnoreCase(str);
    }

    /**
     * Create a {@code this.equalsIgnoreCase(str)} expression
     *
     * <p>Compares this {@code StringExpression} to another {@code StringExpression}, ignoring case
     * considerations.</p>
     *
     * @param str string
     * @return this.equalsIgnoreCase(str)
     * @see java.lang.String#equalsIgnoreCase(String)
     */
    default BooleanExpression equalsIgnoreCase(Expression<String> str) {
    	return mixin().equalsIgnoreCase(str);
    }

    /**
     * Create a {@code this.equalsIgnoreCase(str)} expression
     *
     * <p>Compares this {@code StringExpression} to another {@code StringExpression}, ignoring case
     * considerations.</p>
     *
     * @param str string
     * @return this.equalsIgnoreCase(str)
     * @see java.lang.String#equalsIgnoreCase(String)
     */
    default BooleanExpression equalsIgnoreCase(String str) {
    	return mixin().equalsIgnoreCase(str);
    }

    /**
     * Create a {@code this.indexOf(str)} expression
     *
     * <p>Get the index of the given substring in this String</p>
     *
     * @param str string
     * @return this.indexOf(str)
     * @see java.lang.String#indexOf(String)
     */
    default NumberExpression<Integer> indexOf(Expression<String> str) {
    	return mixin().indexOf(str);
    }

    /**
     * Create a {@code this.indexOf(str)} expression
     *
     * <p>Get the index of the given substring in this String</p>
     *
     * @param str string
     * @return this.indexOf(str)
     * @see java.lang.String#indexOf(String)
     */
    default NumberExpression<Integer> indexOf(String str) {
    	return mixin().indexOf(str);
    }

    /**
     * Create a {@code this.indexOf(str, i)} expression
     *
     * <p>Get the index of the given substring in this String, starting from the given index</p>
     *
     * @param str string
     * @param i zero based index
     * @return this.indexOf(str, i)
     * @see java.lang.String#indexOf(String, int)
     */
    default NumberExpression<Integer> indexOf(String str, int i) {
    	return mixin().indexOf(str,i);
    }

    /**
     * Create a {@code this.indexOf(str)} expression
     *
     * <p>Get the index of the given substring in this String, starting from the given index</p>
     *
     * @param str string
     * @param i zero based index
     * @return this.indexOf(str)
     */
    default NumberExpression<Integer> indexOf(Expression<String> str, int i) {
    	return mixin().indexOf(str,i);
    }

    /**
     * Create a {@code this.isEmpty()} expression
     *
     * <p>Return true if this String is empty</p>
     *
     * @return this.isEmpty()
     * @see java.lang.String#isEmpty()
     */
    default BooleanExpression isEmpty() {
    	return mixin().isEmpty();
    }

    /**
     * Create a {@code !this.isEmpty()} expression
     *
     * <p>Return true if this String is not empty</p>
     *
     * @return !this.isEmpty()
     * @see java.lang.String#isEmpty()
     */
    default BooleanExpression isNotEmpty() {
    	return mixin().isNotEmpty();
    }

    /**
     * Create a {@code this.length()} expression
     *
     * <p>Return the length of this String</p>
     *
     * @return this.length()
     * @see java.lang.String#length()
     */
    default NumberExpression<Integer> length() {
    	return mixin().length();
    }

    /**
     * Create a {@code this like str} expression
     *
     * @param str string
     * @return this like str
     */
    default BooleanExpression like(String str) {
    	return mixin().like(str);
    }

    /**
     * Create a {@code this like str} expression
     *
     * @param str string
     * @return this like str
     */
    default BooleanExpression like(Expression<String> str) {
    	return mixin().like(str);
    }

    /**
     * Create a {@code this like str} expression ignoring case
     *
     * @param str string
     * @return this like str
     */
    default BooleanExpression likeIgnoreCase(String str) {
    	return mixin().likeIgnoreCase(str);
    }

    /**
     * Create a {@code this like str} expression ignoring case
     *
     * @param str string
     * @return this like str
     */
    default BooleanExpression likeIgnoreCase(Expression<String> str) {
    	return mixin().likeIgnoreCase(str);
    }

    /**
     * Create a {@code this like str} expression
     *
     * @param str string
     * @param escape escape character
     * @return this like str
     */
    default BooleanExpression like(String str, char escape) {
    	return mixin().like(str,escape);
    }

    /**
     * Create a {@code this like str} expression
     *
     * @param str string
     * @param escape escape character
     * @return this like str
     */
    default BooleanExpression like(Expression<String> str, char escape) {
    	return mixin().like(str,escape);
    }

    /**
     * Create a {@code this like str} expression ignoring case
     *
     * @param str string
     * @param escape escape character
     * @return this like string
     */
    default BooleanExpression likeIgnoreCase(String str, char escape) {
    	return mixin().likeIgnoreCase(str,escape);
    }

    /**
     * Create a {@code this like str} expression ignoring case
     *
     * @param str string
     * @param escape escape character
     * @return this like string
     */
    default BooleanExpression likeIgnoreCase(Expression<String> str, char escape) {
    	return mixin().likeIgnoreCase(str,escape);
    }

    /**
     * Create a {@code locate(str, this)} expression
     *
     * <p>Get the position of the given String in this String, the first position is 1</p>
     *
     * @param str string
     * @return locate(str, this)
     */
    default NumberExpression<Integer> locate(Expression<String> str) {
    	return mixin().locate(str);
    }

    /**
     * Create a {@code locate(str, this)} expression
     *
     * <p>Get the position of the given String in this String, the first position is 1</p>
     *
     * @param str string
     * @return locate(str, this)
     */
    default NumberExpression<Integer> locate(String str) {
    	return mixin().locate(str);
    }

    /**
     * Create a {@code locate(str, this, start)} expression
     *
     * <p>Get the position of the given String in this String, the first position is 1</p>
     *
     * @param str string
     * @param start start
     * @return locate(str, this, start)
     */
    default NumberExpression<Integer> locate(Expression<String> str, NumberExpression<Integer> start) {
    	return mixin().locate(str,start);
    }

    /**
     * Create a {@code locate(str, this, start)} expression
     *
     * <p>Get the position of the given String in this String, the first position is 1</p>
     *
     * @param str string
     * @param start start
     * @return locate(str, this, start)
     */
    default NumberExpression<Integer> locate(String str, int start) {
    	return mixin().locate(str,start);
    }

    /**
     * Create a {@code locate(str, this, start)} expression
     *
     * <p>Get the position of the given String in this String, the first position is 1</p>
     *
     * @param str string
     * @param start start
     * @return locate(str, this, start)
     */
    default NumberExpression<Integer> locate(String str, Expression<Integer> start) {
    	return mixin().locate(str,start);
    }

    /**
     * Create a {@code this.toLowerCase()} expression
     *
     * <p>Get the lower case form</p>
     *
     * @return this.toLowerCase()
     * @see java.lang.String#toLowerCase()
     */
    default StringExpression lower() {
    	return mixin().lower();
    }

    /**
     * Create a {@code this.matches(regex)} expression
     *
     * <p>Return true if this String matches the given regular expression</p>
     *
     * <p>Some implementations such as Querydsl JPA will try to convert a regex expression into like
     * form and will throw an Exception when this fails</p>
     *
     * @param regex regular expression
     * @return this.matches(right)
     * @see java.lang.String#matches(String)
     */
    default BooleanExpression matches(Expression<String> regex) {
    	return mixin().matches(regex);
    }

    /**
     * Create a {@code this.matches(regex)} expression
     *
     * <p>Return true if this String matches the given regular expression</p>
     *
     * <p>Some implementations such as Querydsl JPA will try to convert a regex expression into like
     * form and will throw an Exception when this fails</p>
     *
     * @param regex regular expression
     * @return this.matches(regex)
     * @see java.lang.String#matches(String)
     */
    default BooleanExpression matches(String regex) {
    	return mixin().matches(regex);
    }

    /**
     * Create a {@code max(this)} expression
     *
     * <p>Get the maximum value of this expression (aggregation)</p>
     *
     * @return max(this)
     */
    default StringExpression max() {
    	return mixin().max();
    }

    /**
     * Create a {@code min(this)} expression
     *
     * <p>Get the minimum value of this expression (aggregation)</p>
     *
     * @return min(this)
     */
    @Override
    default StringExpression min() {
    	return mixin().min();
    }

    /**
     * Create a {@code !this.equalsIgnoreCase(str)} expression
     *
     * <p>Compares this {@code StringExpression} to another {@code StringExpression}, ignoring case
     * considerations.</p>
     *
     * @param str string
     * @return !this.equalsIgnoreCase(str)
     * @see java.lang.String#equalsIgnoreCase(String)
     */
    default BooleanExpression notEqualsIgnoreCase(Expression<String> str) {
    	return mixin().notEqualsIgnoreCase(str);
    }


    /**
     * Create a {@code !this.equalsIgnoreCase(str)} expression
     *
     * <p>Compares this {@code StringExpression} to another {@code StringExpression}, ignoring case
     * considerations.</p>
     *
     * @param str string
     * @return !this.equalsIgnoreCase(str)
     * @see java.lang.String#equalsIgnoreCase(String)
     */
    default BooleanExpression notEqualsIgnoreCase(String str) {
    	return mixin().notEqualsIgnoreCase(str);
    }

    /**
     * Create a {@code this not like str} expression
     *
     * @param str string
     * @return expression of this not like str
     */
    default BooleanExpression notLike(String str) {
    	return mixin().notLike(str);
    }

    /**
     * Create a {@code this not like str} expression
     *
     * @param str string
     * @return expression of this not like str
     */
    default BooleanExpression notLike(Expression<String> str) {
    	return mixin().notLike(str);
    }

    /**
     * Create a {@code this not like str} expression
     *
     * @param str string
     * @param escape escape char.
     * @return expression of this not like str
     */
    default BooleanExpression notLike(String str, char escape) {
    	return mixin().notLike(str,escape);
    }

    /**
     * Create a {@code this not like str} expression
     *
     * @param str string
     * @param escape escape char.
     * @return expression of this not like str
     */
    default BooleanExpression notLike(Expression<String> str, char escape) {
    	return mixin().notLike(str,escape);
    }

    /**
     * Create a {@code concat(str, this)} expression
     *
     * <p>Prepend the given String and return the result</p>
     *
     * @param str string
     * @return str + this
     */
    default StringExpression prepend(Expression<String> str) {
    	return mixin().prepend(str);
    }

    /**
     * Create a {@code concat(str, this)} expression
     *
     * <p>Prepend the given String and return the result</p>
     *
     * @param str string
     * @return str + this
     */
    default StringExpression prepend(String str) {
    	return mixin().prepend(str);
    }

    /**
     * Create a {@code this.startsWith(str)} expression
     *
     * <p>Return true if this starts with str</p>
     *
     * @param str string
     * @return this.startsWith(str)
     * @see java.lang.String#startsWith(String)
     */
    default BooleanExpression startsWith(Expression<String> str) {
    	return mixin().startsWith(str);
    }

    /**
     * Create a {@code this.startsWithIgnoreCase(str)} expression
     *
     * @param str string
     * @return this.startsWithIgnoreCase(str)
     */
    default BooleanExpression startsWithIgnoreCase(Expression<String> str) {
    	return mixin().startsWithIgnoreCase(str);
    }

    /**
     * Create a {@code this.startsWith(str)} expression
     *
     * <p>Return true if this starts with str</p>
     *
     * @param str string
     * @return this.startsWith(str)
     * @see java.lang.String#startsWith(String)
     */
    default BooleanExpression startsWith(String str) {
    	return mixin().startsWith(str);
    }

    /**
     * Create a {@code this.startsWithIgnoreCase(str)} expression
     *
     * @param str string
     * @return this.startsWithIgnoreCase(str)
     */
    default BooleanExpression startsWithIgnoreCase(String str) {
    	return mixin().startsWithIgnoreCase(str);
    }

    default StringExpression stringValue() {
        return mixin();
    }

    /**
     * Create a {@code this.substring(beginIndex)} expression
     *
     * @param beginIndex inclusive start index
     * @return this.substring(beginIndex)
     * @see java.lang.String#substring(int)
     */
    default StringExpression substring(int beginIndex) {
        return mixin().substring(beginIndex);
    }

    /**
     * Create a {@code this.substring(beginIndex, endIndex)} expression
     *
     * @param beginIndex inclusive start index
     * @param endIndex exclusive end index
     * @return this.substring(beginIndex, endIndex)
     * @see java.lang.String#substring(int, int)
     */
    default StringExpression substring(int beginIndex, int endIndex) {
    	return mixin().substring(beginIndex,endIndex);
    }

    /**
     * Create a {@code this.substring(beginIndex, endIndex)} expression
     *
     * @param beginIndex inclusive start index
     * @param endIndex exclusive end index
     * @return this.substring(beginIndex, endIndex)
     * @see java.lang.String#substring(int, int)
     */
    default StringExpression substring(Expression<Integer> beginIndex, int endIndex) {
    	return mixin().substring(beginIndex,endIndex);
    }

    /**
     * Create a {@code this.substring(beginIndex, endIndex)} expression
     *
     * @param beginIndex inclusive start index
     * @param endIndex exclusive end index
     * @return this.substring(beginIndex, endIndex)
     * @see java.lang.String#substring(int, int)
     */
    default StringExpression substring(int beginIndex, Expression<Integer> endIndex) {
    	return mixin().substring(beginIndex,endIndex);
    }

    /**
     * Create a {@code this.substring(beginIndex)} expression
     *
     * @param beginIndex inclusive start index
     * @return this.substring(beginIndex)
     * @see java.lang.String#substring(int)
     */
    default StringExpression substring(Expression<Integer> beginIndex) {
    	return mixin().substring(beginIndex);
    }

    /**
     * Create a {@code this.substring(beginIndex, endIndex)} expression
     *
     * @param beginIndex inclusive start index
     * @param endIndex exclusive end index
     * @return this.substring(beginIndex, endIndex)
     * @see java.lang.String#substring(int, int)
     */
    default StringExpression substring(Expression<Integer> beginIndex, Expression<Integer> endIndex) {
    	return mixin().substring(beginIndex,endIndex);
    }

    /**
     * Create a {@code this.toLowerCase()} expression
     *
     * <p>Get the lower case form</p>
     *
     * @return this.toLowerCase()
     * @see java.lang.String#toLowerCase()
     */
    default StringExpression toLowerCase() {
    	return mixin().toLowerCase();
    }

    /**
     * Create a {@code this.toUpperCase()} expression
     *
     * <p>Get the upper case form</p>
     *
     * @return this.toUpperCase()
     * @see java.lang.String#toUpperCase()
     */
    default StringExpression toUpperCase() {
    	return mixin().toUpperCase();
    }

    /**
     * Create a {@code this.trim()} expression
     *
     * <p>Create a copy of the string, with leading and trailing whitespace
     * omitted.</p>
     *
     * @return this.trim()
     * @see java.lang.String#trim()
     */
    default StringExpression trim() {
        return mixin().trim();
    }

    /**
     * Create a {@code this.toUpperCase()} expression
     *
     * <p>Get the upper case form</p>
     *
     * @return this.toUpperCase()
     * @see java.lang.String#toUpperCase()
     */
    default StringExpression upper() {
    	return mixin().upper();
    }


    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other other expression
     * @return nullif(this, other)
     */
    @Override
    default StringExpression nullif(Expression<String> other) {
    	return mixin().nullif(other);
    }

    /**
     * Create a {@code nullif(this, other)} expression
     *
     * @param other other expression
     * @return nullif(this, other)
     */
    @Override
    default StringExpression nullif(String other) {
    	return mixin().nullif(other);
    }

    /**
     * Create a {@code coalesce(this, expr)} expression
     *
     * @param expr additional argument
     * @return coalesce
     */
    @Override
    default StringExpression coalesce(Expression<String> expr) {
    	return mixin().coalesce(expr);
    }

    /**
     * Create a {@code coalesce(this, exprs...)} expression
     *
     * @param exprs additional arguments
     * @return coalesce
     */
    @Override
    default StringExpression coalesce(Expression<?>... exprs) {
    	return mixin().coalesce(exprs);
    }

    /**
     * Create a {@code coalesce(this, arg)} expression
     *
     * @param arg additional argument
     * @return coalesce
     */
    @Override
    default StringExpression coalesce(String arg) {
    	return mixin().coalesce(arg);
    }

    /**
     * Create a {@code coalesce(this, args...)} expression
     *
     * @param args additional arguments
     * @return coalesce
     */
    @Override
    default StringExpression coalesce(String... args) {
    	return mixin().coalesce(args);
    }
}
