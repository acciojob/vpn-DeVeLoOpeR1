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
        if(map.get(countryName).equals(user.getOriginalCountry().getCode())){
            System.out.println(user.getOriginalCountry().getCountryName()+" same is equal to both");
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
        int id = 1000;
        for(ServiceProvider serviceProvider : offerService){
            if(serviceProvider.getId()<id){
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

        if(!sender.getConnected() && !receiver.getConnected() && sender.getOriginalCountry().getCode().equals(receiver.getOriginalCountry().getCode()))
        {
            System.out.println("communication success and sender and receiver is not connected");
            return sender;
        }
        if(!sender.getConnected() && receiver.getConnected() && sender.getOriginalCountry().getCode().equals(receiver.getMaskedIp().split(".")[0]) ){
            System.out.println("communication success and sender is not connected and RECEIVER is CONNECTED");
            return sender;
        }
        if(sender.getConnected() && !receiver.getConnected() && receiver.getOriginalCountry().getCode().equals(sender.getMaskedIp().split(".")[0]) ){
            System.out.println("communication success and SENDER is CONNECTED and RECEIVER is NOT CONNECTED");
            return sender;
        }
        if(sender.getConnected() && receiver.getConnected())
        {
            if(sender.getMaskedIp().split(".")[0].equals(receiver.getMaskedIp().split(".")[0])){

                System.out.println("communication success and SENDER is CONNECTED and RECEIVER is CONNECTED");
                return sender;
            }
            else{
                System.out.println("communication establish between both sender and receiver");
                throw new Exception("Cannot establish communication");
            }

        }
        // make a connection request to make a new connection
        if(receiver.getConnected()){
           sender = connect(senderId,map.get(receiver.getMaskedIp().split(".")[0]));
        }
        else{
            sender = connect(senderId,map.get(receiver.getOriginalCountry().getCode()));
        }

        System.out.println("communication establish by changing the SENDER VALUES TO BE THERE");
        return sender;

    }
}
