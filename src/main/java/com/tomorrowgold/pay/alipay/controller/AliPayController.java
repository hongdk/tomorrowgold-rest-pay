package com.tomorrowgold.pay.alipay.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.trade.config.Configs;
import com.tomorrowgold.facade.SystradeTradeFacade;
import com.tomorrowgold.pay.alipay.service.IAliPayService;
import com.tomorrowgold.pay.common.constants.Constants;
import com.tomorrowgold.pay.common.model.PayOrder;

/*************************************
 * 支付宝支付
 ************************************/

@Api(tags ="支付宝支付")
@Controller
@RequestMapping(value = "alipay")

public class AliPayController {
	
	private static final Logger logger = LoggerFactory.getLogger(AliPayController.class);
	
	@Autowired
	private IAliPayService aliPayService;
	
	@Reference(version = com.tomorrowgold.common.constant.Constants.DUBBO_SERVICE_VERSION)
    SystradeTradeFacade systradeTradeFacade;
	
	/**
	 * 支付测试主页
	 * @return
	 */
	@ApiOperation(value="支付测试主页")
	@RequestMapping(value="index",method=RequestMethod.GET)
    public String   index() {
        return "alipay/index";
    }
	
	
	/**
	 * 描述:电脑网站支付
	 *    直接向支付宝发起【支付请求】
	 *    电脑支付直接跳转到支付宝的官方支付界面（可以扫码也可以登录支付宝）
	 * 说明:
	 *    参见
	 *    https://docs.open.alipay.com/270/105899/   
	 * @param product
	 * @param map
	 * @return
	 */
	
	@ApiOperation(value="电脑网站支付")
	@RequestMapping(value="pcPay",method=RequestMethod.POST)
    public String  pcPay(PayOrder product,ModelMap map) {
		logger.info("电脑支付");
		String form  =  aliPayService.aliPayPc(product);
		map.addAttribute("form", form);
		return "alipay/pay";
    }
	
	
	/**
	 * 描述:
	 * 	   App支付
	 *     App从服务单获取签名后的订单信息
	 * 说明:
	 * 	  https://docs.open.alipay.com/204/105297/
	 * @param product
	 * @param map
	 * @return
	 */
	@ApiOperation(value="app支付")
	@RequestMapping(value="appPay",method=RequestMethod.POST)
    public String  appPay(PayOrder product,ModelMap map) {
		logger.info("app支付服务端，订单编号:"+product.getOutTradeNo());
		String orderString  =  aliPayService.appPay(product);
		map.addAttribute("orderString", orderString);
		return "alipay/pay";
    }
	
	
	/**
	 * 描述:电脑网站查询订单
	 * 	     请求参数支持out_trade_no和trade_no
	 * 说明:
	 *    https://docs.open.alipay.com/api_1/alipay.trade.query
	 * @param product
	 * @return
	 */
	@ApiOperation(value="支付宝查询")
	@RequestMapping(value="query",method=RequestMethod.POST)
    public String  query(PayOrder product,ModelMap map) {
		logger.info("支付宝查询"+"OutTradeNo:"+product.getOutTradeNo()+";"+"TradeNo:"+product.getTradeNo());
		
		String orderString  =  aliPayService.query(product);
		map.addAttribute("orderString", orderString);
		return "alipay/pay";
    }
	
	
	/**
	 * 描述:
	 *    手机网站支付
	 *    1.生成订单信息并构造支付请求
	 *    2.向支付宝系统发送支付请求
	 * 说明:
	 *    https://docs.open.alipay.com/203/105285/
	 * @param product
	 * @param map
	 * @return
	 */
	@ApiOperation(value="手机H5支付（手机网站支付）")
	@RequestMapping(value="mobilePay",method=RequestMethod.POST)
    public String  mobilePay(PayOrder product,ModelMap map) {
		logger.info("手机H5支付（（手机网站支付））");
		String form  =  aliPayService.aliPayMobile(product);
		map.addAttribute("form", form);
		return "alipay/pay";
    }
	
	
	/**描述:
	 *    alipay.trade.precreate(统一收单线下交易预创建)
	 * 说明:
	 * 	     阿里支付预下单
	 *    如果你调用的是当面付预下单接口(alipay.trade.precreate)，调用成功后订单实际上是没有生成，
	 *    因为创建一笔订单要买家、卖家、金额三要素。
     *    预下单并没有创建订单，所以根据商户订单号操作订单，比如查询或者关闭，会报错订单不存在。
     *    当用户扫码后订单才会创建，用户扫码之前二维码有效期2小时，
     *    扫码之后有效期根据timeout_express时间指定。
	 */
	@ApiOperation(value="二维码支付")
	@RequestMapping(value="qcPay",method=RequestMethod.POST)
    public String  qcPay(PayOrder product,ModelMap map) {
		logger.info("二维码支付");
		String message  =  aliPayService.prePay(product);
		if(Constants.SUCCESS.equals(message)){
			String img= "../qrcode/"+product.getOutTradeNo()+".png";
			map.addAttribute("img", img);
		}else{
			//失败
		}
		return "alipay/qcpay";
    }
	
	
 
