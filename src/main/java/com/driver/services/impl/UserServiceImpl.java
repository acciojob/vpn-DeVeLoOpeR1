package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{

        // create a new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setMaskedIp(null);
        user.setConnected(false);

        // create a country entity
        HashMap<String, String> map = new HashMap<>();
        map.put("IND","001");
        map.put("USA","002");
        map.put("AUS","003");
        map.put("CHI","004");
        map.put("JPN","005");

        countryName = countryName.toUpperCase();

        if(!map.containsKey(countryName)){
            throw new Exception("Country not found");
        }
        Country country = new Country();
        if(countryName.equals("IND")){
            country.setCountryName(CountryName.IND);
            country.setCode("001");
        }
        else if(countryName.equals("USA")){
            country.setCountryName(CountryName.USA);
            country.setCode("002");
        }
        else if(countryName.equals("AUS")){
            country.setCountryName(CountryName.AUS);
            country.setCode("003");
        }
        else if(countryName.equals("CHI")){
            country.setCountryName(CountryName.CHI);
            country.setCode("004");
        }
        else if(countryName.equals("JPN")){
            country.setCountryName(CountryName.JPN);
            country.setCode("005");
        }

        country.setUser(user);


        String orgIp = (country.getCode()+"."+user.getId());
        user.setOriginalIp(orgIp);
        user.setOriginalCountry(country);
        userRepository3.save(user);
        return user;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {

        // find the service provider
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();
        User user = userRepository3.findById(userId).get();

        user.getServiceProviderList().add(serviceProvider);
        serviceProvider.getUsers().add(user);

        serviceProviderRepository3.save(serviceProvider);

        return user;
    }
}
