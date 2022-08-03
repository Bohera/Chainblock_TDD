import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ChainblockImplTest {

    private Chainblock chainblock;
    private List<Transaction> transactions;

    @Before
    public void prepare() {
        this.chainblock = new ChainblockImpl();
        this.transactions = new ArrayList<>();
        this.prepareTransactions();

    }

    private void fillChainblockWithTransactions() {
        transactions.forEach(t -> chainblock.add(t));
    }

    private void prepareTransactions() {
        Transaction transaction = new TransactionImpl(0, TransactionStatus.SUCCESSFUL, "Pesho", "Sasho", 11.20);
        Transaction transaction1 = new TransactionImpl(1, TransactionStatus.SUCCESSFUL, "Pesho", "Toshko", 10);
        Transaction transaction2 = new TransactionImpl(2, TransactionStatus.UNAUTHORIZED, "Sasho", "Pesho", 11.0);
        Transaction transaction3 = new TransactionImpl(3, TransactionStatus.FAILED, "Toshko", "Sasho", 12.20);
        Transaction transaction4 = new TransactionImpl(4, TransactionStatus.SUCCESSFUL, "Sasho", "Pesho", 10.50);
        transactions.add(transaction);
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        transactions.add(transaction4);
    }

    @Test
    public void testAdd_ShouldAddTransaction() {
        chainblock.add(transactions.get(0));
        assertEquals(1, chainblock.getCount());
        chainblock.add(transactions.get(1));
        assertEquals(2, chainblock.getCount());

    }

    @Test
    public void testAdd_ShouldNotAddDuplicateTransaction(){
        chainblock.add(transactions.get(0));
        chainblock.add(transactions.get(0));
        assertEquals(1, chainblock.getCount());
    }

    @Test
    public void testContains_WithTransactionShouldReturnFalse() {
        chainblock.add(transactions.get(0));
        boolean chainBlockContainsTransaction = chainblock.contains(transactions.get(1));
        assertFalse(chainBlockContainsTransaction);
    }

    @Test
    public void testContains_WithTransactionShouldReturnTrue() {
        chainblock.add(transactions.get(1));
        boolean chainBlockContainsTransaction = chainblock.contains(transactions.get(1));
        assertTrue(chainBlockContainsTransaction);
    }

    @Test
    public void testContainsWithID_ShouldReturnFalse() {
        chainblock.add(transactions.get(0));
        boolean chainBlockContainsID = chainblock.contains(transactions.get(1).getId());
        assertFalse(chainBlockContainsID);
    }

    @Test
    public void testContainsWithID_ShouldReturnTrue() {
        chainblock.add(transactions.get(0));
        boolean chainBlockContainsID = chainblock.contains(transactions.get(0).getId());
        assertTrue(chainBlockContainsID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeTransactionStatus_ShouldThrowForMissingTransaction() {
        chainblock.changeTransactionStatus(100, TransactionStatus.FAILED);
    }

    @Test
    public void testChangeTransactionStatus_ShouldChangeStatus() {
        chainblock.add(transactions.get(0));

        chainblock.changeTransactionStatus(transactions.get(0).getId(), TransactionStatus.FAILED);

        TransactionStatus newTransactionStatus = chainblock.getById(transactions.get(0).getId()).getStatus();

        assertEquals(TransactionStatus.FAILED, newTransactionStatus);
    }

    @Test
    public void testGetByID_ShouldReturnTransaction() {
        chainblock.add(transactions.get(0));

        Transaction actual = chainblock.getById(transactions.get(0).getId());

        assertEquals(transactions.get(0), actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByID_ShouldThrowForMissingTransaction() {
        fillChainblockWithTransactions();

        chainblock.getById(199);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTransactionByID_ShouldThrowForMissingTransaction() {
        fillChainblockWithTransactions();

        chainblock.removeTransactionById(200);
    }

    @Test
    public void testRemoveTransactionByID_ShouldRemove() {
        fillChainblockWithTransactions();

        chainblock.removeTransactionById(1);

        assertFalse(chainblock.contains(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByTransactionStatus_ShouldThrowIfNoSuchTransactions() {
        fillChainblockWithTransactions();

        chainblock.getByTransactionStatus(TransactionStatus.ABORTED);
    }

    @Test
    public void testGetByTransactionStatus_ShouldReturnSortedByDescending() {
        fillChainblockWithTransactions();
        Iterable<Transaction> expectedTransactions = transactions.stream()
                .filter(t -> t.getStatus().equals(TransactionStatus.SUCCESSFUL))
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .collect(Collectors.toList());

        Iterable<Transaction> actualTransactions = chainblock.getByTransactionStatus(TransactionStatus.SUCCESSFUL);
        assertEquals(expectedTransactions, actualTransactions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSendersWithTransactionStatus_ShouldThrowIfNoTransactions() {
        fillChainblockWithTransactions();

        chainblock.getAllSendersWithTransactionStatus(TransactionStatus.ABORTED);
    }

    @Test
    public void testGetAllSendersWithTransactionStatus_ShouldReturnSortedNames() {
        fillChainblockWithTransactions();
        List<String> expectedTransactionSenders = transactions.stream()
                .filter(t -> t.getStatus().equals(TransactionStatus.SUCCESSFUL))
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .map(Transaction::getFrom)
                .collect(Collectors.toList());

        Iterable<String> actualTransactionSenders = chainblock.getAllSendersWithTransactionStatus(TransactionStatus.SUCCESSFUL);

        assertEquals(expectedTransactionSenders, actualTransactionSenders);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllReceiversWithTransactionStatus_ShouldThrowIfNoTransactions() {
        fillChainblockWithTransactions();

        chainblock.getAllSendersWithTransactionStatus(TransactionStatus.ABORTED);
    }

    @Test
    public void testGetAllReceiversWithTransactionStatus_ShouldReturnSortedNames() {
        fillChainblockWithTransactions();
        List<String> expectedTransactionReceivers = transactions.stream()
                .filter(t -> t.getStatus().equals(TransactionStatus.SUCCESSFUL))
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .map(Transaction::getTo)
                .collect(Collectors.toList());

        Iterable<String> actualTransactionReceivers = chainblock.getAllReceiversWithTransactionStatus(TransactionStatus.SUCCESSFUL);

        assertEquals(expectedTransactionReceivers, actualTransactionReceivers);
    }



}