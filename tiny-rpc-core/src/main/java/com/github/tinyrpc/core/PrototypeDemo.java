package com.github.tinyrpc.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrototypeDemo {

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    static class Message {
        String serviceKey;
        String methodName;
        Object[] args;

        boolean success;
        String responseMessage;
        Object returnValue;

        public Message(String serviceKey, String methodName, Object[] args) {
            this.serviceKey = serviceKey;
            this.methodName = methodName;
            this.args = args;
        }

        public Message(Object returnValue) {
            this.success = true;
            this.returnValue = returnValue;
        }

        public Message(boolean success, String responseMessage) {
            this.success = success;
            this.responseMessage = responseMessage;
        }
    }

    static class Util {
        static Message deserialize(InputStream is) {
            return null;
        }

        static byte[] serialize(Message os) {
            return null;
        }

        static Message readMessage(Socket socket) {
            return null;
        }

        static void writeMessage(Socket socket, Message message) {
        }

        static Object callMethod(Object obj, String methodName, Object[] args) {
            return null;
        }
    }

    static class Server {
        private final int port;
        Map<String, Object> services = new ConcurrentHashMap<>();

        public Server(int port) {
            this.port = port;
        }

        public void register(Object service) {
            services.put(service.getClass().getCanonicalName(), service);
        }

        public void listen() throws IOException {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Message message = Util.readMessage(clientSocket);
                Object service = services.get(message.getServiceKey());
                Object returnValue = Util.callMethod(service, message.methodName, message.getArgs());
                Util.writeMessage(clientSocket, new Message(returnValue));
            }
        }

        public static void main(String[] args) throws IOException {
            Server server = new Server(9000);
            server.register(new HelloAPIImpl());
            server.listen();
        }
    }

    static class Client {
        public static void main(String[] args) {
            Client client = new Client("127.0.0.1",9000);
            HelloAPIImpl helloAPI = client.lookup(HelloAPIImpl.class);
            helloAPI.hello();
        }

        private final String serverHost;
        private final int serverPort;

        public Client(String serverHost, int serverPort) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
        }

        public <T> T lookup(Class<T> c) {
            //proxy object should be cached
            return createAPIProxy(c);
        }

        @SuppressWarnings("unchecked")
        private <T> T createAPIProxy(Class<T> apiClazz) {
            Thread.currentThread().getContextClassLoader();
            return (T) Proxy.newProxyInstance(PrototypeDemo.class.getClassLoader(), new Class[]{apiClazz}, (obj, method, args) -> {
                //1.serialize the input arguments
                System.out.println("序列化参数:" + args);
                //2.call remote service
                Message request = new Message(obj.getClass().getCanonicalName(), method.getName(), args);
                System.out.println("调用接口:" + request);
                Socket socket = new Socket(serverHost, serverPort);
                Util.writeMessage(socket, request);
                Message response = Util.readMessage(socket);
                //3.deserialize the return value
                System.out.println("反序列化结果:" + response);
                //4.convert return value as the method's return type
                return null;
            });
        }
    }


    interface HelloAPI {
        String hello();
    }

    static class HelloAPIImpl implements HelloAPI {

        @Override
        public String hello() {
            return "ohayo~";
        }
    }

}
