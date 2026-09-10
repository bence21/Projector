package com.bence.songbook.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class LocalNetwork {

    private static final String TAG = LocalNetwork.class.getSimpleName();

    public static Network findLocalNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return null;
        }
        Network[] networks = connectivityManager.getAllNetworks();
        if (networks == null) {
            return null;
        }
        for (Network network : networks) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                continue;
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                continue;
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return network;
            }
        }
        return null;
    }

    public static void bindSocket(Context context, Socket socket) {
        if (context == null || socket == null) {
            return;
        }
        try {
            Network network = findLocalNetwork(context);
            if (network != null) {
                network.bindSocket(socket);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static List<String> getRfc1918Addresses() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            if (enumeration == null) {
                return ips;
            }
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                try {
                    if (networkInterface.isLoopback() || !networkInterface.isUp() || isLikelyCellularOrVirtual(networkInterface)) {
                        continue;
                    }
                } catch (SocketException e) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!(address instanceof Inet4Address) || address.isLoopbackAddress() || address.isLinkLocalAddress()) {
                        continue;
                    }
                    String hostAddress = address.getHostAddress();
                    if (isRfc1918Ipv4(hostAddress)) {
                        ips.add(hostAddress);
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return ips;
    }

    @SuppressWarnings("deprecation")
    public static String getDhcpGateway(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return null;
            }
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo == null || dhcpInfo.gateway == 0) {
                return null;
            }
            String gateway = intToIpv4(dhcpInfo.gateway);
            if (isRfc1918Ipv4(gateway)) {
                return gateway;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static String subnetPrefix(String ipv4) {
        if (!isValidIpv4(ipv4)) {
            return null;
        }
        String[] split = ipv4.split("\\.");
        return split[0] + "." + split[1] + "." + split[2] + ".";
    }

    public static boolean isRfc1918Ipv4(String hostAddress) {
        if (!isValidIpv4(hostAddress)) {
            return false;
        }
        String[] split = hostAddress.split("\\.");
        int first = Integer.parseInt(split[0]);
        int second = Integer.parseInt(split[1]);
        if (first == 10) {
            return true;
        }
        if (first == 192 && second == 168) {
            return true;
        }
        return first == 172 && second >= 16 && second <= 31;
    }

    public static boolean isValidIpv4(String hostAddress) {
        if (hostAddress == null) {
            return false;
        }
        String[] split = hostAddress.split("\\.");
        if (split.length != 4) {
            return false;
        }
        for (String part : split) {
            if (part.isEmpty()) {
                return false;
            }
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
                if (!Integer.toString(value).equals(part)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLikelyCellularOrVirtual(NetworkInterface networkInterface) {
        String name = networkInterface.getName();
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.US);
        return lower.startsWith("rmnet")
                || lower.startsWith("ccmni")
                || lower.startsWith("clat")
                || lower.startsWith("dummy")
                || lower.startsWith("tun")
                || lower.startsWith("ppp")
                || lower.startsWith("radio");
    }

    private static String intToIpv4(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }
}
