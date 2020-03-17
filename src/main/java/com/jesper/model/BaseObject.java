package com.jesper.model;

import lombok.Data;

import java.util.Date;

@Data
public class BaseObject {

	private int start;
	private int end;
	private int pageCurrent;
	private int pageSize;
	private int pageCount;
	private String orderBy;

	private Date beginTime;

	private Date endTime;
	private String beginTimeStr;

	private String endTimeStr;
}
