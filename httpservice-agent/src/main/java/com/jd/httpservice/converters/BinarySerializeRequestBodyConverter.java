/**
 * Copyright: Copyright 2016-2020 JD.COM All Right Reserved
 * FileName: com.jd.blockchain.sdk.converters.BinarySerializeRequestBodyConverter
 * Author: shaozhuguang
 * Department: 区块链研发部
 * Date: 2018/9/5 下午5:09
 * Description: 序列化请求体
 */
package com.jd.httpservice.converters;

import java.io.IOException;
import java.io.OutputStream;

import com.jd.httpservice.RequestBodyConverter;

import utils.serialize.binary.BinarySerializeUtils;

public class BinarySerializeRequestBodyConverter implements RequestBodyConverter {

    @Override
    public void write(Object param, OutputStream out) throws IOException {
    	BinarySerializeUtils.serialize(param, out);
    }
}