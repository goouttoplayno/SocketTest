package com.example.sockettest;

import android.content.Context;
import android.content.Intent;
import android.icu.text.TimeZoneFormat;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int CODE_SOCKET_CONNECT = 8688;
    private Button bt_sent;
    private EditText et_receive;
    private PrintWriter mPrintWriter;
    private TextView tv_message;
     private Intent serviceIntent;
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_message.setText(tv_message.getText() +
                    "\n" + getCurrentTime() + "\n" + "客户端：" + msg.obj);
            et_receive.setText("");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
         serviceIntent = new Intent(this, SocketServerService.class);
        startService(serviceIntent);
        new Thread() {
            @Override
            public void run() {
                //连接到服务端
                connectSocketServer();
            }
        }.start();

    }

    private void initView() {
        final Context mcontext = this;
        et_receive = (EditText) findViewById(R.id.et_receive);
        bt_sent = (Button) findViewById(R.id.bt_send);
        tv_message = (TextView) findViewById(R.id.tv_message);
        bt_sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String msg = et_receive.getText().toString();
                        if (!TextUtils.isEmpty(msg) && null != mPrintWriter) {
                            //向服务器发送信息
                            Message m = new Message();
                            m.obj = msg;
                            mhandler.sendMessage(m);
                            mPrintWriter.println(msg);
                        }
                    }
                }).start();
                hideInput(mcontext, et_receive);
            }
        });
    }

    private void connectSocketServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                //一个流套接字，连接到服务端
                socket = new Socket("localhost", CODE_SOCKET_CONNECT);
                //获取到客户端的文本输出流
                //mPrintWriter中包含了socket的端口信息 debug看mprintwriter.out.out.
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
            }
        }
        try {
            //接收服务端发送的消息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!isFinishing()) {
                final String str = reader.readLine();
                if (str != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_message.setText(tv_message.getText() +
                                    "\n" + getCurrentTime() + "\n" + "服务端：" + str);
                        }
                    });
                }
            }
            mPrintWriter.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //new Date()获取当前系统时间
        return df.format(new Date());
    }

    private void hideInput(Context context, View view){
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
    }
}
