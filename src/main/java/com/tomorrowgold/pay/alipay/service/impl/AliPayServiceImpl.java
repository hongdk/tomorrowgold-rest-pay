package com.tomorrowgold.pay.alipay.service.impl;

import java.io.File;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.trade.config.Configs;
import com.alipay.trade.model.ExtendParams;
import com.alipay.trade.model.builder.AlipayTradePrecreateContentBuilder;
import com.alipay.trade.model.builder.AlipayTradeQueryCententBuilder;
import com.alipay.trade.model.builder.AlipayTradeRefundContentBuilder;
import com.alipay.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.trade.model.result.AlipayF2FQueryResult;
import com.alipay.trade.model.result.AlipayF2FRefundResult;
import com.alipay.trade.utils.ZxingUtils;
import com.tomorrowgold.pay.alipay.service.IAliPayService;
import com.tomorrowgold.pay.alipay.util.AliPayConfig;
import com.tomorrowgold.pay.common.constants.Constants;
import com.tomorrowgold.pay.common.model.PayOrder;
import com.tomorrowgold.pay.common.util.CommonUtil;


@Service
public class AliPayServiceImpl implements IAliPayService {
	private static final Logger logger = LoggerFactory.getLogger(AliPayServiceImpl.class);
	
	@Value("${alipay.notify.url}")
	private String notify_url;
	@Value("${alipay.return.url}")
	private String return_url;
	
	
	 public String query(PayOrder product){

	        AlipayF2FQueryResult result = AliPayConfig.getAlipayTradeService().queryTradeResult(product.getOutTradeNo());
	        switch (result.getTradeStatus()) {
	            case SUCCESS:
	                logger.info("查询返回该订单支付成功: )");

	                AlipayTradeQueryResponse resp = result.getResponse();
	                logger.info(resp.getTradeStatus());
//	                logger.info(resp.getFundBillList());
	                break;

	            case FAILED:
	                logger.error("查询返回该订单支付失败!!!");
	                break;

	            case UNKNOWN:
	                logger.error("系统异常，订单支付状态未知!!!");
	                break;

	            default:
	                logger.error("不支持的交易状态，交易返回异常!!!");
	                break;
	        }
	        return result.getResponse().getBody();
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
	 
	@Override
	public String prePay(PayOrder product) {
		logger.info("订单号：{}生成支付宝支付码",product.getOutTradeNo());
		String  message = Constants.SUCCESS;
		//二维码存放路径
		System.out.println(Constants.QRCODE_PATH);
		String imgPath= Constants.QRCODE_PATH+Constants.SF_FILE_SEPARATOR+product.getOutTradeNo()+".png";
		String outTradeNo = product.getOutTradeNo();
		String subject = product.getSubject();
		String totalAmount =  CommonUtil.divide(product.getTotalAmount(), "100").toString();
		// 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
		String sellerId = "";
		// (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
		String storeId = "test_store_id";
		// 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
		ExtendParams extendParams = new ExtendParams();
		extendParams.setSysServiceProviderId("2088100200300400500");
		// 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
		String body = product.getBody();
		// 支付超时，定义为120分钟
		String timeoutExpress = "120m";
		// 创建扫码支付请求builder，设置请求参数
		AlipayTradePrecreateContentBuilder builder = new AlipayTradePrecreateContentBuilder()
		.setSubject(subject)
		.setTotalAmount(totalAmount)
		.setOutTradeNo(outTradeNo)
		.setSellerId(sellerId)
		.setBody(body)//128长度 --附加信息
		.setStoreId(storeId)
		.setExtendParams(extendParams)
		//.setTimeoutExpress(timeoutExpress)
		.setTimeExpress(timeoutExpress)
        .setNotifyUrl(notify_url);
		//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
		
		AlipayF2FPrecreateResult result = AliPayConfig.getAlipayTradeService().tradePrecreate(builder);
		switch (result.getTradeStatus()) {
		case SUCCESS:
			logger.info("支付宝预下单成功: )");
			
			AlipayTradePrecreateResponse response = result.getResponse();
			dumpResponse(response);
			ZxingUtils.getQRCodeImge(response.getQrCode(), 256, imgPath);
			break;
			
		case FAILED:
			logger.info("支付宝预下单失败!!!");
			message = Constants.FAIL;
			break;
			
		case UNKNOWN:
			logger.info("系统异常，预下单状态未知!!!");
			message = Constants.FAIL;
			break;
			
		default:
			logger.info("不支持的交易状态，交易返回异常!!!");
			message = Constants.FAIL;
			break;
		}
		return message;
	}
	
	
	@Override
	public String aliRefund(PayOrder product) {
		logger.info("订单号："+product.getOutTradeNo()+"支付宝退款");
		String  message = Constants.SUCCESS;
		 // (必填) 外部订单号，需要退款交易的商户外部订单号
        String outTradeNo = product.getOutTradeNo();
        // (必填) 退款金额，该金额必须小于等于订单的支付金额，单位为元
        String refundAmount = CommonUtil.divide(product.getTotalAmount(), "100").toString();

        // (必填) 退款原因，可以说明用户退款原因，方便为商家后台提供统计
        String refundReason = "正常退款，用户买多了";

        // (必填) 商户门店编号，退款情况下可以为商家后台提供退款权限判定和统计等作用，详询支付宝技术支持
        String storeId = "test_store_id";

        // 创建退款请求builder，设置请求参数
        AlipayTradeRefundContentBuilder builder = new AlipayTradeRefundContentBuilder()
                .setOutTradeNo(outTradeNo)
                .setRefundAmount(refundAmount)
                .setRefundReason(refundReason)
                //.setOutRequestNo(outRequestNo)
                .setStoreId(storeId);

        AlipayF2FRefundResult result = AliPayConfig.getAlipayTradeService().tradeRefund(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
            	logger.info("支付宝退款成功: )");
                break;

            case FAILED:
            	logger.info("支付宝退款失败!!!");
            	message = Constants.FAIL;
                break;

            case UNKNOWN:
            	logger.info("系统异常，订单退款状态未知!!!");
            	message = Constants.FAIL;
                break;

            default:
            	logger.info("不支持的交易状态，交易返回异常!!!");
            	message = Constants.FAIL;
                break;
        }
		return message;
	}
	
	/**
     * 退款查询
     * @param orderNo 商户订单号
     * @param refundOrderNo 请求退款接口时，传入的退款请求号，如果在退款请求时未传入，则该值为创建交易时的外部订单号
     * @return
     * @throws AlipayApiException
     */
   
    public String refundQuery(String orderNo, String refundOrderNo) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest alipayRequest = new AlipayTradeFastpayRefundQueryRequest();

        AlipayTradeFastpayRefundQueryModel model=new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(orderNo);
        model.setOutRequestNo(refundOrderNo);
        alipayRequest.setBizModel(model);

        AlipayTradeFastpayRefundQueryResponse alipayResponse = AliPayConfig.getAlipayClient().execute(alipayRequest);
        System.out.println(alipayResponse.getBody());

        return alipayResponse.getBody();
    }
    
