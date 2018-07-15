package com.tomorrowgold.pay.unionpay.service;

import java.util.Map;

import com.tomorrowgold.pay.common.model.PayOrder;




public interface IUnionPayService {
	/**
	 * 银联支付
	 */
	String unionPay(PayOrder product);
	/**
	 * 前台回调验证
	 */
	String validate(Map<String, String> valideData, String encoding);
	/**
	 * 对账单下载
	 *
	 */
	void fileTransfer();
	
	String query(PayOrder product);
}
