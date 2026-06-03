package com.payroll.service.impl;

import com.payroll.dto.request.BankBranchRequestDTO;
import com.payroll.dto.response.BankBranchResponseDTO;
import com.payroll.entity.BankBranch;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.BankBranchMapper;
import com.payroll.repository.BankBranchRepository;
import com.payroll.repository.BankRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.BankBranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BankBranchServiceImpl implements BankBranchService {

    private final BankBranchRepository bankBranchRepository;
    private final BankRepository bankRepository;
    private final UsrRepository usrRepository;
    private final BankBranchMapper bankBranchMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BankBranchResponseDTO> getAllBankBranches(String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException("Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<BankBranch> records = "all".equals(isActive)
                ? bankBranchRepository.findAll(sort)
                : bankBranchRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream().map(bankBranchMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankBranchResponseDTO> getBankBranchesByBankId(Long bankId) {
        if (!bankRepository.existsById(bankId)) {
            throw new ResourceNotFoundException("Bank", "id", bankId);
        }
        return bankBranchRepository.findAllByBankId(bankId, Sort.by("id").ascending())
                .stream().map(bankBranchMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BankBranchResponseDTO getBankBranchById(Long id) {
        BankBranch bankBranch = bankBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankBranch", "id", id));
        return bankBranchMapper.toResponseDTO(bankBranch);
    }

    @Override
    public BankBranchResponseDTO createBankBranch(BankBranchRequestDTO requestDTO) {
        if (bankBranchRepository.existsByBankIdAndBranchCodeIgnoreCase(requestDTO.getBankId(), requestDTO.getBranchCode())) {
            throw new IllegalArgumentException("Branch code '" + requestDTO.getBranchCode() + "' already exists for this bank");
        }
        BankBranch entity = bankBranchMapper.toEntity(requestDTO);
        entity.setIsActive(true);
        entity.setBank(bankRepository.getReferenceById(requestDTO.getBankId()));
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        return bankBranchMapper.toResponseDTO(bankBranchRepository.save(entity));
    }

    @Override
    public BankBranchResponseDTO updateBankBranch(Long id, BankBranchRequestDTO requestDTO) {
        BankBranch existing = bankBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankBranch", "id", id));
        if (!existing.getBranchCode().equalsIgnoreCase(requestDTO.getBranchCode())
                && bankBranchRepository.existsByBankIdAndBranchCodeIgnoreCase(requestDTO.getBankId(), requestDTO.getBranchCode())) {
            throw new IllegalArgumentException("Branch code '" + requestDTO.getBranchCode() + "' already exists for this bank");
        }
        bankBranchMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getBankId() != null) {
            existing.setBank(bankRepository.getReferenceById(requestDTO.getBankId()));
        }
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return bankBranchMapper.toResponseDTO(bankBranchRepository.save(existing));
    }

    @Override
    public void deleteBankBranch(Long id) {
        BankBranch bankBranch = bankBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankBranch", "id", id));
        bankBranchRepository.delete(bankBranch);
    }
}
