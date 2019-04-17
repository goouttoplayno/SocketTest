package com.example.sockettest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.WildcardType;
import java.net.ServerSocket;
import java.net.Socket;

import javax.crypto.Cipher;

public class SocketServerService extends Service {
    private static final String TAG = "SocketServerServer";
    private boolean isServiceDestroy = false;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new TcpServer()).start();
        Log.d(TAG, "服务端已onCreate");
    }

    private class TcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket;
            try {
                //监听指定的端口
                serverSocket = new ServerSocket(MainActivity.CODE_SOCKET_CONNECT);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (!isServiceDestroy) {
                try {
                    final Socket client = serverSocket.accept();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 响应给客户端信息
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        //获取输入流  用于接收客户端信息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //获取输出流  用于向客户端发送消息
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
        out.println("服务端已连接");
        while (!isServiceDestroy){
            String str = in.readLine();
            Log.d(TAG, "服务器收到的信息：" + str);
            if (TextUtils.isEmpty(str)){
                Log.d(TAG, "服务器收到的信息为空，已断开连接");
                break;
            }
            //从客户端收到的信息加工在发送给客户端
            out.println("收到了客户端发来的消息------" + str);
        }
        in.close();
        out.close();
        client.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceDestroy = true;
    }
}
