package com.example.agrichain.service;

import com.example.agrichain.dto.BlockchainResponseDTO;
import com.example.agrichain.model.BlockchainTransaction;
import com.example.agrichain.model.Order;
import com.example.agrichain.model.Product;
import com.example.agrichain.repository.BlockchainTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

@Service
public class BlockchainService {

    private final Web3j web3j;
    private final BlockchainTransactionRepository transactionRepository;

    @Value("${ethereum.contract.address}")
    private String contractAddress;

    @Value("${ethereum.private.key}")
    private String privateKey;

    // ✅ Manual constructor (No Lombok)
    public BlockchainService(Web3j web3j,
            BlockchainTransactionRepository transactionRepository) {
        this.web3j = web3j;
        this.transactionRepository = transactionRepository;
    }

    public String registerProduct(Product product) throws Exception {

        Credentials credentials = Credentials.create(privateKey);

        String txHash = simulateTransaction("REGISTER_PRODUCT");

        BlockchainTransaction transaction = new BlockchainTransaction();
        transaction.setTransactionHash(txHash);
        transaction.setTransactionType("PRODUCT_REGISTRATION");
        transaction.setFromAddress(credentials.getAddress());
        transaction.setToAddress(contractAddress);
        transaction.setProduct(product);
        transaction.setBlockNumber(1L);   // simulated
        transaction.setGasUsed(21000L);   // simulated

        transactionRepository.save(transaction);

        return txHash;
    }

    public String recordOrder(Order order) throws Exception {

        Credentials credentials = Credentials.create(privateKey);

        String txHash = simulateTransaction("RECORD_ORDER");

        BlockchainTransaction transaction = new BlockchainTransaction();
        transaction.setTransactionHash(txHash);
        transaction.setTransactionType("ORDER_RECORD");
        transaction.setFromAddress(credentials.getAddress());
        transaction.setToAddress(contractAddress);
        transaction.setOrder(order);
        transaction.setBlockNumber(1L);
        transaction.setGasUsed(21000L);

        transactionRepository.save(transaction);

        return txHash;
    }

    public BlockchainResponseDTO verifyProduct(String blockchainHash) {

        BlockchainTransaction transaction =
                transactionRepository.findByTransactionHash(blockchainHash)
                        .orElseThrow(() ->
                                new RuntimeException("Transaction not found")
                        );

        BlockchainResponseDTO response = new BlockchainResponseDTO();
        response.setTransactionHash(transaction.getTransactionHash());
        response.setBlockNumber(transaction.getBlockNumber());
        response.setStatus("VERIFIED");
        response.setGasUsed(transaction.getGasUsed());
        response.setMessage("Product verified on blockchain");

        return response;
    }

    // Simulated transaction
    private String simulateTransaction(String type) {

        return "0x"
                + java.util.UUID.randomUUID().toString().replace("-", "")
                + java.util.UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 4);
    }

    public boolean isConnected() {

        try {
            return web3j.web3ClientVersion()
                    .send()
                    .getWeb3ClientVersion() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
