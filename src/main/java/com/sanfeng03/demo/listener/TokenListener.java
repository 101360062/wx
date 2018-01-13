package com.sanfeng03.demo.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sanfeng03.demo.controller.TT;

import weixin.popular.support.TokenManager;

public class TokenListener implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//WEB容器 初始化时调用
		TokenManager.init(TT.APPID,TT.SECRET);
		System.out.println(TokenManager.getToken(TT.APPID));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		//WEB容器  关闭时调用
		TokenManager.destroyed();
	}
}
