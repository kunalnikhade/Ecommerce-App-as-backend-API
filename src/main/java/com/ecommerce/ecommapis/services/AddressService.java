package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.AddressDto;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import com.ecommerce.ecommapis.exception.UserNameNotFoundException;
import com.ecommerce.ecommapis.model.AddressEntity;
import com.ecommerce.ecommapis.model.auth.UserEntity;
import com.ecommerce.ecommapis.repositories.AddressRepository;
import com.ecommerce.ecommapis.repositories.auth.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AddressService
{
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(final AddressRepository addressRepository, final UserRepository userRepository)
    {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AddressDto createNewAddress(final AddressDto address)
    {
        final UserEntity user = userRepository.findById(address.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final AddressEntity addressEntity = convertToEntity(address);
        addressEntity.setUser(user);

        return convertToDto(addressRepository.save(addressEntity));
    }

    @Transactional
    public AddressDto updateAddress(final UUID addressId, final AddressDto addressDto)
    {
        final AddressEntity addressEntity = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressEntity.setFullName(addressDto.getFullName());
        addressEntity.setStreet(addressDto.getStreet());
        addressEntity.setCity(addressDto.getCity());
        addressEntity.setState(addressDto.getState());
        addressEntity.setCountry(addressDto.getCountry());
        addressEntity.setPostalCode(addressDto.getPostalCode());
        addressEntity.setPhoneNumber(addressDto.getPhoneNumber());

        return convertToDto(addressRepository.save(addressEntity));
    }

    public List<AddressDto> getAllAddresses()
    {
        return addressRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    public AddressDto getAddressById(final UUID addressId)
    {
        final AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        return convertToDto(address);
    }

    public void getDeleteById(final UUID addressId)
    {
        final AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }

    private AddressDto convertToDto(final AddressEntity addressEntity)
    {
        final AddressDto addressDto = new AddressDto();

        addressDto.setFullName(addressEntity.getFullName());
        addressDto.setStreet(addressEntity.getStreet());
        addressDto.setCity(addressEntity.getCity());
        addressDto.setState(addressEntity.getState());
        addressDto.setCountry(addressEntity.getCountry());
        addressDto.setPostalCode(addressEntity.getPostalCode());
        addressDto.setPhoneNumber(addressEntity.getPhoneNumber());
        addressDto.setUserId(addressEntity.getUser().getId());

        return addressDto;
    }

    private AddressEntity convertToEntity(final AddressDto addressDto)
    {
        final AddressEntity addressEntity = new AddressEntity();

        addressEntity.setFullName(addressDto.getFullName());
        addressEntity.setStreet(addressDto.getStreet());
        addressEntity.setCity(addressDto.getCity());
        addressEntity.setState(addressDto.getState());
        addressEntity.setCountry(addressDto.getCountry());
        addressEntity.setPostalCode(addressDto.getPostalCode());
        addressEntity.setPhoneNumber(addressDto.getPhoneNumber());

        final UserEntity user = userRepository.findById(addressDto.getUserId())
                .orElseThrow(() -> new UserNameNotFoundException("User not found with ID: " + addressDto.getUserId()));

        addressEntity.setUser(user);

        return addressEntity;
    }
}