	/**描述:
	 *    支付宝支付回调(二维码、H5、网站)
	 * 说明:
	 * 	  
	 */
	@ApiOperation(value="支付宝支付回调(二维码、H5、网站)")
	@RequestMapping(value="notify",method=RequestMethod.POST)
	public void alipay_notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String  message = "success";
		Map<String, String> params = new HashMap<String, String>();
		// 取出所有参数是为了验证签名
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			params.put(parameterName, request.getParameter(parameterName));
		}
		//验证签名 校验签名
		boolean signVerified = false;
		try {
			//signVerified = AlipaySignature.rsaCheckV1(params, Configs.getAlipayPublicKey(), "UTF-8");
			//各位同学这里可能需要注意一下,2018/01/26 以后新建应用只支持RSA2签名方式，目前已使用RSA签名方式的应用仍然可以正常调用接口，注意下自己生成密钥的签名算法
			signVerified = AlipaySignature.rsaCheckV1(params, Configs.getAlipayPublicKey(), "UTF-8","RSA2");
		} catch (AlipayApiException e) {
			e.printStackTrace();
			message =  "failed";
		}
		if (signVerified) {
			logger.info("支付宝验证签名成功！");
			// 若参数中的appid和填入的appid不相同，则为异常通知
			if (!Configs.getAppid().equals(params.get("app_id"))) {
				logger.info("与付款时的appid不同，此为异常通知，应忽略！");
				message =  "failed";
			}else{
				String outtradeno = params.get("out_trade_no");
				//在数据库中查找订单号对应的订单，并将其金额与数据库中的金额对比，若对不上，也为异常通知
				String status = params.get("trade_status");
				if (status.equals("WAIT_BUYER_PAY")) { // 如果状态是正在等待用户付款
					logger.info(outtradeno + "订单的状态正在等待用户付款");
				} else if (status.equals("TRADE_CLOSED")) { // 如果状态是未付款交易超时关闭，或支付完成后全额退款
					logger.info(outtradeno + "订单的状态已经关闭");
				} else if (status.equals("TRADE_SUCCESS") || status.equals("TRADE_FINISHED")) { // 如果状态是已经支付成功
					logger.info("(支付宝订单号:"+outtradeno+"付款成功)");
					//这里 根据实际业务场景 做相应的操作
					Long[] ids = new Long[1];
					Long lgOuttradeno = Long.parseLong(outtradeno);
					ids[0] = lgOuttradeno;
					systradeTradeFacade.updatepaySuccess(ids);
				} else {
					
				}
			}
		} else { // 如果验证签名没有通过
			message =  "failed";
			logger.info("验证签名失败！");
		}
		BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(message.getBytes());
		out.flush();
		out.close();
	}
	
	

}
