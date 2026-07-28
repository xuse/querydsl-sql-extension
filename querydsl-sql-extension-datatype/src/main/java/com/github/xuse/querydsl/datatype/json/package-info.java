/**
 * 基于 Jackson 实现的Fastjson API 兼容层。
 * <p>
 * 迁移步骤：
 * <pre>
 * // Before
 * import com.alibaba.fastjson.JSON;
 * import com.alibaba.fastjson.JSONObject;
 * import com.alibaba.fastjson.JSONArray;
 *
 * // After
 * import com.github.xuse.querydsl.datatype.json.JSON;
 * import com.github.xuse.querydsl.datatype.json.JSONObject;
 * import com.github.xuse.querydsl.datatype.json.JSONArray;
 * </pre>
 * <p>
 * 业务逻辑代码无需修改，仅替换 import 即可完成迁移。
 *
 * @see JSON
 * @see JSONObject
 * @see JSONArray
 */
package com.github.xuse.querydsl.datatype.json;
