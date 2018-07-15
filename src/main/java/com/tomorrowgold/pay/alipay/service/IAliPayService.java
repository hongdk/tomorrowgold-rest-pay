package com.tomorrowgold.pay.alipay.service;

import com.tomorrowgold.pay.common.model.PayOrder;

/**
 * 扫码支付以及手机H5支付
 */
public interface IAliPayService {
	
	
	/**
	 * 描述:电脑网站支付
	 *    直接向支付宝发起【支付请求】
	 *    电脑支付直接跳转到支付宝的官方支付界面（可以扫码也可以登录支付宝）
	 * 说明:
	 *    参见
	 *    https://docs.open.alipay.com/270/105899/   
	 **/    
	String aliPayPc(PayOrder product);
	
	/**
	 * 描述:App支付
	 *     App从服务单获取签名后的订单信息
	 * 说明:
	 * 	  https://docs.open.alipay.com/204/105297/
	 **/ 
	String appPay(PayOrder product);

	 
	/**描述:统一收单线下交易预创建
	 *    alipay.trade.precreate(统一收单线下交易预创建)
	 * 说明:
	 * 	     阿里支付预下单
	 *    如果你调用的是当面付预下单接口(alipay.trade.precreate)，调用成功后订单实际上是没有生成，
	 *    因为创建一笔订单要买家、卖家、金额三要素。
     *    预下单并没有创建订单，所以根据商户订单号操作订单，比如查询或者关闭，会报错订单不存在。
     *    当用户扫码后订单才会创建，用户扫码之前二维码有效期2小时，
     *    扫码之后有效期根据timeout_express时间指定。
	 */
	String prePay(PayOrder product);
    
	
	/**
	 * 描述:电脑网站查询订单
	 * 	     请求参数支持out_trade_no和trade_no
	 * 说明:
	 *    https://docs.open.alipay.com/api_1/alipay.trade.query
	 * @param product
	 * @return
	 */
	String query(PayOrder product);
	
	
	/**
	 * 描述:统一收单交易退款接口
	 * 	     请求参数支持out_trade_no和trade_no不能同是为空,refund_amount退款金额
	 * 说明:
	 *    https://docs.open.alipay.com/api_1/alipay.trade.refund
	 * @param product
	 * @return
	 */
	String aliRefund(PayOrder product);
	
	
	/**
	 * 关闭订单
	 */
	String aliCloseorder(PayOrder product);
	
	
	/**
     * 下载对账单 
	 */
	String downloadBillUrl(String billDate,String billType);

	
	/**
	 * 手机H5支付、腾讯相关软件下不支持、使用UC等浏览器打开
	 * 方法一：
	 * 对于页面跳转类API，SDK不会也无法像系统调用类API一样自动请求支付宝并获得结果，而是在接受request请求对象后，
	 * 为开发者生成前台页面请求需要的完整form表单的html（包含自动提交脚本），商户直接将这个表单的String输出到http response中即可。
	 * 方法二：
	 * 如果是远程调用返回消费放一个form表单 然后调用方刷新到页面自动提交即可
	 * 备注：人民币单位为分
	 * attach 附件参数 使用json格式传递 用于回调区分
	 */
	String aliPayMobile(PayOrder product);
	
	
	
}
