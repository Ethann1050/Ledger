package com.example.demo;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
class TestDataCleaner {
    @Autowired private AccountRepo accountRepo;
    @Autowired private LedgerRepo ledgerRepo;
    @Autowired private ProcessedTransferRepo processedTransferRepo;

    @Transactional
    public void cleanAll() {
        ledgerRepo.deleteAll();
        accountRepo.deleteAll();
        processedTransferRepo.deleteAll();
    }
}

@SpringBootTest
class TransferServiceConcurrencyTest {


    @Autowired
    private TestDataCleaner testDataCleaner;

    @BeforeEach
    void setUp() {
        testDataCleaner.cleanAll();
    }

    @AfterEach
    void tearDown() {
        testDataCleaner.cleanAll();
    }

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransferService transferService;


    @Test
    void testConcurrentTransfersAvoidRaceConditions() throws InterruptedException {
        Account sender = accountRepo.save(new Account(new BigDecimal("1000.00"), "Alice"));
        Account receiver = accountRepo.save(new Account(new BigDecimal("100.00"), "Bob"));

        Long senderId = sender.getId();
        Long receiverId = receiver.getId();

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final String uniqueKey = "IDEMPOTENCY-KEY-" + i;
            executorService.submit(() -> {
                try {
                    transferService.transfer(senderId, receiverId, new BigDecimal("10.00"), uniqueKey);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        Account updatedSender = accountRepo.findById(senderId).orElseThrow();
        Account updatedReceiver = accountRepo.findById(receiverId).orElseThrow();

        // 1000.00 - (10 * 10.00) = 900.00
        assertEquals(0, new BigDecimal("900.00").compareTo(updatedSender.getBalance()));
        // 100.00 + (10 * 10.00) = 200.00
        assertEquals(0, new BigDecimal("200.00").compareTo(updatedReceiver.getBalance()));
    }

    @Test
    void testDeadlockPreventionCrossTransfers() throws InterruptedException {
        Account acc1 = accountRepo.save(new Account(new BigDecimal("1000.00"), "Alice"));
        Account acc2 = accountRepo.save(new Account(new BigDecimal("1000.00"), "Bob"));

        Long id1 = acc1.getId();
        Long id2 = acc2.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // Thread 1: Transports 1 -> 2
        executor.submit(() -> {
            try {
                transferService.transfer(id1, id2, new BigDecimal("50.00"), "KEY-DIRECTION-1");
            } finally {
                latch.countDown();
            }
        });

        // Thread 2: Transports 2 -> 1
        executor.submit(() -> {
            try {
                transferService.transfer(id2, id1, new BigDecimal("30.00"), "KEY-DIRECTION-2");
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        Account updated1 = accountRepo.findById(id1).orElseThrow();
        Account updated2 = accountRepo.findById(id2).orElseThrow();

        // 1000 - 50 + 30 = 980.00
        assertEquals(0, new BigDecimal("980.00").compareTo(updated1.getBalance()));
        // 1000 + 50 - 30 = 1020.00
        assertEquals(0, new BigDecimal("1020.00").compareTo(updated2.getBalance()));
    }

    @Test
    void testIdempotencyPreventsDuplicateTransfers() {
        Account sender = accountRepo.save(new Account(new BigDecimal("1000.00"), "Alice"));
        Account receiver = accountRepo.save(new Account(new BigDecimal("100.00"), "Bob"));

        String sharedKey = "DUPLICATE-KEY-123";

        // First call: succeeds
        transferService.transfer(sender.getId(), receiver.getId(), new BigDecimal("100.00"), sharedKey);
        // Duplicate call: ignored
        transferService.transfer(sender.getId(), receiver.getId(), new BigDecimal("100.00"), sharedKey);

        Account updatedSender = accountRepo.findById(sender.getId()).orElseThrow();
        Account updatedReceiver = accountRepo.findById(receiver.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("900.00").compareTo(updatedSender.getBalance()));
        assertEquals(0, new BigDecimal("200.00").compareTo(updatedReceiver.getBalance()));
    }

    @Test
    void testTransferFailsOnInsufficientFunds() {
        Account sender = accountRepo.save(new Account(new BigDecimal("10.00"), "Broke Person"));
        Account receiver = accountRepo.save(new Account(new BigDecimal("100.00"), "Rich Person"));

        try {
            transferService.transfer(sender.getId(), receiver.getId(), new BigDecimal("50.00"), "KEY-BROKE");
        } catch (IllegalStateException e) {
            assertEquals("Insufficient funds", e.getMessage());
        }

        Account unchangedSender = accountRepo.findById(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("10.00").compareTo(unchangedSender.getBalance()));
    }
}