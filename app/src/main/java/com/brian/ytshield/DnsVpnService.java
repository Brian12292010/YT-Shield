package com.brian.ytshield;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.content.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class DnsVpnService extends VpnService {
  ParcelFileDescriptor tun; volatile boolean running; Set<String> block=Collections.synchronizedSet(new HashSet<>()), allow=Collections.synchronizedSet(new HashSet<>());
  @Override public void onCreate(){super.onCreate();load();registerReceiver(new BroadcastReceiver(){public void onReceive(Context c,Intent i){load();}},new IntentFilter("com.brian.ytshield.RELOAD"));}
  void load(){block.clear();allow.clear();add(block,"doubleclick.net\ngooglesyndication.com\ngoogleadservices.com\nadservice.google.com\npagead2.googlesyndication.com\nadnxs.com\nadform.net");android.content.SharedPreferences p=getSharedPreferences("shield",0);add(block,p.getString("deny",""));add(allow,p.getString("allow",""));}
  void add(Set<String>s,String v){for(String x:v.split("\\R")){x=x.trim().toLowerCase(Locale.ROOT);if(x.startsWith("||"))x=x.substring(2);if(x.endsWith("^"))x=x.substring(0,x.length()-1);if(x.matches("[a-z0-9.-]+")&&x.contains("."))s.add(x);}}
  @Override public int onStartCommand(Intent i,int f,int id){if(tun==null)startTunnel();return START_STICKY;}
  void startTunnel(){try{tun=new Builder().setSession("YT Shield").addAddress("10.10.10.2",32).addDnsServer("10.10.10.1").addRoute("10.10.10.1",32).setMtu(1500).establish();if(tun==null){stopSelf();return;}running=true;new Thread(this::loop).start();}catch(Exception e){stopSelf();}}
  void loop(){byte[] b=new byte[32767];try(FileInputStream in=new FileInputStream(tun.getFileDescriptor());FileOutputStream out=new FileOutputStream(tun.getFileDescriptor())){while(running){int n=in.read(b);if(n<28)continue;int ih=(b[0]&15)*4;if((b[0]>>4)!=4||(b[9]&255)!=17||ih+8>n||u16(b,ih+2)!=53)continue;int ul=u16(b,ih+4),off=ih+8;if(ul<20||ih+ul>n)continue;String host=name(b,off+12,off+ul);if(host==null)continue;android.content.SharedPreferences p=getSharedPreferences("shield",0);p.edit().putLong("queries",p.getLong("queries",0)+1).apply();byte[] ans;if(denied(host)){ans=Arrays.copyOfRange(b,off,off+ul);ans[2]=(byte)0x81;ans[3]=(byte)0x83;ans[6]=ans[7]=ans[8]=ans[9]=0;p.edit().putLong("blocked",p.getLong("blocked",0)+1).apply();}else{ans=upstream(b,off,ul);if(ans==null)continue;}out.write(reply(b,ih,ans));}}catch(Exception ignored){}}
  boolean denied(String h){for(String d:allow)if(h.equals(d)||h.endsWith("."+d))return false;for(String d:block)if(h.equals(d)||h.endsWith("."+d))return true;return false;}
  String name(byte[]b,int p,int end){try{StringBuilder s=new StringBuilder();while(p<end){int n=b[p++]&255;if(n==0)break;if((n&192)!=0||p+n>end)return null;if(s.length()>0)s.append('.');s.append(new String(b,p,n,StandardCharsets.US_ASCII));p+=n;}return s.length()==0?null:s.toString().toLowerCase(Locale.ROOT);}catch(Exception e){return null;}}
  byte[] upstream(byte[]b,int o,int n){try{DatagramSocket s=new DatagramSocket();if(!protect(s)){s.close();return null;}s.setSoTimeout(3000);byte[]q=Arrays.copyOfRange(b,o,o+n);s.send(new DatagramPacket(q,q.length,InetAddress.getByName("1.1.1.1"),53));byte[]x=new byte[4096];DatagramPacket r=new DatagramPacket(x,x.length);s.receive(r);s.close();return Arrays.copyOf(x,r.getLength());}catch(Exception e){return null;}}
  int u16(byte[]b,int p){return ((b[p]&255)<<8)|(b[p+1]&255);} void put(byte[]b,int p,int v){b[p]=(byte)(v>>8);b[p+1]=(byte)v;}
  byte[] reply(byte[]q,int ih,byte[]dns){int len=28+dns.length;byte[]r=new byte[len];r[0]=0x45;r[2]=(byte)(len>>8);r[3]=(byte)len;r[6]=0x40;r[8]=64;r[9]=17;System.arraycopy(q,16,r,12,4);System.arraycopy(q,12,r,16,4);put(r,20,53);put(r,22,u16(q,ih));put(r,24,8+dns.length);System.arraycopy(dns,0,r,28,dns.length);put(r,10,checksum(r,0,20));return r;}
  int checksum(byte[]b,int o,int n){long s=0;for(int i=o;i<o+n;i+=2)s+=((b[i]&255)<<8)|(i+1<o+n?(b[i+1]&255):0);while((s>>16)!=0)s=(s&65535)+(s>>16);return (int)(~s)&65535;}
  @Override public void onDestroy(){running=false;try{if(tun!=null)tun.close();}catch(Exception e){}tun=null;super.onDestroy();}
}
