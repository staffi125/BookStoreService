package com.spring.project.service;

import com.spring.project.dto.ClientDTO;

import java.util.List;

public interface ClientService {

    List<ClientDTO> getAllClients();

    ClientDTO getClientByEmail(String email);

    ClientDTO updateClientByEmail(String email, ClientDTO client);

    void deleteClientByEmail(String email);

    ClientDTO addClient(ClientDTO client);

    ClientDTO updateOwnProfile(String email, ClientDTO client);
}
