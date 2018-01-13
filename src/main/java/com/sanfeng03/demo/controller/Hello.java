package com.sanfeng03.demo.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import weixin.popular.bean.message.EventMessage;
import weixin.popular.bean.xmlmessage.XMLImageMessage;
import weixin.popular.bean.xmlmessage.XMLMessage;
import weixin.popular.bean.xmlmessage.XMLTextMessage;
import weixin.popular.support.ExpireKey;
import weixin.popular.support.TokenManager;
import weixin.popular.support.expirekey.DefaultExpireKey;
import weixin.popular.util.SignatureUtil;
import weixin.popular.util.XMLConverUtil;

@Controller
public class Hello {
	private String appId = TT.APPID; // appid 通过微信后台获取
	private String token = TT.TOKEN; // 通过微信后台获取

	// 重复通知过滤
	private static ExpireKey expireKey = new DefaultExpireKey();

	@RequestMapping(value = "/Hello")
	public String HelloWorld(Model model) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();

		// 加密模式
		String encrypt_type = request.getParameter("encrypt_type");
		String msg_signature = request.getParameter("msg_signature");

		model.addAttribute("message", "Hello World!!!");
		return "HelloWorld";
	}

	@RequestMapping(value = "/wx")
	public void receive(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (null == token) {
			token = TokenManager.getToken(TT.APPID);
			System.out.println(token);
		}
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		ServletInputStream inputStream = request.getInputStream();
		ServletOutputStream outputStream = response.getOutputStream();

		// 首次请求申请验证,返回echostr
		if (echostr != null) {
			outputStreamWrite(outputStream, echostr);
			return;
		}

		// 验证请求签名
		if (!signature.equals(SignatureUtil.generateEventMessageSignature(
				token, timestamp, nonce))) {
			System.out.println("The request signature is invalid");
			return;
		}
		if (inputStream != null) {
			// 转换XML
			EventMessage eventMessage = XMLConverUtil.convertToObject(
					EventMessage.class, inputStream);
			String key = eventMessage.getFromUserName() + "__"
					+ eventMessage.getToUserName() + "__"
					+ eventMessage.getMsgId() + "__"
					+ eventMessage.getCreateTime();
			if (expireKey.exists(key)) {
				// 重复通知不作处理
				return;
			} else {
				expireKey.add(key);
			}
			if ("image".equals(eventMessage.getMsgType())) {
				XMLMessage xmlImageMessage = new XMLImageMessage(
						eventMessage.getFromUserName(),
						eventMessage.getToUserName(), eventMessage.getMediaId());
				// 回复
				xmlImageMessage.outputStreamWrite(outputStream);
				return;
			}
			// 创建回复
			XMLMessage xmlTextMessage = new XMLTextMessage(
					eventMessage.getFromUserName(),
					eventMessage.getToUserName(), "你好");
			// 回复
			xmlTextMessage.outputStreamWrite(outputStream);
			return;
		}
		outputStreamWrite(outputStream, "");
	}

	/**
	 * 数据流输出
	 * 
	 * @param outputStream
	 * @param text
	 * @return
	 */
	private boolean outputStreamWrite(OutputStream outputStream, String text) {
		try {
			outputStream.write(text.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
