package com.jd.httpservice.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jd.httpservice.RequestBodyConverter;

import utils.io.BytesUtils;

public class InputStreamBodyConverter implements RequestBodyConverter{

	@Override
	public void write(Object param, OutputStream out) throws IOException{
		BytesUtils.copy((InputStream)param, out);
	}

}
