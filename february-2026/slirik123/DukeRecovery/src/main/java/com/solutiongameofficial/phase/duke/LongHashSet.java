package com.solutiongameofficial.phase.duke;

/**
 * Tiny open addressing long set, iteration supported via values()
 */
public final class LongHashSet {

    private long[] table;
    private boolean[] used;
    private int size;

    public LongHashSet(int expected) {
        int cap = 1;
        while (cap < expected * 2) {
            cap <<= 1;
        }
        table = new long[cap];
        used = new boolean[cap];
    }

    public void add(long value) {
        if (size * 2 >= table.length) {
            rehash();
        }

        int mask = table.length - 1;
        int idx = mix(value) & mask;

        while (used[idx]) {
            if (table[idx] == value) {
                return;
            }
            idx = (idx + 1) & mask;
        }

        used[idx] = true;
        table[idx] = value;
        size++;
    }

    public int size() {
        return size;
    }

    public long[] values() {
        long[] output = new long[size];
        int j = 0;
        for (int i = 0; i < table.length; i++) {
            if (used[i]) {
                output[j++] = table[i];
            }
        }
        return output;
    }

    private void rehash() {
        long[] oldTable = table;
        boolean[] oldUsed = used;

        table = new long[oldTable.length << 1];
        used = new boolean[oldUsed.length << 1];
        size = 0;

        for (int index = 0; index < oldTable.length; index++) {
            if (oldUsed[index]) {
                add(oldTable[index]);
            }
        }
    }

    private static int mix(long z) {
        z ^= (z >>> 33);
        z *= 0xff51afd7ed558ccdL;
        z ^= (z >>> 33);
        z *= 0xc4ceb9fe1a85ec53L;
        z ^= (z >>> 33);
        return (int) z;
    }
}