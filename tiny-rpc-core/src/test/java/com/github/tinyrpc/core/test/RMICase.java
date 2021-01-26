package com.github.tinyrpc.core.test;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMICase {
    static class Server {
        public static void main(String[] args) throws RemoteException, AlreadyBoundException {
            Remote service = UnicastRemoteObject.exportObject(new HelloServiceImpl(), 0);
            Registry registry = LocateRegistry.createRegistry(9000);
            registry.bind("Hello", service);
        }
    }

    static class Client {
        public static void main(String[] args) throws RemoteException, NotBoundException {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9000);
            HelloService service = (HelloService) registry.lookup("Hello");
            String r = service.hello();
            System.out.println(r);
        }
    }


    static class HelloServiceImpl implements HelloService {
        @Override
        public String hello() {
            return "ohayo~";
        }
    }

    interface HelloService extends Remote {
        String hello() throws RemoteException;
    }
}
