package org.yipuran.gsonhelper.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * X.509証明書検証スキップする実装.
 * SSLContext 設定で使用する。
 * <PRE>
 * （使用例）
 *      SSLContext ctx = SSLContext.getInstance("SSL");
 *      ctx.init(null, new X509TrustManager[]{ new NonAuthentication() }, null);
 *      SSLSocketFactory factory = ctx.getSocketFactory();
 *
 *      Url url;
 *      // url セットする。
 *      HttpsURLConnection uc;
 *      if (proxy_server != null){
 *         // Proxy経由
 *         String proxy_server; // Proxyサーバ名
 *         String proxyuser;    // Proxyユーザ名、
 *         String proxypass;    // Proxyパスワード
 *         Integer port;        // Proxy使用 port番号
 *
 *         if (proxyuser != null && proxypass != null){
 *            Authenticator.setDefault(new Authenticator(){
 *                ＠Override
 *                protected PasswordAuthentication getPasswordAuthentication(){
 *                   return new PasswordAuthentication(user, passwd.toCharArray());
 *                }
 *            });
 *         }
 *         Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_server, Optional.ofNullable(port).orElse(80)));
 *         uc = (HttpsURLConnection)url.openConnection(proxy);
 *      }else{
 *         uc = (HttpsURLConnection)url.openConnection();
 *      }
 *      uc.setDefaultHostnameVerifier(new HostnameVerifier(){
 *         ＠Override
 *         public boolean verify(String hostname, SSLSession session){
 *            return true;
 *         }
 *      });
 *      uc.setSSLSocketFactory(factory);
 *      uc.setDoOutput(true);
 *
 * </PRE>
 */
public class NonAuthentication implements X509TrustManager{
	/* @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String) */
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException{
	}
	/* @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String) */
	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException{
	}
	/* @see javax.net.ssl.X509TrustManager#getAcceptedIssuers() */
	@Override
	public X509Certificate[] getAcceptedIssuers(){
		return null;
	}
}
