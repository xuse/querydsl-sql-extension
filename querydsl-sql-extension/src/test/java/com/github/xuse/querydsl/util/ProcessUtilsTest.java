package com.github.xuse.querydsl.util;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.ProcessUtil.NetworkInfo;

public class ProcessUtilsTest {
	@Test
	public void testProcess() {
		NetworkInfo[] networks=ProcessUtil.getActiveNetwork();
		assertTrue(networks.length>0);
		NetworkInfo network=networks[0];
		
		
		//本机地址
		String text=network.getIpAddress();
		System.out.println(text);
		assertTrue(text.length()>0);
		
		text=network.getBroadcastAddress();
		System.out.println(text);
		assertTrue(text.endsWith(".255"));
		
		//主机名
		text = network.getCanonicalHostName();
		System.out.println(text);
		assertTrue(text.length()>0);
		
		//主机名
		text=network.getHostName();
		System.out.println(text);
		assertTrue(text.length()>0);
		
		//网卡名
		text=network.getDisplayName();
		System.out.println(text);
		assertTrue(text.length()>0);
		
		//MAC
		text=network.getMac();
		System.out.println(text);
		assertTrue(text.length()>0);
		
		//MASK
		text=network.getMaskAddress();
		System.out.println(text);
		assertTrue(text.length()>0);
		
		//eth0
		text=network.getName();
		System.out.println(text);
		assertTrue(text.length()>0);
		
		int mtu=network.getMTU();
		System.out.println(mtu);
		assertTrue(text.length()>0);
		
		text=network.toString();
		assertTrue(text.length()>0);
		
		long start=ProcessUtil.getStartPeriod();
		assertTrue(start>0);
		
		int port=ProcessUtil.getFreePort();
		assertTrue(port>0);
		
		String str=ProcessUtil.getHostname();
		assertTrue(str.length()>0);
		
		String ip=ProcessUtil.getLocalIp();
		assertTrue(ip.length()>0);
		
		String os=ProcessUtil.getOSName();
		assertTrue(os.length()>0);
		
		int pid=ProcessUtil.getPid();
		assertTrue(pid>0);
		
		assertTrue(ProcessUtil.is64BitOs());
		
		String testHost="www.baidu.com";
		try {
			String remote=ProcessUtil.getRemoteIp(testHost);
			System.out.println(testHost + " -> " + remote);
			assertTrue(remote.length()>0);
		}catch(UnknownHostException e) {
			System.out.println("Unable to analysis DNS "+testHost);
		}
	}
}