    /**
     * 如果你调用的是当面付预下单接口(alipay.trade.precreate)，调用成功后订单实际上是没有生成，因为创建一笔订单要买家、卖家、金额三要素。
     * 预下单并没有创建订单，所以根据商户订单号操作订单，比如查询或者关闭，会报错订单不存在。
     * 当用户扫码后订单才会创建，用户扫码之前二维码有效期2小时，扫码之后有效期根据timeout_express时间指定。
     * =====只有支付成功后 调用此订单才可以=====
     */
	@Override
	public String aliCloseorder(PayOrder product) {
		logger.info("订单号："+product.getOutTradeNo()+"支付宝关闭订单");
		String  message = Constants.SUCCESS;
		try {
			String imgPath= Constants.QRCODE_PATH+Constants.SF_FILE_SEPARATOR+"alipay_"+product.getOutTradeNo()+".png";
			File file = new File(imgPath);
            if(file.exists()){
            	AlipayClient alipayClient = AliPayConfig.getAlipayClient();
            	AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            	request.setBizContent("{" +
            			"    \"out_trade_no\":\""+product.getOutTradeNo()+"\"" +
            			"  }");
            	AlipayTradeCloseResponse response = alipayClient.execute(request);
            	if(response.isSuccess()){//扫码未支付的情况
            		logger.info("订单号："+product.getOutTradeNo()+"支付宝关闭订单成功并删除支付二维码");
            		file.delete();
            	}else{
            		if("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())){
            			logger.info("订单号："+product.getOutTradeNo()+response.getSubMsg()+"(预下单 未扫码的情况)");
            		}else if("ACQ.TRADE_STATUS_ERROR".equals(response.getSubCode())){
            			logger.info("订单号："+product.getOutTradeNo()+response.getSubMsg());
            		}else{
            			logger.info("订单号："+product.getOutTradeNo()+"支付宝关闭订单失败"+response.getSubCode()+response.getSubMsg());
            			message = Constants.FAIL;
            		}
            	}
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = Constants.FAIL;
			logger.info("订单号："+product.getOutTradeNo()+"支付宝关闭订单异常");
		}
		return message;
	}
	@Override
	public String downloadBillUrl(String billDate,String billType) {
		logger.info("获取支付宝订单地址:"+billDate);
		String downloadBillUrl = "";
		try {
			AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
			request.setBizContent("{" + "    \"bill_type\":\"trade\","
					                   + "    \"bill_date\":\"2016-12-26\"" + "  }");
			
			AlipayDataDataserviceBillDownloadurlQueryResponse response
			                          = AliPayConfig.getAlipayClient().execute(request);
			if (response.isSuccess()) {
				logger.info("获取支付宝订单地址成功:"+billDate);
				downloadBillUrl = response.getBillDownloadUrl();//获取下载地
			} else {
				logger.info("获取支付宝订单地址失败"+response.getSubMsg()+":"+billDate);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("获取支付宝订单地址异常:"+billDate);
		}
		return downloadBillUrl;
	}
	@Override
	public String aliPayMobile(PayOrder product) {
		logger.info("支付宝手机支付下单");
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
		String returnUrl = "回调地址 http 自定义";
		alipayRequest.setReturnUrl(returnUrl);//前台通知
        alipayRequest.setNotifyUrl(notify_url);//后台回调
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", product.getOutTradeNo());
        bizContent.put("total_amount", product.getTotalAmount());//订单金额:元
        bizContent.put("subject",product.getSubject());//订单标题
        bizContent.put("seller_id", Configs.getPid());//实际收款账号，一般填写商户PID即可
        bizContent.put("product_code", "QUICK_WAP_PAY");//手机网页支付
		bizContent.put("body", "两个苹果五毛钱"); //TODO
		String biz = bizContent.toString().replaceAll("\"", "'");
        alipayRequest.setBizContent(biz);
        logger.info("业务参数:"+alipayRequest.getBizContent());
        String form = Constants.FAIL;
        try {
            form = AliPayConfig.getAlipayClient().pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
        	logger.error("支付宝构造表单失败",e);
        }
        return form;
	}
	
	
	/*****************************************
	 * 描述:电脑网站支付
	 *    直接向支付宝发起【支付请求】
	 *    电脑支付直接跳转到支付宝的官方支付界面（可以扫码也可以登录支付宝）
	 * 说明:
	 *    参见
	 * @param product
	 * @return
	 ****************************************/
	
	@Override
	public String aliPayPc(PayOrder product) {
		logger.info("支付宝PC支付下单,订单号:"+product.getOutTradeNo());
		String form = Constants.FAIL;
        String productCode = "FAST_INSTANT_TRADE_PAY"; //电脑网站支付
		
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(product.getOutTradeNo());
        model.setSubject(product.getSubject()); //订单标题
        model.setTotalAmount(product.getTotalAmount()); //订单金额:元
        //model.setBody(product.getSubject()+"测试");
        model.setProductCode(productCode);
        
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
		alipayRequest.setReturnUrl(return_url);//前台通知
        alipayRequest.setNotifyUrl(notify_url);//后台回调
        alipayRequest.setBizModel(model);
               
        try {
            form = AliPayConfig.getAlipayClient().pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
        	logger.error("支付宝构造表单失败",e);
        }
        return form;
	}
	
	
	/**
	 * 描述:
	 * 	   App支付服务端
	 *     App从服务单获取签名后的订单信息
	 * 说明:
	 * 	  https://docs.open.alipay.com/204/105297/
	 **/
	
	@Override
	public String appPay(PayOrder product) {
		
		String orderString = Constants.FAIL;
		// 实例化客户端
		AlipayClient alipayClient = AliPayConfig.getAlipayClient();
		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody(product.getBody());
		model.setSubject(product.getSubject());
		model.setOutTradeNo(product.getOutTradeNo());
		model.setTimeoutExpress("30m");
		model.setTotalAmount(product.getTotalAmount());
		model.setProductCode("QUICK_MSECURITY_PAY");
		request.setBizModel(model);
		request.setNotifyUrl(notify_url);
		
		try {
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipayTradeAppPayResponse response = alipayClient
					.sdkExecute(request);
			orderString  = response.getBody();//就是orderString 可以直接给客户端请求，无需再做处理。
			//System.out.println(response.getBody());
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return orderString ;
	}
	
	

	 // 简单打印应答
   private void dumpResponse(AlipayResponse response) {
       if (response != null) {
       	logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
           if (StringUtils.isNotEmpty(response.getSubCode())) {
           	logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(), response.getSubMsg()));
           }
           logger.info("body:" + response.getBody());
       }
   }
}
