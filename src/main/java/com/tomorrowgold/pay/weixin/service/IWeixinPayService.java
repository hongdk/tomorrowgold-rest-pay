package com.tomorrowgold.pay.weixin.service;

import com.tomorrowgold.pay.common.model.PayOrder;


public interface IWeixinPayService {
	
	
	/**
	 * 描述:公众号支付
	 * 说明:
     *    a.统一认证auth2后重定向道 weixinMobile\dopay  WeixinMobilePayController(JSAPI 公众号支付)
     *    b.生成带签名的
     * 参见:
     * 	  https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_4
     */
	String pubAccountPay(PayOrder product);
	
	
	/**
	 * 描述:H5支付
	 * 说明:
	 *    H5支付是指商户在微信客户端外的移动端网页展示商品或服务，用户在前述页面确认使用微信支付时，商户发起本服务呼起微信客户端进行支付。
	 *    主要用于触屏版的手机浏览器请求微信支付的场景。可以方便的从外部浏览器唤起微信支付
	 *    
	 * 申请入口：登录商户平台-->产品中心-->我的产品-->支付产品-->H5支付
	 * 参见:
	 *    https://pay.weixin.qq.com/wiki/doc/api/H5.php?chapter=15_3
	 */
	String h5Pay(PayOrder product);

	
	/**********************************************************************
	 * 描述:扫码支付(采用模式二)
	 * 说明:   
	 * 	     微信支付下单(模式二)
	 *    模式二与模式一相比，流程更为简单，不依赖设置的回调支付URL
	 *    商户后台系统先调用微信支付的统一下单接口，微信后台系统返回链接参数code_url，
	 *    商户后台系统将code_url值生成二维码图片，用户使用微信客户端扫码后发起支付
	 * 参见:
	 *    https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_5
	 ********************************************************************/
	
	String scanPay2(PayOrder product);
	
	
	/*********************************************************************
	 * 描述:微信支付下单(模式一 后续测试)
	 * 说明:
	 *    模式一开发前，商户必须在公众平台后台设置支付回调URL
	 * 参见:
	 *    https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_4    
	 ******************************************************************/
	void scanPay1(PayOrder product);
	
	
	
    /**
     * 微信支付退款
     */
	String weixinRefund(PayOrder product);
	/**
	 * 关闭订单
	 */
	String weixinCloseorder(PayOrder product);
	/**
	 * 下载微信账单
	 */
	void saveBill();
   
	
	
}
