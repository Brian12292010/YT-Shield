package com.brian.ytshield;
import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
public class MainActivity extends Activity {
  TextView status, stats; EditText deny, allow;
  @Override public void onCreate(Bundle b){super.onCreate(b);
    LinearLayout l=new LinearLayout(this); l.setPadding(28,32,28,24); l.setOrientation(1);
    TextView title=new TextView(this); title.setText("YT Shield"); title.setTextSize(30); l.addView(title);
    status=new TextView(this); status.setText("本機 DNS 廣告與追蹤網域過濾"); status.setPadding(0,20,0,12); l.addView(status);
    Button start=new Button(this); start.setText("啟用防護"); start.setOnClickListener(v->{Intent i=VpnService.prepare(this);if(i!=null)startActivityForResult(i,1);else startService(new Intent(this,DnsVpnService.class));}); l.addView(start);
    Button stop=new Button(this); stop.setText("停止防護"); stop.setOnClickListener(v->{stopService(new Intent(this,DnsVpnService.class));status.setText("防護已停止");}); l.addView(stop);
    stats=new TextView(this); l.addView(stats);
    deny=field(l,"封鎖網域（每行一個）"); allow=field(l,"白名單（每行一個，優先允許）");
    Button save=new Button(this); save.setText("儲存規則"); save.setOnClickListener(v->{getSharedPreferences("shield",0).edit().putString("deny",deny.getText().toString()).putString("allow",allow.getText().toString()).apply();sendBroadcast(new Intent("com.brian.ytshield.RELOAD").setPackage(getPackageName()));}); l.addView(save);
    Button reset=new Button(this); reset.setText("重設統計"); reset.setOnClickListener(v->{getSharedPreferences("shield",0).edit().putLong("queries",0).putLong("blocked",0).apply();refresh();}); l.addView(reset);
    TextView note=new TextView(this); note.setText("\nDNS 過濾無法可靠移除官方 YouTube App 的影片內廣告；此版本不解密 HTTPS。"); note.setTextSize(13); l.addView(note);
    ScrollView s=new ScrollView(this);s.addView(l);setContentView(s);
    android.content.SharedPreferences p=getSharedPreferences("shield",0);deny.setText(p.getString("deny",""));allow.setText(p.getString("allow",""));refresh();
  }
  EditText field(LinearLayout l,String label){TextView t=new TextView(this);t.setText(label);l.addView(t);EditText e=new EditText(this);e.setMinLines(2);e.setGravity(Gravity.TOP);l.addView(e);return e;}
  void refresh(){android.content.SharedPreferences p=getSharedPreferences("shield",0);stats.setText("DNS 查詢："+p.getLong("queries",0)+"  已封鎖："+p.getLong("blocked",0));}
  @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==1&&c==RESULT_OK){startService(new Intent(this,DnsVpnService.class));status.setText("防護啟動中");}}
}
