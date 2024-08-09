package com.github.xuse.querydsl.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 跟进程相关的以及环境相关的若干工具方法
 * 
 * 这个工具类中还有若干和网络地址相关方法，比如获取本机的网络信息，空闲端口等
 * 
 * @author Administrator 部分功能使用了JDK 1.6中的方法，因此本类的使用范围要求JDK 1.6
 * @since 1.6
 */
@Slf4j
public class ProcessUtil {
	private static final int processId;
	private static final String hostname;
	private static final long startTime;

	static {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName(); // format: "pid@hostname"
		processId = Integer.parseInt(name.substring(0, name.indexOf('@')));
		hostname = name.substring(name.indexOf('@') + 1);
		startTime = runtime.getStartTime();
	}

	/**
	 * @return 获取进程号
	 */
	public static int getPid() {
		return processId;
	}

	/**
	 * 网络适配器信息
	 *
	 */
	public static class NetworkInfo {
		NetworkInterface net;
		InetAddress addr;

		NetworkInfo(NetworkInterface t) {
			this.net = t;
			Enumeration<InetAddress> addrs = net.getInetAddresses();
			addr = addrs.nextElement();
			if (addrs.hasMoreElements()) {
				log.warn("The network " + net.getDisplayName() + " has more than one address");
			}
		}

		/**
		 * @return 获得广播地址
		 */
		public String getBroadcastAddress() {
			InterfaceAddress addr = net.getInterfaceAddresses().get(0);
			return addr.getBroadcast().getHostAddress();
		}

		/**
		 * 
		 * @return 获得子网掩码
		 */
		public String getMaskAddress() {
			InterfaceAddress addr = net.getInterfaceAddresses().get(0);
			int add = addr.getNetworkPrefixLength();
			String[] s = new String[] { "0", "128", "192", "224", "240", "248", "252", "254", "255" };
			if (add > 24) {
				return "255.255.255." + s[add - 24];
			} else if (add > 16) {
				return "255.255." + s[add - 16] + ".0";
			} else if (add > 8) {
				return "255." + s[add - 8] + ".0.0";
			} else {
				return s[add] + ".0.0.0";
			}
		}

		/**
		 * @return 获得IP地址
		 */
		public String getIpAddress() {
			return addr.getHostAddress();
		}

		/**
		 * @return 获得主机名
		 */
		public String getHostName() {
			return addr.getHostName();
		}

		/**
		 * @return 获得缩写主机名
		 */
		public String getCanonicalHostName() {
			return addr.getCanonicalHostName();
		}

		/**
		 * 注意JDK有BUG，无线网卡的MAC地址不能获得，会返回有线网卡的MAC地址
		 * 
		 * @return 获得Mac地址
		 */
		public String getMac() {
			try {
				return StringUtils.join(net.getHardwareAddress(), '-');
			} catch (SocketException e) {
				throw Exceptions.toRuntime(e);
			}
		}

		/**
		 * @return 获得最大传输单元
		 */
		public int getMTU() {
			try {
				return net.getMTU();
			} catch (SocketException e) {
				throw Exceptions.toRuntime(e);
			}
		}

		/**
		 * @return 获得名称
		 */
		public String getName() {
			return net.getName();
		}

		/**
		 * @return 获得适配器名称
		 */
		public String getDisplayName() {
			return net.getDisplayName();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getName()).append(' ');
			sb.append(getIpAddress()).append('|').append(getHostName());
			return sb.toString();
		}
	}

	/**
	 * lo 127.0.0.1也不会在这里出现
	 * 
	 * @return 获得现有活动的网络连接信息。未连接的不会在这里出现
	 */
	public static NetworkInfo[] getActiveNetwork() {
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (IOException e) {
			throw Exceptions.toRuntime(e);
		}
		List<NetworkInfo> n = new ArrayList<NetworkInfo>();
		while (nets.hasMoreElements()) {
			NetworkInterface t = nets.nextElement();
			try {
				byte[] mac = t.getHardwareAddress();
				if (mac != null && mac.length == 6 && t.getInetAddresses().hasMoreElements()) {
					n.add(new NetworkInfo(t));
				}
			} catch (IOException e) {
				throw Exceptions.toRuntime(e);
			}
		}
		return n.toArray(new NetworkInfo[n.size()]);
	}

	/**
	 * @return 获取JVM启动时间。（毫秒数）
	 */
	public static long getStarttime() {
		return startTime;
	}

	/**
	 * @return 获取一个空闲端口
	 */
	public static int getFreePort() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(0); // 读取空闲的可用端口
			int port = serverSocket.getLocalPort();
			return port;
		} catch (Exception e) {
			return 0;
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 
	 * @return 获取当前主机名
	 */
	public static String getHostname() {
		return hostname;
	}

	/**
	 * @return 判断当前的操作系统是不是64位的操作系统
	 */
	public static boolean is64BitOs() {
		return System.getProperty("java.vm.name").indexOf("64") >= 0;
	}

	/**
	 * @return 获取当前操作系统名称. return 操作系统名称 例如:windows xp,linux 等.
	 */
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}

	/**
	 * <p>
	 * 方法 getLocalIp
	 * </p>
	 * 
	 * @return 获取本机ip地址
	 */
	public static String getLocalIp() {
		String localIp = null;
		try {
			localIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw Exceptions.toRuntime(e);
		}
		return localIp;
	}

	public static String getRemoteIp(String hostName) throws UnknownHostException {
		InetAddress addr = InetAddress.getByName(hostName);
		return addr.getHostAddress();
	}
}
