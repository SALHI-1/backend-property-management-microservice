package com.lsiproject.app.propertymanagementmicroservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
public class Web3jConfig {

    @Value("${web3j.client-address}")
    private String clientAddress;

    @Value("${wallet.private-key}")
    private String privateKey;

    // Gas Price and Limit constants (Use standard values or fetch from network)
    // NOTE: These should be optimized for the target network (e.g., Mainnet, Sepolia)
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(6_721_975L); // Standard limit

    /**
     * Creates the Web3j client connection bean.
     */
    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(clientAddress));
    }

    /**
     * Credentials for signing transactions (using the private key).
     */
    @Bean
    public Credentials credentials() {
        // IMPORTANT: In production, load this key from a secure vault, not plaintext properties.
        return Credentials.create(privateKey);
    }

    /**
     * Gas provider for transactions. We use a static provider to control gas fees.
     */
    @Bean
    public StaticGasProvider gasProvider() {
        // You can switch to DefaultGasProvider() for networks where gas estimation works well.
        return new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
    }
}