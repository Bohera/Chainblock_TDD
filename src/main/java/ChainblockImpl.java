import java.util.*;
import java.util.stream.Collectors;

public class ChainblockImpl implements Chainblock {
    private Map<Integer, Transaction> transactionMap;

    public ChainblockImpl() {
        this.transactionMap = new LinkedHashMap<>();
    }

    public int getCount() {
        return transactionMap.size();
    }

    public void add(Transaction transaction) {
        transactionMap.putIfAbsent(transaction.getId(), transaction);
    }

    public boolean contains(Transaction transaction) {
        return contains(transaction.getId());
    }

    public boolean contains(int id) {
        return transactionMap.containsKey(id);
    }

    public void changeTransactionStatus(int id, TransactionStatus newStatus) {
        Transaction transaction = getById(id);
        transaction.setStatus(newStatus);
    }

    public void removeTransactionById(int id) {
        checkForTransactionThrow(id);
        transactionMap.remove(id);
    }

    public Transaction getById(int id) {
        checkForTransactionThrow(id);
        return transactionMap.get(id);
    }

    public Iterable<Transaction> getByTransactionStatus(TransactionStatus status) {
        List<Transaction> transactions = transactionMap.values().stream()
                .filter(t -> t.getStatus().equals(status))
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .collect(Collectors.toList());

        if (transactions.size() == 0) {
            throw new IllegalArgumentException();
        }
        return transactions;
    }

    public Iterable<String> getAllSendersWithTransactionStatus(TransactionStatus status) {
        List<Transaction> transactionsByStatus = convertIterableToList(getByTransactionStatus(status));
        return transactionsByStatus.stream()
                .map(Transaction::getFrom)
                .collect(Collectors.toList());
    }

    private List<Transaction> convertIterableToList(Iterable<Transaction> iterable) {
        List<Transaction> list = new ArrayList<>();

        iterable.forEach(item -> list.add(item));
        return list;
    }

    public Iterable<String> getAllReceiversWithTransactionStatus(TransactionStatus status) {
        List<Transaction> transactionsByStatus = convertIterableToList(getByTransactionStatus(status));
        return transactionsByStatus.stream()
                .map(Transaction::getTo)
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getAllOrderedByAmountDescendingThenById() {
        return transactionMap.values().stream()
                .sorted(Comparator.comparing(Transaction::getAmount).reversed().thenComparing(Transaction::getId))
                .collect(Collectors.toList());

    }

    public Iterable<Transaction> getBySenderOrderedByAmountDescending(String sender) {
        List<Transaction> result = transactionMap.values()
                .stream()
                .filter(t -> t.getFrom().equals(sender))
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .collect(Collectors.toList());
        if (result.size() == 0) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public Iterable<Transaction> getByReceiverOrderedByAmountThenById(String receiver) {
        List<Transaction> sortedTransactions = convertIterableToList(getAllOrderedByAmountDescendingThenById());
        List<Transaction> result = sortedTransactions.stream()
                .filter(t -> t.getTo().equals(receiver))
                .collect(Collectors.toList());

        if (result.size() == 0) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public Iterable<Transaction> getByTransactionStatusAndMaximumAmount(TransactionStatus status, double amount) {
        return transactionMap.values()
                .stream()
                .filter(t -> t.getStatus().equals(status) && t.getAmount() < amount).collect(Collectors.toList());
    }

    public Iterable<Transaction> getBySenderAndMinimumAmountDescending(String sender, double amount) {
        List<Transaction> result = transactionMap.values()
                .stream()
                .filter(t -> t.getFrom().equals(sender) && t.getAmount() > amount)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .collect(Collectors.toList());

        if (result.size() == 0) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public Iterable<Transaction> getByReceiverAndAmountRange(String receiver, double lo, double hi) {
        List<Transaction> transactionsByRange = convertIterableToList(getAllInAmountRange(lo, hi));
        List<Transaction> transactionsByRangeAndReceiver = transactionsByRange.stream()
                .filter(t -> t.getTo().equals(receiver))
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .collect(Collectors.toList());

        if (transactionsByRangeAndReceiver.size() == 0) {
            throw new IllegalArgumentException();
        }
        return transactionsByRangeAndReceiver;
    }

    public Iterable<Transaction> getAllInAmountRange(double lo, double hi) {
        return transactionMap.values()
                .stream()
                .filter(t -> t.getAmount() < hi && t.getAmount() > lo)
                .collect(Collectors.toList());
    }

    public Iterator<Transaction> iterator() {
        return null;
    }

    private void checkForTransactionThrow(int id) {
        if (!transactionMap.containsKey(id)) {
            throw new IllegalArgumentException();
        }
    }
}
