package com.payroll.service.impl;

import com.payroll.dto.request.BankRequestDTO;
import com.payroll.dto.response.BankResponseDTO;
import com.payroll.entity.Bank;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.BankMapper;
import com.payroll.repository.BankRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BankServiceImpl implements BankService {

    private final BankRepository bankRepository;
    private final UsrRepository usrRepository;
    private final BankMapper bankMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BankResponseDTO> getAllBanks(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException("Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Bank> records = "all".equals(isActive)
                ? bankRepository.findAll(sort)
                : bankRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(bankMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BankResponseDTO getBankById(Long id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", id));
        return bankMapper.toResponseDTO(bank);
    }

    @Override
    public BankResponseDTO createBank(BankRequestDTO requestDTO) {
        if (bankRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException("Bank with code '" + requestDTO.getCode() + "' already exists");
        }
        Bank entity = bankMapper.toEntity(requestDTO);
        entity.setIsActive(true);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        return bankMapper.toResponseDTO(bankRepository.save(entity));
    }

    @Override
    public BankResponseDTO updateBank(Long id, BankRequestDTO requestDTO) {
        Bank existing = bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", id));
        if (!existing.getCode().equalsIgnoreCase(requestDTO.getCode())
                && bankRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException("Bank with code '" + requestDTO.getCode() + "' already exists");
        }
        bankMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return bankMapper.toResponseDTO(bankRepository.save(existing));
    }

    @Override
    public void deleteBank(Long id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", id));
        bankRepository.delete(bank);
    }
}
