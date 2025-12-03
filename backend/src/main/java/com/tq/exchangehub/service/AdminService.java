package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AdminUserDto;
import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final ItemRepository itemRepository;

    public AdminService(UserAccountRepository userAccountRepository, ItemRepository itemRepository) {
        this.userAccountRepository = userAccountRepository;
        this.itemRepository = itemRepository;
    }

    public List<AdminUserDto> getUsers() {
        return userAccountRepository.findAll().stream()
                .map(this::toAdminUserDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> getItems() {
        return itemRepository.findAll().stream().map(DtoMapper::toItemDto).collect(Collectors.toList());
    }

    private AdminUserDto toAdminUserDto(UserAccount account) {
        return new AdminUserDto(
                account.getId(),
                account.getEmail(),
                account.getRole(),
                account.getProfile() != null ? account.getProfile().getDisplayName() : null,
                account.getCreatedAt());
    }
}
