/**
 * Copyright: Copyright 2016-2020 JD.COM All Right Reserved
 * FileName: com.jd.blockchain.sdk.converters.BinarySerializeResponseConverter
 * Author: shaozhuguang
 * Department: 区块链研发部
 * Date: 2018/9/5 下午5:22
 * Description:
 */
package com.jd.httpservice.converters;

import java.io.InputStream;

import com.jd.httpservice.HttpServiceContext;
import com.jd.httpservice.ResponseConverter;
import com.jd.httpservice.agent.ServiceRequest;

import utils.serialize.binary.BinarySerializeUtils;

public class BinarySerializeResponseConverter implements ResponseConverter {

    @Override
    public Object getResponse(ServiceRequest request, InputStream responseStream, HttpServiceContext serviceContext)
            throws Exception {
    	return BinarySerializeUtils.deserialize(responseStream);
    }

}