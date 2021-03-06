package com.jd.httpservice.converters;

import com.jd.httpservice.StringConverter;

import utils.serialize.json.JSONSerializeUtils;

/**
 * JSON 格式的参数转换器；
 * 
 * @author haiq
 *
 */
public class JsonConverter implements StringConverter {

	private Class<?> dataType;

	public JsonConverter(Class<?> dataType) {
		this.dataType = dataType;
	}

	@Override
	public String toString(Object obj) {
		// TODO:未定义“日期时间”的输出格式 ；
		return JSONSerializeUtils.serializeToJSON(obj, dataType);
		// return JSON.toJSONString(obj);
	}

}
