package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {
        // create a new admin entity
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);

        // save the admin entity
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {

        // find the id from the table
        Admin admin = adminRepository1.findById(adminId).get();
        // create a new service provider
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setName(providerName);
        serviceProvider.setAdmin(admin);

        // set the provider list in admin
        admin.getServiceProviders().add(serviceProvider);
        //save the parent entity admin and child will saved automatically due to cascade effect
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{

        System.out.println(serviceProviderId+"  service provider id");
        ServiceProvider serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();

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

        country.setUser(null);
        country.setServiceProvider(serviceProvider);

        // add country to the service provider list and save the service provider
        serviceProvider.getCountryList().add(country);

        serviceProviderRepository1.save(serviceProvider);
        return serviceProvider;

    }
}
