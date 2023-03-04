package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();
        if(user.getConnected()){
            throw new Exception("Already connected");
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("IND","001");
        map.put("USA","002");
        map.put("AUS","003");
        map.put("CHI","004");
        map.put("JPN","005");
        countryName = countryName.toUpperCase();
        if(map.get(countryName).equals(user.getCountry().getCode())){
            System.out.println(user.getCountry().getCountryName()+" same is equal to both");
            return user;
        }
        // now check the service provider which provide vpn this country
        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
        List<ServiceProvider> offerService = new ArrayList<>();
        for(ServiceProvider serviceProvider : serviceProviderList){
            List<Country> countryList = serviceProvider.getCountryList();
            for(Country country:countryList){
                if(country.getCode().equals(map.get(countryName))){
                    offerService.add(serviceProvider);
                    break;
                }
            }
        }
        if(offerService.size()==0){
            System.out.println("size is the major drawback is there "+offerService.size());
            throw new Exception("Unable to connect");
        }
        ServiceProvider serviceProvider1 = null;
        int id = 0;
        for(ServiceProvider serviceProvider : offerService){
            if(serviceProvider.getId()>id){
                serviceProvider1 = serviceProvider;
                id = serviceProvider.getId();
            }
        }

        // now make a connection
        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(serviceProvider1);

        // connection is all set

        // update connection in service provider
        serviceProvider1.getConnectionList().add(connection);
        serviceProvider1.getUsers().add(user);
        // update user table also
        user.setConnected(true);
        user.getConnectionList().add(connection);


        countryName = countryName.toUpperCase();
        String newCode = map.get(countryName);
        String updateIp = newCode+"."+serviceProvider1.getId()+"."+user.getId();
        user.setMaskedIp(updateIp);
        serviceProviderRepository2.save(serviceProvider1);
        userRepository2.save(user);

        return user;

    }
    @Override
    public User disconnect(int userId) throws Exception {

        User user = userRepository2.findById(userId).get();
        if(user.getConnected()==false){
            throw new Exception("Already disconnected");
        }
        user.setConnected(false);
        user.setMaskedIp(null);

        userRepository2.save(user);

        return user;

    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        HashMap<String, String> map = new HashMap<>();
        map.put("001","IND");
        map.put("002","USA");
        map.put("003","AUS");
        map.put("004","CHI");
        map.put("005","JPN");
        String receiverCurrentCode = null;
        String[] ip;
        if(receiver.getConnected()==true)
        {
            ip = receiver.getMaskedIp().split(".");
            receiverCurrentCode = ip[0];
        }
        else {
            ip = receiver.getOriginalIp().split(".");
            receiverCurrentCode = ip[0];
        }

        if(sender.getConnected()==true){
            String[] senIp = sender.getMaskedIp().split(".");
            if(receiverCurrentCode.equals(senIp[0])){
                System.out.println("communication establish between both sender and receiver");
                return sender;
            }
            else {
                System.out.println("No Communication is possible between the both the things ");
                throw new Exception("Cannot establish communication");
            }
        }
        // sender is not connected
        String[] ip1 = sender.getOriginalIp().split(".");
        if(receiverCurrentCode.equals(ip1[0])){
            System.out.println("communication establish between both sender and receiver");
            return sender;
        }
        else{
            // make a connection request to make a new connection
            connect(senderId,map.get(receiverCurrentCode));
            System.out.println("communication establish between both sender and receiver");
        }
        return sender;

    }
}
